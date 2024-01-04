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

/**
 * FdRepository.kt
 *
 * This file contains the FdRepository,
 * which implements all the functions of the FdRemoteDataSource.
 */

class FdRepository(
    private val remoteDataSource: FdRemoteDataSource,
) {
    suspend fun getPrescriptions(
        token: String,
        context: Context,
    ): List<PrescriptionListObject> {
        return remoteDataSource.getPrescriptions(
            token = token,
            context,
        )
    }
}
