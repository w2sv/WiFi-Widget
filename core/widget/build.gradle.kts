plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.networking)

    implementation(libs.androidx.workmanager)
    implementation(libs.google.material)
    implementation(libs.slimber)
    implementation(libs.w2sv.androidutils)
    implementation(libs.w2sv.kotlinutils)
}