plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
}

dependencies {
    implementation(projects.domain)

    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidutils)
    implementation(libs.slimber)
}