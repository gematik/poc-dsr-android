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

package de.gematik.dsr.common.fd

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import de.gematik.dsr.common.prescription.PrescriptionListObject
import de.gematik.dsr.common.utils.state.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.compose.rememberInstance

/**
 * FdController.kt
 *
 * This file contains the FdController,
 * which behaves similar to a Viewmodel.
 * It aggregates all the functions related to the FdUseCase needed in the UI .
 */

class FdController(
    private val fdUseCase: FdUseCase,
) {
    private var prescriptionScreenStateFlow = MutableStateFlow<ScreenState>(ScreenState.Neutral)

    val state
        @Composable
        get() = prescriptionScreenStateFlow.collectAsState()

    private var prescriptionsFlow = MutableStateFlow<List<PrescriptionListObject>>(emptyList())

    val prescriptions
        @Composable
        get() = prescriptionsFlow.collectAsState()

    suspend fun getPrescriptions(token: String) {
        prescriptionScreenStateFlow.value = ScreenState.Loading
        runCatching { fdUseCase.getPrescriptions(token) }.fold(
            onSuccess = {
                prescriptionsFlow.value = it
                prescriptionScreenStateFlow.value = ScreenState.Success
            },
            onFailure = {
                prescriptionScreenStateFlow.value = ScreenState.Error(it.message.toString())
            },
        )
    }
}

@Composable
fun rememberFdController(): FdController {
    val fdUseCase by rememberInstance<FdUseCase>()

    return remember {
        FdController(
            fdUseCase = fdUseCase,
        )
    }
}
