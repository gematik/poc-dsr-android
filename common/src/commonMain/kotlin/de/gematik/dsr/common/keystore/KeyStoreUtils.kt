/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.dsr.common.keystore

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.io.ByteArrayInputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * KeyStoreUtils.kt
 *
 * This file contains the KeyStoreUtils,
 * which are functions working the android keystore.
 */

private const val KEYSIZE = 256
private const val EXPIRYTIME = 365
const val MTLSCERTALIAS = "mTLSCertificate"
const val MTLSKEYPAIRALIAS = "mTLSKeyPair"
const val ATTESTKEYPAIRALIAS = "attestKey"
const val ATTESTATIONKEYPAIRALIAS = "attestationKeyPair"

/**
 * function to create a keypair which is used for signing
 */
@RequiresApi(Build.VERSION_CODES.S)
fun generateKeypair(
    nonce: ByteArray,
    currentTime: LocalDateTime,
    alias: String,
    useStrongBox: Boolean = false,
    useAttestKey: Boolean = false,
): KeyPair {
    val startDate = Date.from(currentTime.toInstant(ZoneOffset.UTC))
    val expiryDate = Date.from(
        currentTime.plusDays(EXPIRYTIME.toLong()).toInstant(
            ZoneOffset.UTC,
        ),
    )
    val keyPairGenerator = KeyPairGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_EC,
        "AndroidKeyStore",
    )
    val keySpecBuilder = KeyGenParameterSpec.Builder(
        alias,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
    ).apply {
        setDigests(KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA256)
        setDevicePropertiesAttestationIncluded(true)
        setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
        setKeySize(KEYSIZE)
        setKeyValidityStart(startDate)
        setKeyValidityEnd(expiryDate)
        setAttestationChallenge(nonce)
        if (useStrongBox) {
            setIsStrongBoxBacked(true)
        }
        if (useAttestKey) {
            setAttestKeyAlias(ATTESTKEYPAIRALIAS)
        }
    }
    keyPairGenerator.initialize(keySpecBuilder.build())

    return keyPairGenerator.generateKeyPair()
}

/**
 * function to create a keypair which is used for attesting the integrity of the keystore
 */
@RequiresApi(Build.VERSION_CODES.S)
fun generateAttestKey(
    attestNonce: ByteArray,
    currentTime: LocalDateTime,
    alias: String,
    useStrongBox: Boolean = false,
): KeyPair {
    val startDate = Date.from(currentTime.toInstant(ZoneOffset.UTC))
    val keyPairGenerator = KeyPairGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_EC,
        "AndroidKeyStore",
    )
    val keySpecBuilder = KeyGenParameterSpec.Builder(
        alias,
        KeyProperties.PURPOSE_ATTEST_KEY,
    ).apply {
        setDigests(KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA256)
        setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
        setKeySize(KEYSIZE)
        setKeyValidityStart(startDate)
        setAttestationChallenge(attestNonce)
        if (useStrongBox) {
            setIsStrongBoxBacked(true)
        }
    }
    keyPairGenerator.initialize(keySpecBuilder.build())

    return keyPairGenerator.generateKeyPair()
}

/**
 * function to create a keychain which consists of the attest chain with an added new leaf certificate
 */
@RequiresApi(Build.VERSION_CODES.S)
fun getAttestationChain(
    context: Context,
    attestNonce: ByteArray,
    currentTime: LocalDateTime,
): Array<Certificate> {
    val keyStore = getKeyStore()
    keyStore.load(null)
    val packageManager = context.packageManager
    // generate a new key for fresh attestation results unless the persistent key is not yet created
    val canUseAttestKey = packageManager.hasSystemFeature(PackageManager.FEATURE_KEYSTORE_APP_ATTEST_KEY)
    var useAttestKey = false
    if (keyStore.containsAlias(MTLSKEYPAIRALIAS)) {
        if (keyStore.containsAlias(ATTESTATIONKEYPAIRALIAS)) {
            keyStore.deleteEntry(ATTESTATIONKEYPAIRALIAS)
        }
        useAttestKey = if (keyStore.containsAlias(ATTESTKEYPAIRALIAS)) {
            true
        } else {
            if (canUseAttestKey) {
                generateAttestKey(attestNonce, currentTime, ATTESTKEYPAIRALIAS)
                true
            } else {
                false
            }
        }
    }
    generateKeypair(attestNonce, currentTime, ATTESTATIONKEYPAIRALIAS, useAttestKey = useAttestKey)

    return if (useAttestKey) {
        val attestationCertificate = getCertificate(ATTESTATIONKEYPAIRALIAS)
        val attestCertificateChain = getCertificateChain(ATTESTKEYPAIRALIAS)
        arrayOf(attestationCertificate) + attestCertificateChain
    } else {
        getCertificateChain(ATTESTATIONKEYPAIRALIAS)
    }
}

fun getCertificateChain(alias: String = MTLSKEYPAIRALIAS): Array<Certificate> {
    val keyStore = getKeyStore()
    keyStore.load(null)
    return keyStore.getCertificateChain(alias) ?: throw Exception()
}

fun saveToKeyStore(mTLSCertificateBytes: ByteArray, alias: String = MTLSCERTALIAS) {
    val keyStore = getKeyStore()
    keyStore.load(null)
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val certificate = certificateFactory.generateCertificate(ByteArrayInputStream(mTLSCertificateBytes)) as X509Certificate
    keyStore.setEntry(alias, KeyStore.TrustedCertificateEntry(certificate), null)
}

fun getCertificate(alias: String = MTLSCERTALIAS): Certificate {
    val keyStore = getKeyStore()
    keyStore.load(null)
    return keyStore.getCertificate(alias) ?: throw Exception()
}

fun deviceRegistrationCompleted(): Boolean {
    val keyStore = getKeyStore()
    keyStore.load(null)
    return keyStore.getCertificate(MTLSCERTALIAS) != null
}

fun deleteAllKeys() {
    val keyStore = getKeyStore()
    keyStore.load(null)
    if (keyStore.containsAlias(MTLSCERTALIAS)) {
        keyStore.deleteEntry(MTLSCERTALIAS)
    }
    if (keyStore.containsAlias(MTLSCERTALIAS)) {
        keyStore.deleteEntry(MTLSKEYPAIRALIAS)
    }
    if (keyStore.containsAlias(ATTESTKEYPAIRALIAS)) {
        keyStore.deleteEntry(ATTESTKEYPAIRALIAS)
    }
    if (keyStore.containsAlias(ATTESTATIONKEYPAIRALIAS)) {
        keyStore.deleteEntry(ATTESTATIONKEYPAIRALIAS)
    }
}

fun getPrivateKey(alias: String = MTLSKEYPAIRALIAS): PrivateKey {
    val keyStore = getKeyStore()
    keyStore.load(null)
    return keyStore.getKey(alias, null) as PrivateKey
}

fun getKeyStore(type: String = "AndroidKeyStore"): KeyStore {
    return KeyStore.getInstance(type)
}
