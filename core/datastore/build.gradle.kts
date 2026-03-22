plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
}

android {
    defaultConfig {
        consumerProguardFiles("consumer-proguard-rules.pro")
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.datastoreProto)
    implementation(libs.w2sv.datastoreutils.preferences)
    implementation(libs.w2sv.datastoreutils.datastoreflow)
    implementation(libs.w2sv.androidutils)
    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)
    testImplementation(libs.androidx.datastore.preferences.core)
    testImplementation(libs.bundles.unitTest)
}
