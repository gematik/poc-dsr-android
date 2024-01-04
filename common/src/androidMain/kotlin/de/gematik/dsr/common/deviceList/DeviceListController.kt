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

package de.gematik.dsr.common.deviceList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import de.gematik.dsr.common.utils.state.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.compose.rememberInstance

private const val userIdentifier = "X764228532"

/**
 * DeviceListController.kt
 *
 * This file contains the DeviceListController,
 * which behaves similar to a Viewmodel.
 * It aggregates all the functions related to the DeviceListUseCase needed in the UI .
 */

class DeviceListController(
    private val deviceListUseCase: DeviceListUseCase,
) {
    private var deviceListScreenStateFlow = MutableStateFlow<ScreenState>(ScreenState.Neutral)

    val state
        @Composable
        get() = deviceListScreenStateFlow.collectAsState()

    private var deviceListFlow = MutableStateFlow<List<DeviceListObject>>(emptyList())

    val deviceList
        @Composable
        get() = deviceListFlow.collectAsState()

    suspend fun getListedDevices(userId: String = userIdentifier) {
        deviceListScreenStateFlow.value = ScreenState.Loading
        runCatching { deviceListUseCase.getListedDevices(userId) }.fold(
            onSuccess = {
                deviceListFlow.value = it
                deviceListScreenStateFlow.value = ScreenState.Success
            },
            onFailure = {
                deviceListScreenStateFlow.value = ScreenState.Error(it.message.toString())
            },
        )
    }
    suspend fun deleteListedDevice(userId: String = userIdentifier, deviceId: String) {
        runCatching { deviceListUseCase.deleteListedDevice(userId, deviceId) }.fold(
            onSuccess = {
                val updatedList = deviceListFlow.value.toMutableList()
                updatedList.remove(
                    updatedList.find { device -> device.deviceIdentifier == deviceId },
                )
                deviceListFlow.emit(updatedList)
                deviceListScreenStateFlow.emit(ScreenState.Success)
            },
            onFailure = {
                deviceListScreenStateFlow.value = ScreenState.Error(it.message.toString())
            },
        )
    }
}

@Composable
fun rememberDeviceListController(): DeviceListController {
    val deviceListUseCase by rememberInstance<DeviceListUseCase>()

    return remember {
        DeviceListController(
            deviceListUseCase = deviceListUseCase,
        )
    }
}
