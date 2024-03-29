plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
}

dependencies {
    implementation(projects.common)
    implementation(projects.domain)
    implementation(projects.networking)

    implementation(libs.androidx.workmanager)
    implementation(libs.androidx.localbroadcastmanager)
    implementation(libs.google.material)
    implementation(libs.slimber)
    implementation(libs.androidutils)
}