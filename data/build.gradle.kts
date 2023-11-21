plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
}

dependencies {
    implementation(project(":common"))

    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidutils)
    implementation(libs.slimber)
}
