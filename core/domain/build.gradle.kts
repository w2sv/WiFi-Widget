plugins {
    alias(libs.plugins.wifiwidget.library)
}

dependencies {
    implementation(projects.core.common)

    api(libs.w2sv.datastoreutils.datastoreflow)
    api(libs.w2sv.datastoreutils.preferences)
    implementation(libs.androidx.core)
    implementation(libs.w2sv.androidutils)
    implementation(libs.slimber)

    testImplementation(libs.bundles.unitTest)
}
