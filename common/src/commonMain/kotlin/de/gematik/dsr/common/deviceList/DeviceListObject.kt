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

import kotlinx.serialization.Serializable

/**
 * DeviceListObject.kt
 *
 * This file contains DeviceListObject data class.
 * The DeviceListObject data class is used while parsing the response from the gms getDeviceList request.
 */

@Serializable
data class DeviceListObject(
    val userIdentifier: String,
    val deviceIdentifier: String,
    val deviceType: String,
    val createdAt: String,
)
