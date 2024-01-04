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
import de.gematik.dsr.common.utils.okHttpClientFactory.OkHttpClientFactory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import org.bouncycastle.util.encoders.Base64
import org.jose4j.base64url.Base64Url
import org.json.JSONObject

/**
 * GmsRemoteDataSource.kt
 *
 * This file contains the GmsRemoteDataSource,
 * which implements all the transforming functions of responses by both of the GmsServices.
 */

class GmsRemoteDataSource() {
    private val gmsBaseClient = GmsBaseClient(OkHttpClientFactory.createBaseClient())
    private val gmsMtlsClient = GmsMtlsClient(OkHttpClientFactory.createBaseClient())

    suspend fun getNonce(): ByteArray {
        val response = gmsBaseClient.gmsService.getNonce()
        if (response.isSuccessful) {
            return Base64Url.decode(response.body())
        } else {
            throw GMSException(response.message(), response.code().toString())
        }
    }

    suspend fun postDeviceRegistration(
        token: String,
    ): ByteArray {
        val tokenJson = JSONObject()
        tokenJson.put("token", token)
        val response = gmsBaseClient.gmsService.postDeviceRegistration(
            token = tokenJson.toString(),
        )
        val responseBody = response.body().let { it!!.string() }
        val responseJson = JSONObject(responseBody)
        return if (response.isSuccessful) {
            val certValue = responseJson.getString("cert")
            Base64.decode(certValue)!!
        } else {
            val errorCode = responseJson.getString("errorCode")
            val errorMessage = responseJson.getString("description")
            throw GMSException(errorMessage, errorCode)
        }
    }

    suspend fun getDeviceRegistration(
        userIdentifier: String,
        context: Context,
    ): List<DeviceListObject> {
        gmsMtlsClient.switchToMtlsIfNecessarry(context)
        val response = gmsMtlsClient.gmsMtlsService.getDeviceRegistration(
            userIdentifier = userIdentifier,
        )
        val responseBody = response.body().let { it!!.string() }
        return if (response.isSuccessful) {
            Json.decodeFromString<List<DeviceListObject>>(responseBody)
        } else {
            val responseJson = JSONObject(responseBody)
            val errorCode = responseJson.getString("errorCode")
            val errorMessage = responseJson.getString("description")
            throw GMSException(errorMessage, errorCode)
        }
    }

    suspend fun deleteDeviceRegistration(
        userIdentifier: String,
        deviceIdentifier: String,
        context: Context,
    ) {
        gmsMtlsClient.switchToMtlsIfNecessarry(context)
        val response = gmsMtlsClient.gmsMtlsService.deleteDeviceRegistration(
            userIdentifier = userIdentifier,
            deviceIdentifier = deviceIdentifier,
        )
        if (!response.isSuccessful) {
            if (response.code() == 400) {
                throw GMSException(response.message(), response.code().toString())
            }
            val responseBody = response.body().let { it!!.string() }
            val responseJson = JSONObject(responseBody)
            val errorCode = responseJson.getString("errorCode")
            val errorMessage = responseJson.getString("description")
            throw GMSException(errorMessage, errorCode)
        }
    }

    suspend fun postDeviceAttestation(
        token: String,
        codeChallenge: String,
        context: Context,
    ): String {
        val bodyJson = JSONObject()
        bodyJson.put("token", token)
        bodyJson.put("codeChallenge", codeChallenge)
        gmsMtlsClient.switchToMtlsIfNecessarry(context)
        val response = gmsMtlsClient.gmsMtlsService.postDeviceAttestation(
            body = bodyJson.toString(),
        )
        if (response.isSuccessful) {
            return response.body().let { it.toString() }
        } else {
            throw GMSException(response.message(), response.code().toString())
        }
    }

    suspend fun postDeviceToken(
        code: String,
        codeVerifier: String,
        context: Context,
    ): String {
        val formBodyBuilder = FormBody.Builder()
        formBodyBuilder.add("code", code)
        formBodyBuilder.add("code_verifier", codeVerifier)
        val bodyUrl = formBodyBuilder.build()
        gmsMtlsClient.switchToMtlsIfNecessarry(context)
        val response = gmsMtlsClient.gmsMtlsService.postDeviceToken(
            body = bodyUrl,
        )

        return when (response.code()) {
            200 -> {
                val responseBody = response.body().let { it!!.string() }
                val responseJson = JSONObject(responseBody)
                responseJson.getString("token")
            }
            202 -> ""
            else -> {
                val responseBody = response.body().let { it!!.string() }
                val responseJson = JSONObject(responseBody)
                val errorCode = responseJson.getString("errorCode")
                val errorMessage = responseJson.getString("description")
                throw GMSException(errorMessage, errorCode)
            }
        }
    }
}

class GMSException(message: String, errorCode: String) : Exception("Error: $message, ErrorCode: $errorCode")
