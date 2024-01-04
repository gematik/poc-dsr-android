
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.8.22"
    id("org.jetbrains.compose")
    id("com.android.library")
}

group = "de.gematik.dsr"
version = "Version-1.1.1"

kotlin {
    android()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation("org.bouncycastle:bcprov-jdk18on:1.74")
                implementation("org.bouncycastle:bcpkix-jdk18on:1.74")
                implementation("androidx.biometric:biometric:1.1.0")
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.1")
                implementation("com.google.android.play:integrity:1.2.0")
                implementation("org.bitbucket.b_c:jose4j:0.9.3")
                implementation("com.squareup.retrofit2:retrofit:2.9.0")
                implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
                implementation("com.squareup.okhttp3:okhttp:4.10.0")
                implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
                implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
                implementation("org.kodein.di:kodein-di-framework-compose:7.16.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
            }
        }
    }
}

android {
    buildToolsVersion = "33.0.1"
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 26
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
