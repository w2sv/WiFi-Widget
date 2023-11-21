plugins {
    alias(libs.plugins.wifiwidget.library)
}

dependencies {
    implementation(projects.common)

    implementation(libs.androidx.core)

    implementation(libs.androidutils)
    implementation(libs.slimber)
}
