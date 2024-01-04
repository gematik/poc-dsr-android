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

import ExtentedEllipticCurves
import com.google.android.play.core.integrity.IntegrityTokenResponse
import de.gematik.dsr.common.utils.provider.BCProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bouncycastle.asn1.pkcs.CertificationRequest
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.jce.ECNamedCurveTable
import org.jose4j.base64url.Base64
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Security
import java.security.cert.Certificate
import java.util.*

/**
 * DeviceRegistrationJwt.kt
 *
 * This file contains DeviceRegistrationJwt class,
 * which is used to create and sign the DeviceRegistrationJwt using a virtual healthcard.
 */

private const val DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE = "MIIDJjCCAsygAwIBAgIHARaNeArTFDAKBggqhkjOPQQDAjCBljELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxRTBDBgNVBAsMPEVsZWt0cm9uaXNjaGUgR2VzdW5kaGVpdHNrYXJ0ZS1DQSBkZXIgVGVsZW1hdGlraW5mcmFzdHJ1a3R1cjEfMB0GA1UEAwwWR0VNLkVHSy1DQTEwIFRFU1QtT05MWTAeFw0yMDExMjcwMDAwMDBaFw0yMzExMjcwMDAwMDBaMIGoMQswCQYDVQQGEwJERTEpMCcGA1UECgwgZ2VtYXRpayBNdXN0ZXJrYXNzZTFHS1ZOT1QtVkFMSUQxEjAQBgNVBAsMCTg5OTA3Nzg5MTETMBEGA1UECwwKWDc2NDIyODUzMjEOMAwGA1UEBAwFWmVicmExEjAQBgNVBCoMCVphY2hhcmlhczEhMB8GA1UEAwwYWmFjaGFyaWFzIFplYnJhVEVTVC1PTkxZMFowFAYHKoZIzj0CAQYJKyQDAwIIAQEHA0IABEo3u3g4C5NEQAbTSsgSVryCwnm6sovK3H3ZoBH7kd9LKUNSlWFlRpd2lsDI5CpFAphTVSIcDY8QB6V/2xztAiyjge8wgewwIAYDVR0gBBkwFzAKBggqghQATASBIzAJBgcqghQATARGMDAGBSskCAMDBCcwJTAjMCEwHzAdMBAMDlZlcnNpY2hlcnRlLy1yMAkGByqCFABMBDEwDgYDVR0PAQH/BAQDAgeAMDgGCCsGAQUFBwEBBCwwKjAoBggrBgEFBQcwAYYcaHR0cDovL2VoY2EuZ2VtYXRpay5kZS9vY3NwLzAdBgNVHQ4EFgQUHT0IU25tmCDOlfZaCfqbutn1jTwwDAYDVR0TAQH/BAIwADAfBgNVHSMEGDAWgBREsUwBWFQe3Nk3vuSyPypupFyJHTAKBggqhkjOPQQDAgNIADBFAiEAgu1f1Y/99fS02y4EVO33RAOvucgErlmpsJMPZkJdMLUCICHyjEb9KrbkdOfkNAABQvIsEw0LKpCaDis/1bvbuZgN"

private const val DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY =
    "Z4N8vzmPX9Ox8eNQVGz1C53WrMpHP5k/4o4+KSdVc1A="
const val SDKVERSION = "0.0.1"

class DeviceRegistrationJwt {
    companion object {
        private var isCryptoInitialized = false
        private var cryptoInitializedLock = Mutex()

        suspend fun buildAndSignJwt(jwtBody: String): String {
            return signJws(buildJws(jwtBody))
        }
        private suspend fun signJws(jws: JsonWebSignature): String {
            cryptoInitializedLock.withLock {
                if (!isCryptoInitialized) {
                    Security.removeProvider("BC")
                    Security.insertProviderAt(BCProvider, 1)
                    // add BP-256 curve to Bouncycastle
                    ExtentedEllipticCurves.init()
                    isCryptoInitialized = true
                }
            }
            val curveSpec = ECNamedCurveTable.getParameterSpec("brainpoolP256r1")
            val keySpec =
                org.bouncycastle.jce.spec.ECPrivateKeySpec(
                    BigInteger(
                        Base64.decode(
                            DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY,
                        ),
                    ),
                    curveSpec,
                )
            val privateKey = KeyFactory.getInstance("EC", BCProvider).generatePrivate(keySpec)
            jws.key = privateKey
            jws.sign()
            return jws.compactSerialization
        }
        private fun buildJws(body: String): JsonWebSignature {
            val jws = JsonWebSignature()
            jws.algorithmHeaderValue = "BP256R1"
            jws.setHeader("x5c", listOf(Base64.encode(Base64.decode(DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE))))
            jws.payload = body
            return jws
        }
        fun buildJwtBody(
            type: String = "android",
            nonce: ByteArray,
            iat: Date,
            packageName: String = "de.gematik.dsr.android",
            trustVersion: String = "TrustSDK_$SDKVERSION",
            subMTLS: ByteArray,
            subCertMTLS: Certificate,
            integrityVerdict: IntegrityTokenResponse,
            csr: CertificationRequest,
            attestPubKey: ByteArray,
            attestCert: Array<Certificate>,
        ): String {
            val base64Encoder = java.util.Base64.getEncoder()
            val pubKeySubject = SubjectPublicKeyInfo.getInstance(subMTLS)
            val pubKeyBytes = pubKeySubject.publicKeyData.bytes
            val pubKeyHash = MessageDigest.getInstance("SHA-256").digest(pubKeyBytes)
            val pubBase64 = base64Encoder.encodeToString(pubKeyHash)
            val nonceB64 = base64Encoder.encodeToString(nonce)
            val csrB64 = base64Encoder.encodeToString(csr.encoded)

            val pubKeySubjectAttest = SubjectPublicKeyInfo.getInstance(attestPubKey)
            val pubKeyBytesAttest = pubKeySubjectAttest.publicKeyData.bytes
            val pubKeyHashAttest = MessageDigest.getInstance("SHA-256").digest(pubKeyBytesAttest)
            val pubBase64Attest = base64Encoder.encodeToString(pubKeyHashAttest)

            val jwtClaims = JwtClaims()
            jwtClaims.setClaim("iss", trustVersion)
            jwtClaims.setClaim("sub", pubBase64)
            jwtClaims.setClaim("iat", iat.time / 1000) // no milliseconds
            jwtClaims.setClaim("type", type)
            jwtClaims.setClaim("nonce", nonceB64)
            jwtClaims.setClaim("csr", csrB64)
            jwtClaims.setClaim("subjectCert", base64Encoder.encodeToString(subCertMTLS.encoded))
            jwtClaims.setClaim("integrityVerdict", integrityVerdict.token())
            jwtClaims.setClaim("packageName", packageName)
            jwtClaims.setClaim("attestCertChain", attestCert.map { base64Encoder.encodeToString(it.encoded) })
            jwtClaims.setClaim("attestPublicKey", pubBase64Attest)
            return jwtClaims.toJson()
        }
    }
}
