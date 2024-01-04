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

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Context.DEVICE_POLICY_SERVICE
import android.content.Context.KEYGUARD_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import kotlinx.serialization.Serializable
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*

/**
 * DeviceAttestation.kt
 *
 * This file contains DeviceAttestation data class and all the function to gather the needed information.
 * The DeviceAttestation data class is used while creating the DeviceAttestationJwt.
 * It contains all the needed information for the DeviceAttestation.
 */

@Serializable
data class DeviceAttestation(
    val iss: String,
    val sub: String,
    val iat: Long,
    val type: String,
    val nonce: String,
    val attestationCertChain: List<String>,
    val integrityVerdict: String,
    val packageName: String,
    val deviceAttributes: DeviceAttributes,
) {
    @Serializable
    data class DeviceAttributes(
        val build: BuildAttributes,
        val ro: ReadOnlyAttributes,
        val packageManager: PackageManagerAttributes,
        val keyguardManager: KeyguardManagerAttributes,
        val biometricManager: BiometricManagerAttributes,
        val devicePolicyManager: DevicePolicyManagerAttributes,
    )

    @Serializable
    data class BuildAttributes(
        val version: Version,
        val manufacturer: String,
        val product: String,
        val model: String,
        val board: String,
    )

    @Serializable
    data class Version(
        val sdkInit: Int,
        val securityPatch: String,
    )

    @Serializable
    data class ReadOnlyAttributes(
        val crypto: CryptoAttributes,
        val product: ProductAttributes,
    )

    @Serializable
    data class CryptoAttributes(
        val state: Boolean,
    )

    @Serializable
    data class ProductAttributes(
        val firstAPILevel: Int,
    )

    @Serializable
    data class PackageManagerAttributes(
        val featureVerifiedBoot: Boolean,
        val mainLinePatchLevel: String,
    )

    @Serializable
    data class KeyguardManagerAttributes(
        val isDeviceSecure: Boolean,
    )

    @Serializable
    data class BiometricManagerAttributes(
        val deviceCredential: Boolean,
        val biometricStrong: Boolean,
    )

    @Serializable
    data class DevicePolicyManagerAttributes(
        val passwordComplexity: Int,
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun getDeviceAttributes(
    context: Context,
): DeviceAttestation.DeviceAttributes {
    val currentSdkVersion = Build.VERSION.SDK_INT
    val version = DeviceAttestation.Version(
        sdkInit = currentSdkVersion,
        securityPatch = Build.VERSION.SECURITY_PATCH,
    )

    val buildAttributes = DeviceAttestation.BuildAttributes(
        version = version,
        manufacturer = Build.MANUFACTURER,
        product = Build.PRODUCT,
        model = Build.MODEL,
        board = Build.BOARD,
    )

    val packageManager = context.packageManager
    val packageInfo = packageManager.getPackageInfo("com.google.android.modulemetadata", PackageManager.PackageInfoFlags.of(0))
    val packageManagerAttributes = DeviceAttestation.PackageManagerAttributes(
        featureVerifiedBoot = packageManager.hasSystemFeature(PackageManager.FEATURE_VERIFIED_BOOT),
        mainLinePatchLevel = parseDateFromVersionName(packageInfo.versionName),
    )

    val keyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
    val keyguardManagerAttributes = DeviceAttestation.KeyguardManagerAttributes(
        isDeviceSecure = keyguardManager.isDeviceSecure,
    )

    val biometricManagerAttributes = if (currentSdkVersion <= Build.VERSION_CODES.R) {
        DeviceAttestation.BiometricManagerAttributes(
            deviceCredential = false,
            biometricStrong = false,
        )
    } else {
        val biometricManager = BiometricManager.from(context)
        DeviceAttestation.BiometricManagerAttributes(
            deviceCredential = biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS,
            biometricStrong = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS,
        )
    }

    val devicePolicyManager = context.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val devicePolicyManagerAttributes = DeviceAttestation.DevicePolicyManagerAttributes(passwordComplexity = devicePolicyManager.passwordComplexity)

    val deviceEncryption = devicePolicyManager.storageEncryptionStatus
    val cryptoAttributes = DeviceAttestation.CryptoAttributes(
        state = when (deviceEncryption) {
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE,
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING,
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER,
            -> true
            else -> false
        },
    )

    val sdkFactoryLevel = getSystemProperty("ro.product.first_api_level")
    val productAttributes = DeviceAttestation.ProductAttributes(
        firstAPILevel = sdkFactoryLevel?.toInt() ?: 0,
    )

    val readOnlyAttributes = DeviceAttestation.ReadOnlyAttributes(
        crypto = cryptoAttributes,
        product = productAttributes,
    )

    return DeviceAttestation.DeviceAttributes(
        build = buildAttributes,
        ro = readOnlyAttributes,
        packageManager = packageManagerAttributes,
        keyguardManager = keyguardManagerAttributes,
        biometricManager = biometricManagerAttributes,
        devicePolicyManager = devicePolicyManagerAttributes,
    )
}

@SuppressLint("PrivateApi")
fun getSystemProperty(property: String): String? {
    val getPropMethod: Method = Class.forName("android.os.SystemProperties").getMethod("get", String::class.java)
    return getPropMethod.invoke(null, property) as? String
}

private val VERSION_NAME_DATE_PATTERNS = listOf(
    "yyyy-MM-dd",
    "yyyy-MM",
)

private fun parseDateFromVersionName(text: String): String {
    var date = text
    for (pattern in VERSION_NAME_DATE_PATTERNS) {
        try {
            val simpleDateFormat = SimpleDateFormat(
                pattern,
                Locale.getDefault(),
            )
            simpleDateFormat.timeZone = TimeZone.getDefault()
            date = simpleDateFormat.parse(text)?.toString() ?: text
        } catch (e: Exception) {
            // TODO
        }
    }
    return date
}
