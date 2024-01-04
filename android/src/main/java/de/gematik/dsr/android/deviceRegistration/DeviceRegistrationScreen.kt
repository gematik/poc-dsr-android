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

package de.gematik.dsr.android.deviceRegistration

import AppTheme
import SpacerMedium
import SpacerSmall
import SpacerXLarge
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.dsr.android.DebugButton
import de.gematik.dsr.android.R
import de.gematik.dsr.android.components.AnimatedElevationScaffold
import de.gematik.dsr.android.components.BottomAppBar
import de.gematik.dsr.android.components.PhoneWithStateIcon
import de.gematik.dsr.android.components.PrimaryButton
import de.gematik.dsr.common.deviceRegistration.rememberDeviceRegistrationController
import de.gematik.dsr.common.utils.state.ScreenState
import de.gematik.dsr.common.utils.state.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * DeviceRegistrationScreen.kt
 *
 * This file contains the DeviceRegistrationScreen,
 * on which performs the deviceRegistration at the GMS and shows the user the state of this registration process.
 * If the phone is secure, the device will get registered and the user can continue.
 */

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Suppress("LongMethod")
@Composable
fun DeviceRegistrationScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    onChangeToken: (String) -> Unit,
    onOpenDebug: () -> Unit,
) {
    val deviceRegistrationController = rememberDeviceRegistrationController()
    val state by deviceRegistrationController.state
    val token by deviceRegistrationController.token
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        navigationMode = null,
        onBack = onBack,
        topBarTitle = "",
        listState = listState,
        bottomBar = {
            DeviceRegistrationScreenBottomBar(
                state = state,
                onStartRegistration = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            deviceRegistrationController.runDeviceRegistration()
                        }
                    }
                },
                onChangeToken = onChangeToken,
                token = token,
                onNext = onNext,
            )
        },
        actions = {
            DebugButton(onOpenDebug)
        },
    ) { innerPadding ->
        val contentPadding = remember {
            PaddingValues(
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium + innerPadding.calculateBottomPadding(),
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
            )
        }
        LazyColumn(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            state = listState,
        ) {
            item {
                DeviceRegistrationHeader(state)
                SpacerMedium()
            }
            item {
                DeviceRegistrationBody(state)
                SpacerSmall()
            }
            item {
                SpacerXLarge()
                PhoneWithStateIcon(state)
            }
        }
    }
}

@Composable
private fun DeviceRegistrationScreenBottomBar(
    state: UIState,
    onStartRegistration: () -> Unit,
    onChangeToken: (String) -> Unit,
    token: String,
    onNext: () -> Unit,
) {
    BottomAppBar {
        Column(
            Modifier
                .fillMaxWidth(),
        ) {
            when (state) {
                ScreenState.Neutral -> PrimaryButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = onStartRegistration,
                ) {
                    Icon(Icons.Outlined.Policy, null)
                    SpacerMedium()
                    Text(stringResource(R.string.deviceRegistration_button))
                }

                ScreenState.Success -> PrimaryButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        onChangeToken(token)
                        onNext()
                    },
                ) {
                    Text(stringResource(R.string.deviceRegistration_button_success))
                }

                ScreenState.Loading -> PrimaryButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = onNext,
                    enabled = false,
                ) {
                    Text(stringResource(R.string.deviceRegistration_button_success))
                }

                is ScreenState.Error -> PrimaryButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = onStartRegistration,
                ) {
                    Icon(Icons.Outlined.Refresh, null)
                    SpacerMedium()
                    Text(stringResource(R.string.deviceRegistration_button_error))
                }
            }
        }
    }
}

@Composable
private fun DeviceRegistrationHeader(state: UIState) {
    when (state) {
        ScreenState.Neutral -> Text(
            stringResource(R.string.deviceRegistration_header),
            style = AppTheme.typography.h5,
        )
        ScreenState.Success -> Text(
            stringResource(R.string.deviceRegistration_header_success),
            style = AppTheme.typography.h5,
        )
        ScreenState.Loading -> Text(
            stringResource(R.string.deviceRegistration_header_loading),
            style = AppTheme.typography.h5,
        )
        is ScreenState.Error -> Text(
            stringResource(R.string.deviceRegistration_header_error),
            style = AppTheme.typography.h5,
        )
    }
}

@Composable
private fun DeviceRegistrationBody(state: UIState) {
    when (state) {
        ScreenState.Neutral -> Text(
            stringResource(R.string.deviceRegistration_body),
            style = AppTheme.typography.body1,
        )
        ScreenState.Success -> Text(
            stringResource(R.string.deviceRegistration_body_success),
            style = AppTheme.typography.body1,
        )
        ScreenState.Loading -> Text(
            stringResource(R.string.deviceRegistration_body_loading),
            style = AppTheme.typography.body1,
        )
        is ScreenState.Error -> Text(
            state.message,
            style = AppTheme.typography.body1,
        )
    }
}
