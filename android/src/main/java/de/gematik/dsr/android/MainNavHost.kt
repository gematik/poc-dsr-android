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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.gematik.dsr.android.components.NavigationAnimation
import de.gematik.dsr.android.components.navigationModeState
import de.gematik.dsr.android.deviceAttestationAndList.DeviceAttestationAndListNavigation
import de.gematik.dsr.android.deviceAttestationAndList.DeviceListScreen
import de.gematik.dsr.android.deviceAttestationAndList.PrescriptionScreen
import de.gematik.dsr.android.deviceAttestationAndList.UserAuthenticationScreen
import de.gematik.dsr.android.deviceRegistration.DeviceRegistrationCanScreen
import de.gematik.dsr.android.deviceRegistration.DeviceRegistrationIntroScreen
import de.gematik.dsr.android.deviceRegistration.DeviceRegistrationNavigation
import de.gematik.dsr.android.deviceRegistration.DeviceRegistrationNfcScreen
import de.gematik.dsr.android.deviceRegistration.DeviceRegistrationPinScreen
import de.gematik.dsr.android.deviceRegistration.DeviceRegistrationScreen
import de.gematik.dsr.android.deviceRegistration.DeviceRegistrationStartScreen
import de.gematik.dsr.common.keystore.deleteAllKeys
import de.gematik.dsr.common.keystore.deviceRegistrationCompleted

/**
 * MainNavHost.kt
 *
 * This file contains MainNavHost of the App.
 * Combining the Routes of DeviceAttestationAndList- and DeviceRegistrationNavigation.
 */

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainNavHost(
    navController: NavHostController,
) {
    val startDestination = if (deviceRegistrationCompleted()) {
        DeviceAttestationAndListNavigation.UserAuthentication.path()
    } else {
        DeviceRegistrationNavigation.Intro.path()
    }

    val navigationMode by navController.navigationModeState(
        startDestination = startDestination,
    )

    val onBack: () -> Unit = {
        if (navController.currentDestination?.route != startDestination) {
            navController.popBackStack()
        }
    }

    val onOpenDebug: () -> Unit = {
        navController.navigate(DeviceRegistrationNavigation.Debug.path())
    }

    var cardAccessNumber
        by rememberSaveable { mutableStateOf("") }
    var personalIdentificationNumber
        by rememberSaveable { mutableStateOf("") }
    var deviceToken
        by rememberSaveable { mutableStateOf("") }

    NavHost(
        navController,
        startDestination = startDestination,
    ) {
        composable(DeviceRegistrationNavigation.Intro.route) {
            NavigationAnimation(mode = navigationMode) {
                DeviceRegistrationIntroScreen(
                    onNext = { navController.navigate(DeviceRegistrationNavigation.Start.path()) },
                    onOpenDebug = onOpenDebug,
                )
            }
        }

        composable(DeviceRegistrationNavigation.Start.route) {
            NavigationAnimation(mode = navigationMode) {
                DeviceRegistrationStartScreen(
                    onNext = { navController.navigate(DeviceRegistrationNavigation.Can.path()) },
                    onBack = onBack,
                    onOpenDebug = onOpenDebug,
                )
            }
        }

        composable(DeviceRegistrationNavigation.Can.route) {
            NavigationAnimation(mode = navigationMode) {
                DeviceRegistrationCanScreen(
                    can = cardAccessNumber,
                    onCanChange = { cardAccessNumber = it },
                    onNext = {
                        navController.navigate(DeviceRegistrationNavigation.Pin.path())
                    },
                    onBack = onBack,
                    onOpenDebug = onOpenDebug,
                )
            }
        }

        composable(DeviceRegistrationNavigation.Pin.route) {
            NavigationAnimation(mode = navigationMode) {
                DeviceRegistrationPinScreen(
                    pin = personalIdentificationNumber,
                    onPinChange = { personalIdentificationNumber = it },
                    onBack = { onBack() },
                    onNext = { navController.navigate(DeviceRegistrationNavigation.Nfc.path()) },
                    onOpenDebug = onOpenDebug,
                )
            }
        }

        composable(DeviceRegistrationNavigation.Nfc.route) {
            NavigationAnimation(mode = navigationMode) {
                DeviceRegistrationNfcScreen(
                    onNext = { navController.navigate(DeviceRegistrationNavigation.Registration.path()) },
                    onBack = onBack,
                    onOpenDebug = onOpenDebug,
                )
            }
        }

        composable(DeviceRegistrationNavigation.Registration.route) {
            NavigationAnimation(mode = navigationMode) {
                DeviceRegistrationScreen(
                    onNext = { navController.navigate(DeviceAttestationAndListNavigation.Prescription.path()) },
                    onBack = onBack,
                    onChangeToken = { deviceToken = it },
                    onOpenDebug = onOpenDebug,
                )
            }
        }

        composable(DeviceRegistrationNavigation.Debug.route) {
            NavigationAnimation(mode = navigationMode) {
                DebugScreen(
                    onBack = onBack,
                    onRestartWithRegistration = {
                        deleteAllKeys()
                        navController.navigate(DeviceRegistrationNavigation.Intro.path())
                    },
                )
            }
        }

        composable(DeviceAttestationAndListNavigation.UserAuthentication.route) {
            NavigationAnimation(mode = navigationMode) {
                UserAuthenticationScreen(
                    onNext = { navController.navigate(DeviceAttestationAndListNavigation.Prescription.path()) },
                    onChangeToken = { deviceToken = it },
                    onOpenDebug = onOpenDebug,
                )
            }
        }

        composable(DeviceAttestationAndListNavigation.DeviceList.route) {
            NavigationAnimation(mode = navigationMode) {
                DeviceListScreen(
                    onBack = onBack,
                    onOpenDebug = onOpenDebug,
                )
            }
        }

        composable(DeviceAttestationAndListNavigation.Prescription.route) {
            NavigationAnimation(mode = navigationMode) {
                PrescriptionScreen(
                    token = deviceToken,
                    onOpenDevices = { navController.navigate(DeviceAttestationAndListNavigation.DeviceList.path()) },
                    onOpenDebug = onOpenDebug,
                )
            }
        }
    }
}
