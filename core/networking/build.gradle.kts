plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.w2sv.androidutils.core)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.squareup.okhttp3)
    implementation(libs.squareup.okhttp3.coroutines)

    testImplementation(libs.bundles.unitTest)
}
