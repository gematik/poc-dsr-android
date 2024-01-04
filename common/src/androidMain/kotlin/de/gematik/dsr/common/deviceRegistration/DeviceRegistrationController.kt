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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import de.gematik.dsr.common.deviceAttestation.DeviceAttestationUseCase
import de.gematik.dsr.common.utils.state.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.compose.rememberInstance

/**
 * DeviceRegistrationController.kt
 *
 * This file contains the DeviceRegistrationController,
 * which behaves similar to a Viewmodel.
 * It aggregates all the functions related to the DeviceRegistrationUseCase needed in the UI .
 */

class DeviceRegistrationController(
    private val deviceRegistrationUseCase: DeviceRegistrationUseCase,
    private val deviceAttestationUseCase: DeviceAttestationUseCase,
) {
    private var deviceRegistrationStateFlow = MutableStateFlow<ScreenState>(ScreenState.Neutral)

    val state
        @Composable
        get() = deviceRegistrationStateFlow.collectAsState()

    private var deviceTokenFlow = MutableStateFlow("")

    val token
        @Composable
        get() = deviceTokenFlow.collectAsState()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun runDeviceRegistration() {
        deviceRegistrationStateFlow.value = ScreenState.Loading
        runCatching { deviceRegistrationUseCase.runDeviceRegistration() }.fold(
            onSuccess = {
                runCatching { deviceAttestationUseCase.runDeviceAttestation() }.fold(
                    onSuccess = {
                        deviceTokenFlow.value = it
                        deviceRegistrationStateFlow.value = ScreenState.Success
                    },
                    onFailure = {
                        deviceRegistrationStateFlow.value = ScreenState.Error(it.message.toString())
                    },
                )
            },
            onFailure = {
                deviceRegistrationStateFlow.value = ScreenState.Error(it.message.toString())
            },
        )
    }

    fun getDeviceRegistrationToken(): String {
        return deviceRegistrationUseCase.mockDevRegToken
    }

    fun getNonce(): String {
        return deviceRegistrationUseCase.mockNonce
    }

    fun getIntNonce(): String {
        return deviceRegistrationUseCase.intNonce
    }

    fun getCert(): String {
        return deviceRegistrationUseCase.mockCert
    }
}

@Composable
fun rememberDeviceRegistrationController(): DeviceRegistrationController {
    val deviceRegistrationUseCase by rememberInstance<DeviceRegistrationUseCase>()
    val deviceAttestationUseCase by rememberInstance<DeviceAttestationUseCase>()

    return remember {
        DeviceRegistrationController(
            deviceRegistrationUseCase = deviceRegistrationUseCase,
            deviceAttestationUseCase = deviceAttestationUseCase,
        )
    }
}
