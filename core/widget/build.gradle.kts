plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
    alias(libs.plugins.wifiwidget.hilt.work)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.resources)
    implementation(projects.core.domain)

    implementation(libs.google.android.material)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.kotlinutils.core)
    implementation(libs.slimber)

    testImplementation(libs.bundles.unitTest)
}
