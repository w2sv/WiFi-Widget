plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.w2sv.wifiwidget.testing.launcher"
}

dependencies {
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
}
