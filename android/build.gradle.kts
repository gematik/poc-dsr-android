import java.util.Properties

private val compose_version = "1.5.0-beta02"

plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
}

group = "de.gematik.dsr"
version = "Version-1.1.0"

tasks.named("preBuild") {
    dependsOn(":ktlint")
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("org.kodein.di:kodein-di-framework-compose:7.16.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
    implementation("androidx.compose.material:material-icons-core:$compose_version")
    implementation("androidx.compose.material:material-icons-extended:$compose_version")

    implementation("androidx.navigation:navigation-compose:2.6.0")

    implementation("com.airbnb.android:lottie-compose:6.1.0")

    implementation("androidx.biometric:biometric:1.2.0-alpha05")
}

android {
    buildToolsVersion = "33.0.1"
    compileSdk = 33

    defaultConfig {
        configurations.all {
            resolutionStrategy {
                force("androidx.emoji2:emoji2-views-helper:1.3.0")
                force("androidx.emoji2:emoji2:1.3.0")
            }
        }
        applicationId = "de.gematik.dsr.android"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "Version-1.1.1"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    val signingPropsFile = project.rootProject.file("signing.properties")
    if (signingPropsFile.canRead()) {
        println("Signing properties found: $signingPropsFile")
        val signingProps = Properties()
        signingProps.load(signingPropsFile.inputStream())
        signingConfigs {
            fun creatingRelease() = creating {
                val target = this.name // property name; e.g. googleRelease
                println("Create signing config for: $target")
                storeFile = signingProps["$target.storePath"]?.let { file(it) }
                println("\tstore: ${signingProps["$target.storePath"]}")
                keyAlias = signingProps["$target.keyAlias"] as? String
                println("\tkeyAlias: ${signingProps["$target.keyAlias"]}")
                storePassword = signingProps["$target.storePassword"] as? String
                keyPassword = signingProps["$target.keyPassword"] as? String
            }
            if (signingProps["googleRelease.storePath"] != null) {
                val googleRelease by creatingRelease()
            }
        }
    } else {
        println("No signing properties found!")
    }

    buildTypes {
        val release by getting {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingPropsFile.canRead()) {
                signingConfig = signingConfigs.getByName("googleRelease")
            }
            resValue("string", "app_label", "DSR")
        }
        val debug by getting {
            applicationIdSuffix = ""
            resValue("string", "app_label", "DSR-Test")
            versionNameSuffix = "-debug"
            signingConfigs {
                getByName("debug") {
                    storeFile = file("$rootDir/keystore/debug.keystore")
                    keyAlias = "androiddebugkey"
                    storePassword = "android"
                    keyPassword = "android"
                }
            }
        }
    }
}
