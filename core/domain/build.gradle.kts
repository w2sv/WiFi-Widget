plugins {
    alias(libs.plugins.wifiwidget.library)
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.androidx.core.ktx)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.kotlinutils.core)
    testImplementation(libs.bundles.unitTest)
}
