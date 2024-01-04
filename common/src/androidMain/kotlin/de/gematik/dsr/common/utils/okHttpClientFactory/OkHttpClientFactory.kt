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

package de.gematik.dsr.common.utils.okHttpClientFactory

import android.content.Context
import de.gematik.dsr.common.R
import de.gematik.dsr.common.keystore.MTLSKEYPAIRALIAS
import de.gematik.dsr.common.keystore.getCertificate
import de.gematik.dsr.common.keystore.getKeyStore
import de.gematik.dsr.common.keystore.getPrivateKey
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * OkHttpClientFactory.kt
 *
 * This file contains the OkHttpClientFactory,
 * which contains two functions. The first one creates a okHttpClient to communicate in tls.
 * The second one creates a okHttpClient to communicate in mtls.
 */

private const val HTTP_CONNECTION_TIMEOUT = 10000L
private const val HTTP_READ_TIMEOUT = 10000L
private const val HTTP_WRITE_TIMEOUT = 10000L
private const val GMSCERTCHAINALIAS = "GMSCERTNR_"

object OkHttpClientFactory {

    fun createBaseClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(
                timeout = HTTP_CONNECTION_TIMEOUT,
                unit = TimeUnit.MILLISECONDS,
            )
            .readTimeout(
                timeout = HTTP_READ_TIMEOUT,
                unit = TimeUnit.MILLISECONDS,
            )
            .writeTimeout(
                timeout = HTTP_WRITE_TIMEOUT,
                unit = TimeUnit.MILLISECONDS,
            )
            .connectionSpecs(getConnectionSpec())
            .build()
    }

    fun createMTLSClient(context: Context): OkHttpClient {
        val sslContext = SSLContext.getInstance("TLS")

        // Create KeyStore for client certificate and private key
        val clientKeyStore = getKeyStore()
        clientKeyStore.load(null)
        val clientCert = getCertificate()
        val privateKey = getPrivateKey()

        clientKeyStore.setKeyEntry(MTLSKEYPAIRALIAS, privateKey, null, arrayOf(clientCert))

        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(clientKeyStore, null)

        val trustStore = getKeyStore()
        trustStore.load(null)
        // manually add the gms_cert_chain to the trustStore
        val rawCertificate = context.resources.openRawResource(R.raw.gms_cert_chain)
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificates = certificateFactory.generateCertificates(rawCertificate)
        for ((certificateIndex, certificate) in certificates.withIndex()) {
            if (!trustStore.containsAlias("$GMSCERTCHAINALIAS$certificateIndex")) {
                trustStore.setCertificateEntry("$GMSCERTCHAINALIAS$certificateIndex", certificate)
            }
        }
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(trustStore)
        val trustManager = trustManagerFactory.trustManagers

        sslContext.init(keyManagerFactory.keyManagers, trustManager, null)

        return OkHttpClient.Builder()
            .connectTimeout(
                timeout = HTTP_CONNECTION_TIMEOUT,
                unit = TimeUnit.MILLISECONDS,
            )
            .readTimeout(
                timeout = HTTP_READ_TIMEOUT,
                unit = TimeUnit.MILLISECONDS,
            )
            .writeTimeout(
                timeout = HTTP_WRITE_TIMEOUT,
                unit = TimeUnit.MILLISECONDS,
            )
            .connectionSpecs(getConnectionSpec())
            .sslSocketFactory(sslContext.socketFactory, trustManager[0] as X509TrustManager)
            .build()
    }
}

private fun getConnectionSpec(): List<ConnectionSpec> = ConnectionSpec
    .Builder(ConnectionSpec.RESTRICTED_TLS)
    .tlsVersions(
        TlsVersion.TLS_1_3,
    )
    .cipherSuites(
        CipherSuite.TLS_AES_128_GCM_SHA256,
        CipherSuite.TLS_AES_256_GCM_SHA384,
        CipherSuite.TLS_CHACHA20_POLY1305_SHA256,
    )
    .build()
    .let {
        listOf(it)
    }
