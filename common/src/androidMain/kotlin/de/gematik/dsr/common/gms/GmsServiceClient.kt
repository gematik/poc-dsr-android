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
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import de.gematik.dsr.common.utils.okHttpClientFactory.OkHttpClientFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * GmsServiceClient.kt
 *
 * This file contains the GmsBase- and the GmsMtlsClient.
 * Both are creating a Retrofit Client, which is used to communicate with the gms.
 * The GmsMtlsClient can and will be switched to use mtls when the needed mtls certificate is saved on the device.
 */

private const val GMS_URL = "https://dsr.gms.dev.gematik.solutions"
private const val GMS_MTLS_URL = "https://dsr.gms-mtls.dev.gematik.solutions"

data class GmsMtlsClient(
    var okHttpClient: OkHttpClient,
) {
    var gmsMtlsService: GmsMtlsService
    var mtlsActive: Boolean
    init {
        gmsMtlsService = createGmsMtlsService(okHttpClient)
        mtlsActive = false
    }
    fun createGmsMtlsService(okHttpClient: OkHttpClient): GmsMtlsService {
        val clientBuilder = okHttpClient.newBuilder()
        val client = clientBuilder
            .addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY),
            )
            .followRedirects(false)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(GMS_MTLS_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(jsonConverterFactory)
            .build()
            .create(GmsMtlsService::class.java)
    }
    fun switchToMtlsIfNecessarry(context: Context) {
        if (!mtlsActive) {
            okHttpClient = OkHttpClientFactory.createMTLSClient(context).also {
                gmsMtlsService = createGmsMtlsService(it)
                mtlsActive = true
            }
        }
    }
}

data class GmsBaseClient(
    var okHttpClient: OkHttpClient,
) {
    var gmsService: GmsService
    init {
        gmsService = createGmsService(okHttpClient)
    }

    fun createGmsService(okHttpClient: OkHttpClient): GmsService {
        val clientBuilder = okHttpClient.newBuilder()
        val client = clientBuilder
            .addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY),
            )
            .followRedirects(false)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(GMS_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(jsonConverterFactory)
            .build()
            .create(GmsService::class.java)
    }
}

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@OptIn(ExperimentalSerializationApi::class)
private val jsonConverterFactory = json.asConverterFactory("application/json".toMediaType())
