plugins {
    alias(libs.plugins.androidTest)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.baselineprofile)
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

android {
    namespace = "com.w2sv.benchmarking"
    compileSdk = 34

    defaultConfig {
        minSdk = 28
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
}

// This is the configuration block for the Baseline Profile plugin.
// You can specify to run the generators on a managed devices or connected devices.
baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.runner)
}