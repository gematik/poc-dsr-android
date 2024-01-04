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
 * FdClient.kt
 *
 * This file contains the FdClient,
 * which creates a Retrofit Client, which is used to communicate with the fd.
 * The fd returns demo prescriptions.
 * The fdClient can and will be switched to use mtls when the needed mtls certificate is saved on the device.
 */

private const val FD_MTLS_URL = "https://dsr.fd.dev.gematik.solutions"

data class FdMtlsClient(
    var okHttpClient: OkHttpClient,
) {
    var fdMtlsService: FdMtlsService
    var mtlsActive: Boolean
    init {
        fdMtlsService = createFdMtlsService(okHttpClient)
        mtlsActive = false
    }
    fun createFdMtlsService(okHttpClient: OkHttpClient): FdMtlsService {
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
            .baseUrl(FD_MTLS_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(jsonConverterFactory)
            .build()
            .create(FdMtlsService::class.java)
    }
    fun switchToMtlsIfNecessarry(context: Context) {
        if (!mtlsActive) {
            okHttpClient = OkHttpClientFactory.createMTLSClient(context).also {
                fdMtlsService = createFdMtlsService(it)
                mtlsActive = true
            }
        }
    }
}

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@OptIn(ExperimentalSerializationApi::class)
private val jsonConverterFactory = json.asConverterFactory("application/json".toMediaType())
