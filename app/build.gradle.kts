import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.play)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.wifiwidget.hilt)
    alias(libs.plugins.baselineprofile)
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

android {
    val packageName = "com.w2sv.wifiwidget"

    namespace = packageName
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = packageName
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()

        versionCode = project.findProperty("versionCode")!!.toString().toInt()
        versionName = version.toString()

        // Name built bundles "{versionName}-{buildFlavor}.aab"
        setProperty("archivesBaseName", versionName)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            rootProject.file("keystore.properties").let { file ->
                if (file.exists()) {
                    val keystoreProperties = Properties()
                    keystoreProperties.load(FileInputStream(file))

                    storeFile = rootProject.file("keys.jks")
                    storePassword = keystoreProperties["storePassword"] as String
                    keyAlias = keystoreProperties["keyAlias"] as String
                    keyPassword = keystoreProperties["keyPassword"] as String
                }
            }
        }
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
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }

    hilt {
        enableAggregatingTask = true  // Fixes warning
    }

    // Name built apks "{versionName}.apk"
    applicationVariants.all {
        outputs
            .forEach { output ->
                (output as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                    "${versionName}.apk"
            }
    }

    kotlinOptions {
        freeCompilerArgs += listOf(
            // Apply compose_compiler_config.conf
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=${project.rootDir.absolutePath}/compose_compiler_config.conf",
            // Enable strong skipping
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:experimentalStrongSkipping=true"
        )
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
    implementation(projects.core.widget)
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.datastore)
    implementation(projects.core.networking)
    baselineProfile(projects.benchmarking)

    // Owned libraries
    implementation(libs.kotlinutils)
    implementation(libs.androidutils)
    implementation(libs.colorpicker)
    implementation(libs.composed)
    implementation(libs.composed.permissions)

    // AndroidX libraries
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.localbroadcastmanager)
    implementation(libs.androidx.workmanager)

    // Compose libraries
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.profileinstaller)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.viewmodel)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.google.accompanist.permissions)
    implementation(libs.composeWheelPicker)

    // Other libraries
    implementation(libs.slimber)
    lintChecks(libs.compose.lint.checks)
    implementation(libs.kotlinx.collections.immutable)
}