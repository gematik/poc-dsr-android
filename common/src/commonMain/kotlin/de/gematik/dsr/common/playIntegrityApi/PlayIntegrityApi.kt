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

package de.gematik.dsr.common.playIntegrityApi

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityToken
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import kotlinx.coroutines.tasks.await
import org.jose4j.base64url.Base64Url
import java.util.*

/**
 * PlayIntegrityApi.kt
 *
 * This file contains the functions to access the Google PlayIntegrityApi.
 */

suspend fun callIntegrityApi(context: Context, integrityNonce: ByteArray): IntegrityTokenResponse {
    val integrityManager =
        IntegrityManagerFactory.create(context)
    return integrityManager.requestIntegrityToken(
        IntegrityTokenRequest.builder()
            .setCloudProjectNumber(CLOUD_PROJECT_NUMBER)
            .setNonce(
                Base64Url.encode(integrityNonce),
            )
            .build(),
    ).await()
}

suspend fun callStandardIntegrityApi(context: Context, integrityNonce: ByteArray): StandardIntegrityToken {
    val standardIntegrityManager = IntegrityManagerFactory.createStandard(context)
    val base64Encoder = Base64.getUrlEncoder()
    val integrityTokenProvider = standardIntegrityManager.prepareIntegrityToken(
        PrepareIntegrityTokenRequest.builder()
            .setCloudProjectNumber(CLOUD_PROJECT_NUMBER)
            .build(),
    ).await()
    return integrityTokenProvider.request(
        StandardIntegrityTokenRequest.builder()
            .setRequestHash(
                base64Encoder.encodeToString(integrityNonce),
            )
            .build(),
    ).await()
}
