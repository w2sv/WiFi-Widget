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
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.persistedPreferences)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)
    implementation(libs.slimber)
    testImplementation(libs.bundles.unitTest)
    testImplementation(libs.w2sv.kotlinutils.core)
}
