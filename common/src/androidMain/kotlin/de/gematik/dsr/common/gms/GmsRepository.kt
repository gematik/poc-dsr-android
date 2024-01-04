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

package de.gematik.dsr.common.gms

import android.content.Context
import de.gematik.dsr.common.deviceList.DeviceListObject

/**
 * GmsRepository.kt
 *
 * This file contains the GmsRepository,
 * which implements all the functions of the GmsRemoteDataSource.
 */
class GmsRepository(
    private val remoteDataSource: GmsRemoteDataSource,
) {
    suspend fun getNonce(): ByteArray {
        return remoteDataSource.getNonce()
    }

    suspend fun postDeviceRegistration(
        token: String,
    ): ByteArray {
        return remoteDataSource.postDeviceRegistration(
            token = token,
        )
    }

    suspend fun getDeviceRegistration(
        userIdentifier: String,
        context: Context,
    ): List<DeviceListObject> {
        return remoteDataSource.getDeviceRegistration(
            userIdentifier = userIdentifier,
            context,
        )
    }

    suspend fun deleteDeviceRegistration(
        userIdentifier: String,
        deviceIdentifier: String,
        context: Context,
    ) {
        remoteDataSource.deleteDeviceRegistration(
            userIdentifier = userIdentifier,
            deviceIdentifier = deviceIdentifier,
            context,
        )
    }

    suspend fun postDeviceAttestation(
        token: String,
        codeChallenge: String,
        context: Context,
    ): String {
        return remoteDataSource.postDeviceAttestation(
            token = token,
            codeChallenge = codeChallenge,
            context,
        )
    }

    suspend fun postDeviceToken(
        code: String,
        codeVerifier: String,
        context: Context,
    ): String {
        return remoteDataSource.postDeviceToken(
            code = code,
            codeVerifier = codeVerifier,
            context,
        )
    }
}
