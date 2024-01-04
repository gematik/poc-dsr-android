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

package de.gematik.dsr.common.utils.state

import androidx.compose.runtime.Stable

/**
 * ScreenState.kt
 *
 * This file contains the ScreenState-Interface,
 * which is used in the UI to show the matching screen for the specific state.
 */

interface UIState

@Stable
sealed interface ScreenState : UIState {
    @Stable
    object Loading : ScreenState

    @Stable
    object Success : ScreenState

    @Stable
    object Neutral : ScreenState

    @Stable
    data class Error(val message: String) : ScreenState
}

@Stable
sealed interface DeviceAttestationState : UIState {
    @Stable
    object BiometryError : DeviceAttestationState
}
