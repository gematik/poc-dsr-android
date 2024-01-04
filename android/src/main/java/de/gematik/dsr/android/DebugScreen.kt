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

package de.gematik.dsr.android

import SpacerMedium
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.gematik.dsr.android.components.AnimatedElevationScaffold
import de.gematik.dsr.android.components.BottomAppBar
import de.gematik.dsr.android.components.PrimaryButton
import de.gematik.dsr.common.deviceAttestation.rememberDeviceAttestationController
import de.gematik.dsr.common.deviceRegistration.rememberDeviceRegistrationController
import kotlinx.coroutines.launch
import java.io.File

/**
 * DebugScreen.kt
 *
 * This file contains the DebugScreen,
 * on which the user can perform the registration and attestation.
 * Additionally, he can save the gathered information in his download folder
 * or restart the app with deviceRegistration.
 */

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DebugScreen(
    onRestartWithRegistration: () -> Unit,
    onBack: () -> Unit,
) {
    val deviceRegistrationController = rememberDeviceRegistrationController()
    val deviceAttestationController = rememberDeviceAttestationController()
    val scope = rememberCoroutineScope()
    var jwtRegTokenString by remember { mutableStateOf("") }
    var jwtAttTokenString by remember { mutableStateOf("") }
    var nonce by remember { mutableStateOf("") }
    var intNonce by remember { mutableStateOf("") }
    var codeVerifier by remember { mutableStateOf("") }
    var codeChallenge by remember { mutableStateOf("") }
    var authCode by remember { mutableStateOf("") }
    var deviceToken by remember { mutableStateOf("") }
    var cert by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        modifier = Modifier,
        topBarTitle = "",
        listState = listState,
        actions = {},
        bottomBar = {
            DebugBottomBar(onRestartWithRegistration)
        },
        onBack = onBack,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            userScrollEnabled = true,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PrimaryButton(onClick = {
                        scope.launch {
                            deviceRegistrationController.runDeviceRegistration().also {
                                jwtRegTokenString = deviceRegistrationController.getDeviceRegistrationToken()
                                nonce = deviceRegistrationController.getNonce()
                                intNonce = deviceRegistrationController.getIntNonce()
                                cert = deviceRegistrationController.getCert()
                            }
                        }
                    }) {
                        Text("runRegistration")
                    }
                    Text(
                        if (cert.isNotEmpty()) {
                            "Registration finshed"
                        } else {
                            "Registration not finshed"
                        },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PrimaryButton(onClick = {
                        val downloadsDirectory =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        File(downloadsDirectory, "devRegJwtTokenAndroid.txt").writeText(jwtRegTokenString)
                    }) {
                        Text("writeToFile")
                    }
                    Text(
                        if (jwtRegTokenString.isNotEmpty()) {
                            "RegToken ready"
                        } else {
                            "No RegToken"
                        },
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PrimaryButton(onClick = {
                        scope.launch {
                            deviceAttestationController.runDeviceAttestation().also {
                                jwtAttTokenString = deviceAttestationController.getDeviceAttestToken()
                                codeVerifier = deviceAttestationController.getCodeVerifier()
                                codeChallenge = deviceAttestationController.getCodeChallenge()
                                authCode = deviceAttestationController.getAuthCode()
                                deviceToken = deviceAttestationController.getDeviceToken()
                            }
                        }
                    }) {
                        Text("runAttestation")
                    }
                    Text(
                        if (deviceToken.isNotEmpty()) {
                            "Attestation finished"
                        } else {
                            "Attestation not finished"
                        },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PrimaryButton(onClick = {
                        val downloadsDirectory =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        File(downloadsDirectory, "devAttestJwtTokenAndroid.txt").writeText(jwtAttTokenString)
                    }) {
                        Text("writeToFile")
                    }
                    Text(
                        if (jwtAttTokenString.isNotEmpty()) {
                            "AttToken ready"
                        } else {
                            "no AttToken"
                        },
                    )
                }
            }
            item {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val downloadsDirectory =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val dataString = buildString {
                            append("nonce =\n$nonce\n\n")
                            append("intNonce =\n$intNonce\n\n")
                            append("jwtRegTokenString =\n$jwtRegTokenString\n\n")
                            append("mTLSCert =\n$cert\n\n")
                            append("jwtAttTokenString =\n$jwtAttTokenString\n\n")
                            append("codeVerifier =\n$codeVerifier\n\n")
                            append("codeChallenge =\n$codeChallenge\n\n")
                            append("authCode =\n$authCode\n\n")
                            append("deviceToken =\n$deviceToken\n")
                        }
                        File(downloadsDirectory, "AndroidData.md").writeText(dataString)
                    },
                ) {
                    Text("WriteAllDataToFile")
                }
            }
        }
    }
}

@Composable
fun DebugButton(
    onOpenDebug: () -> Unit,
) {
    PrimaryButton(
        onClick = onOpenDebug,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.red500,
            contentColor = AppTheme.colors.neutral000,
        ),
    ) {
        Icon(Icons.Rounded.BugReport, null)
        SpacerMedium()
        Text(stringResource(R.string.debug))
    }
}

@Composable
fun DebugBottomBar(
    onRestartWithRegistration: () -> Unit,
) {
    BottomAppBar {
        Column(
            Modifier
                .fillMaxWidth(),
        ) {
            PrimaryButton(
                onClick = onRestartWithRegistration,
                modifier = Modifier
                    .padding(
                        horizontal = PaddingDefaults.Medium,
                        vertical = PaddingDefaults.ShortMedium,
                    )
                    .align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppTheme.colors.red500,
                    contentColor = AppTheme.colors.neutral000,
                ),
            ) {
                Icon(Icons.Outlined.RestartAlt, null)
                SpacerMedium()
                Text(
                    stringResource(R.string.debug_restart),
                )
            }
        }
    }
}
