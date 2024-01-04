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

package de.gematik.dsr.common.csr

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.pkcs.CertificationRequest
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import java.security.KeyPair

/**
 * Csr.kt
 *
 * This file contains a function to create a Certificate Signing Request,
 * which is contains the commonName, the organizationName and the Country.
 */

fun createCSR(keyPair: KeyPair, csrMTLSNonce: ByteArray): CertificationRequest {
    val challengePasswordOid = ASN1ObjectIdentifier("1.2.840.113549.1.9.7")
    val x500NameBuilder = X500NameBuilder()
    x500NameBuilder.addRDN(challengePasswordOid, java.util.Base64.getEncoder().encodeToString(csrMTLSNonce))

    val csrBuilder: PKCS10CertificationRequestBuilder = JcaPKCS10CertificationRequestBuilder(
        x500NameBuilder.build(),
        keyPair.public,
    )

    val contentSigner: ContentSigner =
        JcaContentSignerBuilder("SHA256withECDSA")
            .build(keyPair.private)

    for (attribute in createAttributes()) {
        csrBuilder.addAttribute(attribute.first, attribute.second)
    }

    val csr: PKCS10CertificationRequest = csrBuilder.build(contentSigner)

    return csr.toASN1Structure()
}

private fun createAttributes(): List<Pair<ASN1ObjectIdentifier, ASN1Encodable>> {
    val attributes = mutableListOf<Pair<ASN1ObjectIdentifier, ASN1Encodable>>()

    // Add the subject information
    val subjectInfo = ASN1EncodableVector()
    subjectInfo.add(DERPrintableString("DE"))
    attributes.add(Pair(ASN1ObjectIdentifier("2.5.4.6"), DERSequence(subjectInfo)))

    // Add the organization information
    val organizationInfo = ASN1EncodableVector()
    organizationInfo.add(DERUTF8String("DSR_POC")) // OrganizationName value
    attributes.add(Pair(ASN1ObjectIdentifier("2.5.4.10"), DERSequence(organizationInfo)))

    // Add the common name information
    val commonNameInfo = ASN1EncodableVector()
    commonNameInfo.add(DERUTF8String("TRUST_CLIENT")) // CommonName value
    attributes.add(Pair(ASN1ObjectIdentifier("2.5.4.3"), DERSequence(commonNameInfo)))

    return attributes
}
