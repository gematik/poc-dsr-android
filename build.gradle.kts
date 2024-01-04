group = "de.gematik.dsr"
version = "Version-1.1.1"

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("android") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.compose") apply false
}

val ktlintMain by configurations.creating

dependencies {
    ktlintMain("com.pinterest:ktlint:0.49.1") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
        }
    }
}

val sourcesKt = listOf(
    "android/src/**/de/gematik/**/*.kt",
    "common/src/**/de/gematik/**/*.kt",
    "**/*.gradle.kts",
)

fun ktlintCreating(format: Boolean, sources: List<String>, disableLicenceRule: Boolean) =
    tasks.creating(JavaExec::class) {
        description = "Fix Kotlin code style deviations."
        classpath = ktlintMain
        mainClass.set("com.pinterest.ktlint.Main")
        args = mutableListOf<String>().apply {
            if (format) add("-F")
            addAll(sources)
            if (disableLicenceRule) add("--disabled_rules=custom:licence-header")
        }
        // required for java > 16; see https://github.com/pinterest/ktlint/issues/1195
        jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
    }

val ktlint by ktlintCreating(format = false, sources = sourcesKt, disableLicenceRule = false)
val ktlintFormat by ktlintCreating(format = true, sources = sourcesKt, disableLicenceRule = false)
