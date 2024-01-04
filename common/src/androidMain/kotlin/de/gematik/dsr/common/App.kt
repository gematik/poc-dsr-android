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

package de.gematik.dsr.common

import android.app.Application
import de.gematik.dsr.common.deviceAttestation.DeviceAttestationUseCase
import de.gematik.dsr.common.deviceList.DeviceListUseCase
import de.gematik.dsr.common.deviceRegistration.DeviceRegistrationUseCase
import de.gematik.dsr.common.fd.FdRemoteDataSource
import de.gematik.dsr.common.fd.FdRepository
import de.gematik.dsr.common.fd.FdUseCase
import de.gematik.dsr.common.gms.GmsRemoteDataSource
import de.gematik.dsr.common.gms.GmsRepository
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bindProvider
import org.kodein.di.instance

/**
 * App.kt
 *
 * This file contains the Kodein Dependencies for this App.
 */

class App : Application(), DIAware {
    override val di by DI.lazy {
        import(androidXModule(this@App))
        importAll(appModul)
    }

    private val appModul = DI.Module("appModule") {
        bindProvider { DeviceRegistrationUseCase(instance(), instance()) }
        bindProvider { DeviceAttestationUseCase(instance(), instance()) }
        bindProvider { DeviceListUseCase(instance(), instance()) }
        bindProvider { FdUseCase(instance(), instance()) }
        bindProvider { GmsRepository(instance()) }
        bindProvider { GmsRemoteDataSource() }
        bindProvider { FdRepository(instance()) }
        bindProvider { FdRemoteDataSource() }
    }
}
