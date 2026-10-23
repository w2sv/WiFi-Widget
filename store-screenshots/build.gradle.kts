import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.android.test)
    alias(libs.plugins.wifiwidget.hilt)
}

android {
    namespace = "com.w2sv.wifiwidget.storescreenshots"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 35
        targetSdk = libs.versions.compileSdk.get().toInt()
        testInstrumentationRunner = "com.w2sv.wifiwidget.storescreenshots.ScreenshotTestRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // These device names are also referenced by screenshot-tasks.gradle.kts when it wires
    // capture, validation, and Play listing copy tasks for each display category.
    @Suppress("UnstableApiUsage")
    testOptions.managedDevices.allDevices {
        create<ManagedVirtualDevice>("storePhoneApi35") {
            device = "Pixel 6"
            apiLevel = 35
            systemImageSource = "aosp"
            testedAbi = "x86_64"
        }
        create<ManagedVirtualDevice>("storeTabletApi35") {
            device = "Nexus 7"
            apiLevel = 35
            systemImageSource = "aosp"
            testedAbi = "x86_64"
        }
        create<ManagedVirtualDevice>("storeLargeTabletApi35") {
            device = "Pixel Tablet"
            apiLevel = 35
            systemImageSource = "aosp"
            testedAbi = "x86_64"
        }
    }

    // Isolate screenshot tests in separate instrumentation processes to prevent
    // their DataStore instances from sharing in-memory state across test classes.
    // Persisted data remains shared and is initialized explicitly by each test.
    testOptions.execution = "ANDROIDX_TEST_ORCHESTRATOR"

    // com.android.test runs the real app as its target rather than packaging a test-only UI.
    targetProjectPath = ":app"
}

dependencies {
    implementation(projects.testing.launcher)
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.datastore)
    implementation(projects.core.networking)
    implementation(projects.core.widget)
    implementation(projects.ui)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.compose.ui.test.junit4.android)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.google.dagger.hilt.android.testing)
    androidTestUtil(libs.androidx.test.orchestrator)
}

// Gradle's plugins block loads precompiled scripts from build-logic, not a module-local file.
// Apply this module-local script after declaring the managed devices it references.
apply(from = "screenshot-tasks.gradle.kts")
