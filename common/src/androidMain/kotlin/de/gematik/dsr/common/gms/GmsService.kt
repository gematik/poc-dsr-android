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

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * GmsService.kt
 *
 * This file contains the GmsService,
 * which creates a Retrofit Interface, which is specifies the Rest endpoints used by the GmsBaseClient.
 */

const val AUTHORIZATIONTOKEN = "FsMxoUGiJZowZ99lg7AfFYZl9/oEZ8jpMvCuMDhbAKE="

interface GmsService {
    @GET("nonce")
    @Headers(
        "Accept: text/plain",
        "X-Authorization: $AUTHORIZATIONTOKEN",
    )
    suspend fun getNonce(): Response<String>

    @POST("register-device")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json",
        "X-Authorization: $AUTHORIZATIONTOKEN",
    )
    suspend fun postDeviceRegistration(
        @Body token: String,
    ): Response<ResponseBody>
}
