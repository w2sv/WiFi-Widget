plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
}

dependencies {
    implementation(projects.domain)
    implementation(projects.common)

    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidutils)
    implementation(libs.slimber)
    implementation(libs.okhttp)
}