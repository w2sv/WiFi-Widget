plugins {
    alias(libs.plugins.wifiwidget.library)
}

android { testOptions.unitTests.all { it.enabled = false } }

dependencies {
    implementation(projects.core.common)
    api(libs.w2sv.datastoreutils.datastoreflow)
    api(libs.w2sv.datastoreutils.preferences)
    implementation(libs.androidx.core)
    implementation(libs.w2sv.androidutils)
    implementation(libs.slimber)
}
