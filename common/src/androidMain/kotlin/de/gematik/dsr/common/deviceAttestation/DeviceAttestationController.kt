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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import de.gematik.dsr.common.utils.state.DeviceAttestationState
import de.gematik.dsr.common.utils.state.ScreenState
import de.gematik.dsr.common.utils.state.UIState
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.compose.rememberInstance

/**
 * DeviceAttestationController.kt
 *
 * This file contains the DeviceAttestationController,
 * which behaves similar to a Viewmodel.
 * It aggregates all the functions related to the DeviceAttestationUseCase needed in the UI .
 */

class DeviceAttestationController(
    private val deviceAttestationUseCase: DeviceAttestationUseCase,
) {
    private var deviceAttestationStateFlow = MutableStateFlow<UIState>(ScreenState.Neutral)

    val state
        @Composable
        get() = deviceAttestationStateFlow.collectAsState()

    private var deviceTokenFlow = MutableStateFlow("")

    val token
        @Composable
        get() = deviceTokenFlow.collectAsState()

    fun setStateToDeviceAttestationStateBiometryError() {
        deviceAttestationStateFlow.value = DeviceAttestationState.BiometryError
    }

    fun getDeviceAttestToken(): String {
        return deviceAttestationUseCase.mockDevAttestToken
    }
    fun getDeviceToken(): String {
        return deviceAttestationUseCase.mockToken
    }
    fun getCodeChallenge(): String {
        return deviceAttestationUseCase.mockChallenge
    }
    fun getCodeVerifier(): String {
        return deviceAttestationUseCase.mockVerifier
    }
    fun getAuthCode(): String {
        return deviceAttestationUseCase.mockAuthCode
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun runDeviceAttestation() {
        deviceAttestationStateFlow.value = ScreenState.Loading
        runCatching { deviceAttestationUseCase.runDeviceAttestation() }.fold(
            onSuccess = {
                deviceTokenFlow.value = it
                deviceAttestationStateFlow.value = ScreenState.Success
            },
            onFailure = {
                deviceAttestationStateFlow.value = ScreenState.Error(it.message.toString())
            },
        )
    }
}

@Composable
fun rememberDeviceAttestationController(): DeviceAttestationController {
    val deviceAttestationUseCase by rememberInstance<DeviceAttestationUseCase>()

    return remember {
        DeviceAttestationController(
            deviceAttestationUseCase = deviceAttestationUseCase,
        )
    }
}
