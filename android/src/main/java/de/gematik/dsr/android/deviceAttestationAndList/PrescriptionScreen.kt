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

package de.gematik.dsr.android.deviceAttestationAndList

import AppTheme
import PaddingDefaults
import SpacerMedium
import SpacerTiny
import SpacerXXLarge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.dsr.android.DebugButton
import de.gematik.dsr.android.R
import de.gematik.dsr.android.components.AnimatedElevationScaffold
import de.gematik.dsr.android.components.BottomAppBar
import de.gematik.dsr.android.components.NavigationBarMode
import de.gematik.dsr.android.components.PrimaryButton
import de.gematik.dsr.common.fd.rememberFdController
import de.gematik.dsr.common.prescription.PrescriptionListObject
import de.gematik.dsr.common.utils.state.ScreenState
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * PrescriptionScreen.kt
 *
 * This file contains the PrescriptionScreen,
 * which shows you a number of mock prescriptions after you successfully finished the deviceAttestation.
 */
@Composable
fun PrescriptionScreen(
    token: String,
    onOpenDevices: () -> Unit,
    onOpenDebug: () -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val prescriptionController = rememberFdController()
    val state by prescriptionController.state
    val prescriptions by prescriptionController.prescriptions

    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.prescription_screen_title),
        navigationMode = NavigationBarMode.Devices,
        listState = listState,
        onBack = onOpenDevices,
        bottomBar = {
            PrescriptionScreenBottomBar(
                state,
                onLoadDeviceList = {
                    scope.launch { prescriptionController.getPrescriptions(token) }
                },
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
        LaunchedEffect(scope) {
            prescriptionController.getPrescriptions(token)
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = contentPadding,
        ) {
            when (state) {
                ScreenState.Loading -> item {
                    PrescriptionScreenLoading(Modifier.fillParentMaxSize())
                }
                ScreenState.Neutral -> item {}
                is ScreenState.Error -> item {
                    EmptyScreenFailure(
                        modifier = Modifier.fillParentMaxSize(),
                        description = (state as ScreenState.Error).message,
                    )
                }
                ScreenState.Success -> {
                    if (prescriptions.isEmpty()) {
                        item { EmptyScreenNoPrescriptions(Modifier.fillParentMaxSize()) }
                    } else {
                        prescriptions.forEach {
                            item {
                                PrescriptionItem(
                                    prescription = it,
                                )
                                Divider(modifier = Modifier.padding(start = PaddingDefaults.Medium))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrescriptionScreenLoading(modifier: Modifier) {
    EmptyScreen(modifier) {
        CircularProgressIndicator(Modifier.size(40.dp))
        SpacerMedium()
        Text(
            stringResource(R.string.prescription_screen_loading),
            style = AppTheme.typography.body1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmptyScreenNoPrescriptions(modifier: Modifier) {
    EmptyScreen(modifier) {
        Image(
            painterResource(R.drawable.girl_red_oh_no),
            contentDescription = null,
        )
        SpacerXXLarge()
        Text(
            stringResource(R.string.prescription_screen_empty_title),
            style = AppTheme.typography.h5,
            textAlign = TextAlign.Center,
        )
        SpacerMedium()
        Text(
            stringResource(R.string.prescription_screen_empty_body),
            style = AppTheme.typography.body1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmptyScreenFailure(
    modifier: Modifier,
    description: String,
) {
    EmptyScreen(modifier) {
        Image(
            painterResource(R.drawable.girl_red_oh_no),
            contentDescription = null,
        )
        SpacerXXLarge()
        Text(
            stringResource(R.string.prescription_screen_error_title),
            style = AppTheme.typography.h5,
            textAlign = TextAlign.Center,
        )
        SpacerMedium()
        Text(
            description,
            style = AppTheme.typography.body1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmptyScreen(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier) {
        Column(
            modifier = Modifier
                .align(BiasAlignment(0f, -0.33f))
                .padding(PaddingDefaults.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        ) {
            content()
        }
    }
}

@Composable
private fun PrescriptionItem(
    prescription: PrescriptionListObject,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Medication, null)
        SpacerMedium()
        Column(
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium,
            ).weight(1f),

        ) {
            Text(
                prescription.prescription.medication,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.subtitle1,
            )
            SpacerTiny()
            Text(
                stringResource(R.string.issuedAt) + Instant.parse(prescription.issuedAt).atZone(ZoneId.of("UTC")).format(
                    DateTimeFormatter.ofPattern(" HH:mm dd.MM.yyyy"),
                ),
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.body2l,
            )
            SpacerTiny()
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    prescription.prescription.packSize,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.body2l,
                )
                SpacerTiny()
                Text(
                    prescription.prescription.dosageInstruction,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.body2l,
                )
            }
        }
    }
}

@Composable
private fun PrescriptionScreenBottomBar(
    state: ScreenState,
    onLoadDeviceList: () -> Unit,
) {
    BottomAppBar {
        Column(
            Modifier
                .fillMaxWidth(),
        ) {
            when (state) {
                ScreenState.Neutral -> PrimaryButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = onLoadDeviceList,
                ) {
                    Icon(Icons.Rounded.Search, null)
                    SpacerMedium()
                    Text(stringResource(R.string.prescription_screen_load))
                }

                ScreenState.Success -> PrimaryButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = onLoadDeviceList,
                ) {
                    Text(stringResource(R.string.prescription_screen_retry))
                }

                ScreenState.Loading -> PrimaryButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = onLoadDeviceList,
                    enabled = false,
                ) {
                    Icon(Icons.Rounded.Search, null)
                    SpacerMedium()
                    Text(stringResource(R.string.prescription_screen_load))
                }

                is ScreenState.Error -> PrimaryButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = onLoadDeviceList,
                ) {
                    Icon(Icons.Rounded.Refresh, null)
                    SpacerMedium()
                    Text(stringResource(R.string.prescription_screen_retry))
                }
            }
        }
    }
}
