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

import de.gematik.dsr.android.components.Route

/**
 * DeviceRegistrationNavigation.kt
 *
 * This file contains navigation routes for deviceRegistration.
 */

object DeviceRegistrationNavigation {
    object Intro : Route("DeviceRegistrationIntro")
    object Start : Route("DeviceRegistrationStart")
    object Can : Route("DeviceRegistrationCan")
    object Pin : Route("DeviceRegistrationPin")
    object Nfc : Route("DeviceRegistrationNfc")
    object Registration : Route("DeviceRegistration")
    object Debug : Route("Debug")
}
