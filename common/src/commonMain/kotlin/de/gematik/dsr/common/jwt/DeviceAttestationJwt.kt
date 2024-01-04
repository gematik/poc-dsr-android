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

package de.gematik.dsr.common.jwt

import DeviceAttestation
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityToken
import de.gematik.dsr.common.keystore.MTLSKEYPAIRALIAS
import de.gematik.dsr.common.keystore.getPrivateKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.jose4j.base64url.Base64
import org.jose4j.jws.JsonWebSignature
import java.security.MessageDigest
import java.security.cert.Certificate
import java.util.*

/**
 * DeviceAttestationJwt.kt
 *
 * This file contains DeviceAttestationJwt class,
 * which is used to create and sign the DeviceAttestationJwt.
 */

class DeviceAttestationJwt {
    companion object {
        fun buildAndSignJwt(jwtBody: String, mTLSCert: Certificate): String {
            return signJws(buildJws(jwtBody, mTLSCert))
        }
        private fun signJws(jws: JsonWebSignature): String {
            jws.key = getPrivateKey(MTLSKEYPAIRALIAS)
            jws.sign()
            return jws.compactSerialization
        }
        private fun buildJws(body: String, mTLSCertificate: Certificate): JsonWebSignature {
            val jws = JsonWebSignature()
            jws.algorithmHeaderValue = "ES256"
            jws.setHeader("x5c", listOf(Base64.encode(mTLSCertificate.encoded)))
            jws.payload = body
            return jws
        }

        fun buildJwtBody(
            type: String = "android",
            nonce: ByteArray,
            iat: Date,
            packageName: String = "de.gematik.dsr.android",
            trustVersion: String = "TrustSDK_$SDKVERSION",
            pubKey: ByteArray,
            attestationCertChain: Array<Certificate>,
            integrityVerdict: StandardIntegrityToken,
            deviceAttributes: DeviceAttestation.DeviceAttributes,
        ): String {
            val base64Encoder = java.util.Base64.getEncoder()
            val pubKeySubject = SubjectPublicKeyInfo.getInstance(pubKey)
            val pubKeyBytes = pubKeySubject.publicKeyData.bytes
            val pubKeyHash = MessageDigest.getInstance("SHA-256").digest(pubKeyBytes)

            return Json.encodeToString(
                DeviceAttestation(
                    iss = trustVersion,
                    sub = base64Encoder.encodeToString(pubKeyHash),
                    iat = iat.time / 1000, // no milliseconds
                    type = type,
                    nonce = base64Encoder.encodeToString(nonce),
                    attestationCertChain = attestationCertChain.map { base64Encoder.encodeToString(it.encoded) },
                    integrityVerdict = integrityVerdict.token(),
                    packageName = packageName,
                    deviceAttributes = deviceAttributes,
                ),
            )
        }
    }
}
