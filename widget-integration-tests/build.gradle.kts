import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.wifiwidget.android.sdk)
    alias(libs.plugins.wifiwidget.code.quality)
    alias(libs.plugins.wifiwidget.hilt)
}

android {
    namespace = "com.w2sv.wifiwidget.integration"

    defaultConfig {
        minSdk = 35
        testInstrumentationRunner = "com.w2sv.wifiwidget.test.runner.hilt.HiltTestApplicationRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions.managedDevices.allDevices {
        @Suppress("UnstableApiUsage")
        create<ManagedVirtualDevice>("widgetPixel6Api35") {
            device = "Pixel 6"
            apiLevel = 35
            systemImageSource = "aosp"
            testedAbi = "x86_64"
        }
    }

    testOptions.execution = "ANDROIDX_TEST_ORCHESTRATOR"
    targetProjectPath = ":app"
}

dependencies {
    implementation(projects.testing.launcher)
    implementation(projects.core.domain)
    implementation(projects.core.datastore)
    implementation(projects.core.networking)
    implementation(projects.core.widget)
    implementation(projects.ui)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.work.testing)
    implementation(libs.google.dagger.hilt.android.testing)
    implementation(libs.squareup.okhttp3)
    androidTestUtil(libs.androidx.test.orchestrator)
}
