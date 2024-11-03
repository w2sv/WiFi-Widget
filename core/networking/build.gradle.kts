plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)

    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.w2sv.androidutils)
    implementation(libs.slimber)
    implementation(libs.okhttp3)

    testImplementation(libs.bundles.unitTest)
}
