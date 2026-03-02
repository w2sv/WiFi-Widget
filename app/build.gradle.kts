import com.android.build.api.dsl.ApkSigningConfig
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.wifiwidget.application)
    alias(libs.plugins.wifiwidget.hilt)
    alias(libs.plugins.play)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
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
        logger.warn("Couldn't create signing config; ${keystorePropertiesFile.path} does not exist")
        return null
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = releaseSigningConfigOrNull()
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        checkDependencies = true
        xmlReport = false
        htmlReport = true
        textReport = false
        htmlOutput = project.layout.buildDirectory.file("reports/lint-results-debug.html").get().asFile
    }
    dependenciesInfo {
        // Disable dependency metadata when building APKs for fdroid reproducibility
        includeInApk = false
    }
}

kotlin {
    compilerOptions {
        optIn.addAll(
            "com.google.accompanist.permissions.ExperimentalPermissionsApi",
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "androidx.compose.foundation.layout.ExperimentalLayoutApi"
        )
    }
}

// https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-compiler.html#compose-compiler-options-dsl
composeCompiler {
    includeSourceInformation = true
    stabilityConfigurationFiles.add(project.layout.projectDirectory.file("compose_compiler_config.conf"))
    metricsDestination.set(project.layout.buildDirectory.dir("compose_compiler"))
    reportsDestination.set(project.layout.buildDirectory.dir("compose_compiler"))
}

// https://github.com/Triple-T/gradle-play-publisher
play {
    serviceAccountCredentials.set(file("service-account-key.json"))
    defaultToAppBundles.set(true)
    artifactDir.set(file("build/outputs/bundle/release"))
}

dependencies {
    // Project modules
    implementation(projects.core.widget)
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.datastore)
    implementation(projects.core.networking)
    baselineProfile(projects.benchmarking)

    // Owned libraries
    implementation(libs.w2sv.androidutils)
    implementation(libs.w2sv.colorpicker)
    implementation(libs.w2sv.composed.core)
    implementation(libs.w2sv.composed.material3)
    implementation(libs.w2sv.composed.permissions)
    implementation(libs.w2sv.composeWheelPicker)
    implementation(libs.w2sv.reversiblestate)

    // AndroidX libraries
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.workmanager)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3.android)

    // Compose libraries
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.profileinstaller)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.viewmodel)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.google.accompanist.permissions)
    implementation(libs.androidx.hilt.navigation.compose)

    // Other libraries
    lintChecks(libs.compose.lint.checks)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.reorderable)

    testImplementation(libs.bundles.unitTest)
    androidTestImplementation(libs.bundles.androidTest)
}
