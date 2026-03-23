plugins {
    alias(libs.plugins.wifiwidget.library)
}

dependencies {
    implementation(projects.core.common)
    api(libs.w2sv.datastoreutils.datastoreflow)
    api(libs.w2sv.datastoreutils.preferences)
    implementation(libs.androidx.core.ktx)
    implementation(libs.w2sv.androidutils)
    testImplementation(libs.bundles.unitTest)
}
