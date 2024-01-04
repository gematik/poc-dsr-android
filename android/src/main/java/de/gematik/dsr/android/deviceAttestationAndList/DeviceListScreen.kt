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
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import de.gematik.dsr.common.deviceList.DeviceListObject
import de.gematik.dsr.common.deviceList.rememberDeviceListController
import de.gematik.dsr.common.utils.state.ScreenState
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * DeviceListScreen.kt
 *
 * This file contains the DeviceListScreen, which shows all the devices which are registered for this healthcard at the gms.
 * You may also delete registrations using this screen.
 */

@Composable
fun DeviceListScreen(
    onBack: () -> Unit,
    onOpenDebug: () -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val deviceListController = rememberDeviceListController()
    val state by deviceListController.state
    val deviceList by deviceListController.deviceList
    var showDialog by remember { mutableStateOf(false) }
    var deviceToDelete by remember { mutableStateOf(DeviceListObject("", "", "", "")) }

    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.device_list_title),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        onBack = onBack,
        bottomBar = {
            PrescriptionScreenBottomBar(
                state,
                onLoadDeviceList = {
                    scope.launch { deviceListController.getListedDevices() }
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
            deviceListController.getListedDevices()
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
                    if (deviceList.isEmpty()) {
                        item { EmptyScreenNoPrescriptions(Modifier.fillParentMaxSize()) }
                    } else {
                        deviceList.forEach {
                            item {
                                Device(
                                    device = it,
                                    onDeleteDevice = {
                                        deviceToDelete = it
                                        showDialog = true
                                    },
                                )
                                Divider(modifier = Modifier.padding(start = PaddingDefaults.Medium))
                            }
                        }
                    }
                }
            }
        }
    }
    if (showDialog) {
        DeleteDialog(
            onConfirmation = {
                scope.launch { deviceListController.deleteListedDevice(deviceId = deviceToDelete.deviceIdentifier) }
                showDialog = false
            },
            onDismissRequest = {
                showDialog = false
            },
        )
    }
}

@Composable
private fun PrescriptionScreenLoading(modifier: Modifier) {
    EmptyScreen(modifier) {
        CircularProgressIndicator(Modifier.size(40.dp))
        SpacerMedium()
        Text(
            stringResource(R.string.device_list_loading),
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
            stringResource(R.string.device_list_empty_title),
            style = AppTheme.typography.h5,
            textAlign = TextAlign.Center,
        )
        SpacerMedium()
        Text(
            stringResource(R.string.device_list_empty_body),
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
            stringResource(R.string.device_list_error_title),
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
private fun Device(
    device: DeviceListObject,
    onDeleteDevice: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Smartphone, null)
        SpacerMedium()
        Column(
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium,
            ).weight(1f),

        ) {
            Text(
                device.deviceIdentifier,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.subtitle1,
            )
            SpacerTiny()
            Text(
                stringResource(R.string.createdAt) + Instant.parse(device.createdAt).atZone(ZoneId.of("UTC")).format(
                    DateTimeFormatter.ofPattern(" HH:mm dd.MM.yyyy"),
                ),
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.body2l,
            )
            SpacerTiny()
            Text(
                device.deviceType,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.body2l,
            )
        }
        Row(modifier = Modifier.padding(horizontal = PaddingDefaults.Medium), horizontalArrangement = Arrangement.End) {
            IconButton(
                onClick = onDeleteDevice,
            ) {
                Icon(Icons.Outlined.Delete, null, tint = AppTheme.colors.red500)
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
                    Text(stringResource(R.string.device_list_load))
                }

                ScreenState.Success -> PrimaryButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = onLoadDeviceList,
                ) {
                    Text(stringResource(R.string.device_list_retry))
                }

                ScreenState.Loading -> PrimaryButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = onLoadDeviceList,
                    enabled = false,
                ) {
                    Icon(Icons.Rounded.Search, null)
                    SpacerMedium()
                    Text(stringResource(R.string.device_list_load))
                }

                is ScreenState.Error -> PrimaryButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = onLoadDeviceList,
                ) {
                    Icon(Icons.Rounded.Refresh, null)
                    SpacerMedium()
                    Text(stringResource(R.string.device_list_retry))
                }
            }
        }
    }
}

@Composable
fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.fillMaxWidth(0.8f),
        title = {
            Text(text = stringResource(R.string.delete_dialog_title), style = AppTheme.typography.h6)
        },
        text = {
            Text(text = stringResource(R.string.delete_dialog_body), style = AppTheme.typography.body2)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                },
            ) {
                Text(stringResource(R.string.delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
            ) {
                Text(stringResource(R.string.delete_dialog_dismiss))
            }
        },
    )
}
