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

package de.gematik.dsr.common.deviceAttestation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import de.gematik.dsr.common.gms.GMSException
import de.gematik.dsr.common.gms.GmsRepository
import de.gematik.dsr.common.jwt.DeviceAttestationJwt
import de.gematik.dsr.common.keystore.getAttestationChain
import de.gematik.dsr.common.keystore.getCertificate
import de.gematik.dsr.common.playIntegrityApi.callStandardIntegrityApi
import de.gematik.dsr.common.utils.generateNonce
import de.gematik.dsr.common.utils.provider.secureRandomInstance
import getDeviceAttributes
import kotlinx.coroutines.delay
import org.jose4j.base64url.Base64Url
import java.security.MessageDigest
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * DeviceAttestationUseCase.kt
 *
 * This file contains the DeviceAttestationUseCase,
 * which performs the deviceAttestation at the gms.
 */

private const val VERIFIERLENGTH = 64
private const val PAUSEMILLIS = 2000L // 2 seconds
private const val DURATIONMILLIS = 60000 // 1 minute

class DeviceAttestationUseCase(
    private val gmsRepository: GmsRepository,
    private val context: Context,
) {
    var mockDevAttestToken = ""
    var mockChallenge = ""
    var mockVerifier = ""
    var mockAuthCode = ""
    var mockToken = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun runDeviceAttestation(): String {
        // get the nonce from the gms via tls
        val gmsNonce = gmsRepository.getNonce()
        // generate a codeVerifier and codeChallenge
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)
        // derive new nonces out of the nonce sent by the gms
        val attestNonce = generateNonce(gmsNonce, "1".toByteArray())
        val playIntegrityApiNonce = generateNonce(gmsNonce, "2".toByteArray())
        // get the current time
        val currentTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
        // get the attestationChain
        val attestationCertChain = getAttestationChain(context, attestNonce, currentTime)
        // get the integrityVerdict from the Google integrity api
        val integrityVerdict = callStandardIntegrityApi(context, playIntegrityApiNonce)
        // collect the security Attributes of the device
        val deviceAttributes = getDeviceAttributes(context)
        // create the jwt body using the previous gathered information
        val jwtBody = DeviceAttestationJwt.buildJwtBody(
            nonce = gmsNonce,
            iat = Date.from(currentTime.toInstant(ZoneOffset.UTC)),
            pubKey = getCertificate().publicKey.encoded,
            attestationCertChain = attestationCertChain,
            integrityVerdict = integrityVerdict,
            deviceAttributes = deviceAttributes,
        )
        // build and sign the jwt using the mtls private key
        val signedJwt = DeviceAttestationJwt.buildAndSignJwt(jwtBody, mTLSCert = getCertificate())
        // send the jwt to the gms to retrieve an authentication code via mtls
        val authCode = gmsRepository.postDeviceAttestation(signedJwt, codeChallenge, context)
        // try to get the deviceToken from the gms using the authCode via mtls. This might take some time.
        val startTime = System.currentTimeMillis()
        var deviceToken: String
        do {
            delay(PAUSEMILLIS)
            deviceToken = gmsRepository.postDeviceToken(authCode, codeVerifier, context)
            val elapsedTime = System.currentTimeMillis() - startTime
        } while (elapsedTime < DURATIONMILLIS && deviceToken.isBlank())
        if (deviceToken.isBlank()) {
            throw GMSException("Token way not ready", "202")
        }
        // data used for debugging
        mockDevAttestToken = signedJwt
        mockVerifier = codeVerifier
        mockChallenge = codeChallenge
        mockAuthCode = authCode
        mockToken = deviceToken
        return deviceToken
    }
}

fun generateCodeVerifier(): String {
    return Base64Url.encode(
        ByteArray(VERIFIERLENGTH).apply {
            secureRandomInstance().nextBytes(this)
        },
    )
}

fun generateCodeChallenge(codeVerifier: String): String {
    return Base64Url.encode(
        MessageDigest.getInstance("SHA-256").apply {
            update(codeVerifier.toByteArray(Charsets.UTF_8))
        }.digest(),
    )
}
