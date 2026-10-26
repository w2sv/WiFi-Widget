plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.w2sv.core.resources"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
