plugins {
    alias(libs.plugins.wifiwidget.library)
}

dependencies {
    implementation(projects.core.common)
    api(libs.w2sv.persistedPreferences)
    implementation(libs.w2sv.kotlinutils.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.w2sv.androidutils.core)
    testImplementation(libs.bundles.unitTest)
}
