import com.android.build.api.dsl.ApkSigningConfig
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.wifiwidget.application)
    alias(libs.plugins.wifiwidget.hilt)
    alias(libs.plugins.wifiwidget.hilt.work)
    alias(libs.plugins.triplet.play)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.kotlin.plugin.compose)
}

android {
    defaultConfig {
        applicationId = namespace
        versionCode = project.property("versionCode").toString().toInt()
        versionName = version.toString()
    }

    fun releaseSigningConfigOrNull(): ApkSigningConfig? {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            val keystoreProperties = Properties().apply { load(FileInputStream(keystorePropertiesFile)) }
            return signingConfigs.create("release") {
                storeFile = rootProject.file("keys.jks")
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
        logger.info("Couldn't create signing config; ${keystorePropertiesFile.path} does not exist")
        return null
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = releaseSigningConfigOrNull()
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        checkDependencies = true
    }
    dependenciesInfo {
        // Disable dependency metadata when building APKs for fdroid reproducibility
        includeInApk = false
    }
}

// https://github.com/Triple-T/gradle-play-publisher
play {
    serviceAccountCredentials.set(file("service-account-key.json"))
    defaultToAppBundles.set(true)
    artifactDir.set(file("build/outputs/bundle/release"))
}

dependencies {
    // Project modules
    implementation(projects.core.common)
    implementation(projects.core.resources)
    implementation(projects.ui)
    implementation(projects.core.widget)
    implementation(projects.core.domain)
    implementation(projects.core.datastore)
    implementation(projects.core.networking)
    baselineProfile(projects.benchmarking)

    implementation(libs.slimber)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.kotlinx.coroutines.core)

    // AndroidX libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.compose.activity)

    testImplementation(libs.bundles.unitTest)
}
