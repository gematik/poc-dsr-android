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

import SpacerMedium
import SpacerSmall
import SpacerTiny
import SpacerXXLarge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.dsr.android.DebugButton
import de.gematik.dsr.android.R
import de.gematik.dsr.android.components.AnimatedElevationScaffold
import de.gematik.dsr.android.components.DeviceRegistrationBottomBar
import de.gematik.dsr.android.components.NavigationBarMode

/**
 * DeviceRegistrationStartScreen.kt
 *
 * This file contains the DeviceRegistrationStartScreen,
 * which displays some information about prerequisites to use this app to the user.
 * They are not actually needed to use this mock app.
 */

@Composable
fun DeviceRegistrationStartScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    onOpenDebug: () -> Unit,
) {
    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        modifier = Modifier,
        topBarTitle = "",
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        actions = {
            DebugButton(onOpenDebug)
        },
        bottomBar = {
            DeviceRegistrationBottomBar(
                onNext = onNext,
                nextEnabled = true,
                aligment = Alignment.CenterHorizontally,
            )
        },
        onBack = onBack,
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
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = contentPadding,
        ) {
            item {
                HealthCardPhoneImage()
                SpacerXXLarge()
            }
            item {
                Text(
                    stringResource(R.string.deviceRegistrationStartHeader),
                    style = AppTheme.typography.h5,
                )
                SpacerMedium()
            }
            item {
                Text(
                    stringResource(R.string.deviceRegistrationStartBody1),
                    style = AppTheme.typography.body1,
                )
                SpacerSmall()
            }
            item {
                Text(
                    stringResource(R.string.deviceRegistrationStartBody2),
                    style = AppTheme.typography.body2,
                )
                SpacerTiny()
            }
            item {
                Text(
                    stringResource(R.string.deviceRegistrationStartBody3),
                    style = AppTheme.typography.body2,
                )
                SpacerTiny()
            }
            item {
                Text(
                    stringResource(R.string.deviceRegistrationStartBody4),
                    style = AppTheme.typography.body2,
                )
                SpacerTiny()
            }
            item {
                Text(
                    stringResource(R.string.deviceRegistrationStartBody5),
                    style = AppTheme.typography.body2,
                )
                SpacerTiny()
            }
        }
    }
}
