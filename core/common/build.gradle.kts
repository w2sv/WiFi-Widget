plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.annotation)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.datastoreutils.preferences)
    api(libs.flowExt)
}
