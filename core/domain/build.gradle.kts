plugins {
    alias(libs.plugins.wifiwidget.library)
}

dependencies {
    implementation(projects.core.common)

    implementation(libs.androidx.core)

    implementation(libs.androidutils)
    implementation(libs.slimber)
}
