plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.networking)

    implementation(libs.androidx.workmanager)
    implementation(libs.androidx.localbroadcastmanager)
    implementation(libs.google.material)
    implementation(libs.slimber)
    implementation(libs.androidutils)
}