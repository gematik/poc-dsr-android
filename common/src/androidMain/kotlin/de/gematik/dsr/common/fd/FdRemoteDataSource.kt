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

package de.gematik.dsr.common.fd

import android.content.Context
import de.gematik.dsr.common.prescription.PrescriptionListObject
import de.gematik.dsr.common.utils.okHttpClientFactory.OkHttpClientFactory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject

/**
 * FdRemoteDataSource.kt
 *
 * This file contains the FdRemoteDataSource,
 * which transforms the prescriptions sent by the fsService to a List.
 */

class FdRemoteDataSource() {
    private val fdMtlsClient = FdMtlsClient(OkHttpClientFactory.createBaseClient())
    suspend fun getPrescriptions(
        token: String,
        context: Context,
    ): List<PrescriptionListObject> {
        fdMtlsClient.switchToMtlsIfNecessarry(context)
        val response = fdMtlsClient.fdMtlsService.getPrescriptions(
            deviceToken = token,
        )
        val responseBody = response.body().let { it!!.string() }
        return if (response.isSuccessful) {
            Json.decodeFromString<List<PrescriptionListObject>>(responseBody)
        } else {
            val responseJson = JSONObject(responseBody)
            val errorCode = responseJson.getString("errorCode")
            val errorMessage = responseJson.getString("description")
            throw FdException(errorMessage, errorCode)
        }
    }
}

class FdException(message: String, errorCode: String) : Exception("Error: $message, ErrorCode: $errorCode")
