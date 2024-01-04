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

import org.jose4j.jwa.AlgorithmFactoryFactory
import org.jose4j.jws.EcdsaUsingShaAlgorithm
import org.jose4j.jws.JsonWebSignatureAlgorithm
import org.jose4j.keys.EllipticCurves
import java.math.BigInteger
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.EllipticCurve

/**
 * ExtendedEllipticCurves.kt
 *
 * This file contains the ExtendedEllipticCurves,
 * which is used to add the BP-256 Curve to BouncyCastle.
 */

object ExtentedEllipticCurves : EllipticCurves() {
    const val BP_256 = "BP-256"
    val BP256 = ECParameterSpec(
        EllipticCurve(
            ECFieldFp(BigInteger("76884956397045344220809746629001649093037950200943055203735601445031516197751")),
            BigInteger("56698187605326110043627228396178346077120614539475214109386828188763884139993"),
            BigInteger("17577232497321838841075697789794520262950426058923084567046852300633325438902"),
        ),
        ECPoint(
            BigInteger("63243729749562333355292243550312970334778175571054726587095381623627144114786"),
            BigInteger("38218615093753523893122277964030810387585405539772602581557831887485717997975"),
        ),
        BigInteger("76884956397045344220809746629001649092737531784414529538755519063063536359079"),
        1,
    )
    private var initializedInSession = false

    fun init(): Boolean = if (initializedInSession) {
        true
    } else {
        try {
            addCurve("BP-256", BP256)
            AlgorithmFactoryFactory.getInstance().jwsAlgorithmFactory.registerAlgorithm(
                EcdsaUsingShaAlgorithmExtending.EcdsaBP256R1UsingSha256(),
            )
            initializedInSession = true
            true
        } catch (e: Exception) {
            throw IllegalStateException("failure on init $e")
        }
    }
}
open class EcdsaUsingShaAlgorithmExtending(
    id: String?,
    javaAlgo: String?,
    curveName: String?,
    signatureByteLength: Int,
) : EcdsaUsingShaAlgorithm(id, javaAlgo, curveName, signatureByteLength),
    JsonWebSignatureAlgorithm {
    class EcdsaBP256R1UsingSha256 : EcdsaUsingShaAlgorithmExtending(
        AlgorithmIdentifiersExtending.BRAINPOOL256_USING_SHA256,
        "SHA256withECDSA",
        ExtentedEllipticCurves.BP_256,
        64,
    )
}
object AlgorithmIdentifiersExtending {
    const val BRAINPOOL256_USING_SHA256 = "BP256R1"
}
