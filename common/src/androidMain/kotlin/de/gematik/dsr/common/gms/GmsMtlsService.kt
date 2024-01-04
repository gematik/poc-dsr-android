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

import okhttp3.FormBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * GmsMtlsService.kt
 *
 * This file contains the GmsMtlsService,
 * which creates a Retrofit Interface, which is specifies the Rest endpoints used by the GmsMtlsClient.
 */
interface GmsMtlsService {
    @GET("device-registrations")
    @Headers(
        "Accept: application/json",
        "X-Authorization: $AUTHORIZATIONTOKEN",
    )
    suspend fun getDeviceRegistration(
        @Query("userIdentifier") userIdentifier: String,
    ): Response<ResponseBody>

    @DELETE("device-registrations")
    @Headers(
        "Accept: application/json",
        "X-Authorization: $AUTHORIZATIONTOKEN",
    )
    suspend fun deleteDeviceRegistration(
        @Query("userIdentifier") userIdentifier: String,
        @Query("deviceIdentifier") deviceIdentifier: String,
    ): Response<ResponseBody>

    @POST("device-attestation")
    @Headers(
        "Accept: text/plain",
        "Content-Type: application/json",
        "X-Authorization: $AUTHORIZATIONTOKEN",
    )
    suspend fun postDeviceAttestation(
        @Body body: String,
    ): Response<String>

    @POST("device-token")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/x-www-form-urlencoded",
        "X-Authorization: $AUTHORIZATIONTOKEN",
    )
    suspend fun postDeviceToken(
        @Body body: FormBody,
    ): Response<ResponseBody>
}
