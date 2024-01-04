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

package de.gematik.dsr.common.deviceRegistration

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import de.gematik.dsr.common.csr.createCSR
import de.gematik.dsr.common.gms.GmsRepository
import de.gematik.dsr.common.jwt.DeviceRegistrationJwt
import de.gematik.dsr.common.keystore.ATTESTKEYPAIRALIAS
import de.gematik.dsr.common.keystore.MTLSKEYPAIRALIAS
import de.gematik.dsr.common.keystore.generateAttestKey
import de.gematik.dsr.common.keystore.generateKeypair
import de.gematik.dsr.common.keystore.getCertificate
import de.gematik.dsr.common.keystore.getCertificateChain
import de.gematik.dsr.common.keystore.saveToKeyStore
import de.gematik.dsr.common.playIntegrityApi.callIntegrityApi
import de.gematik.dsr.common.utils.generateNonce
import org.jose4j.base64url.Base64Url
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * DeviceRegistrationUseCase.kt
 *
 * This file contains the DeviceRegistrationUseCase,
 * which performs the deviceRegistration at the gms.
 */

class DeviceRegistrationUseCase(
    private val gmsRepository: GmsRepository,
    private val context: Context,
) {
    var mockDevRegToken = ""
    var mockNonce = ""
    var mockCert = ""
    var intNonce = ""

    @RequiresApi(Build.VERSION_CODES.S)
    suspend fun runDeviceRegistration() {
        // get the nonce from the gms via tls
        val gmsNonce = gmsRepository.getNonce()
        // derive new nonces out of the nonce sent by the gms
        val keypairMTLSNonce = generateNonce(gmsNonce, "KEYPAIR_MTLS".toByteArray())
        val csrMTLSNonce = generateNonce(gmsNonce, "CSR_MTLS".toByteArray())
        val integrityNonce = generateNonce(gmsNonce, "INTEGRITY".toByteArray())
        val smartcardNonce = generateNonce(gmsNonce, "SMARTCARD".toByteArray())
        val attestNonce = generateNonce(gmsNonce, "ATTEST".toByteArray())
        // get the current time
        val currentTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
        // generate Keys for attestation and mtls
        val attestKeyPair = generateAttestKey(attestNonce, currentTime, ATTESTKEYPAIRALIAS)
        val attestCert = getCertificateChain(ATTESTKEYPAIRALIAS)
        val mtlsKeyPair = generateKeypair(keypairMTLSNonce, currentTime, MTLSKEYPAIRALIAS, useAttestKey = true)
        val mtlsKeyPairCert = getCertificate(MTLSKEYPAIRALIAS)
        // create a certificate signing request
        val csr = createCSR(mtlsKeyPair, csrMTLSNonce)
        // get the integrityToken from the Google integrity api
        val integrityToken = callIntegrityApi(context, integrityNonce)
        // create the jwt body using the previous gathered information
        val unsignedJwtBody = DeviceRegistrationJwt.buildJwtBody(
            nonce = gmsNonce,
            iat = Date.from(currentTime.toInstant(ZoneOffset.UTC)),
            subMTLS = mtlsKeyPair.public.encoded,
            subCertMTLS = mtlsKeyPairCert,
            integrityVerdict = integrityToken,
            csr = csr,
            attestCert = attestCert,
            attestPubKey = attestKeyPair.public.encoded,
        )
        // build and sign the jwt using a virtual healthcard
        val signedJwt = DeviceRegistrationJwt.buildAndSignJwt(unsignedJwtBody)
        // register device the device at the gms via tls
        val mTLSCertificate = gmsRepository.postDeviceRegistration(token = signedJwt)
        // data used for debugging
        mockCert = Base64.getEncoder().encodeToString(mTLSCertificate)
        mockDevRegToken = signedJwt
        mockNonce = Base64.getEncoder().encodeToString(gmsNonce)
        intNonce = Base64Url.encode(integrityNonce)
        // store mtlsCertificate to communicate with the gms via mtls later
        saveToKeyStore(mTLSCertificate)
    }
}
