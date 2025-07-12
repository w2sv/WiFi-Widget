plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.compose.compiler)
}

android {
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.networking)

    implementation(libs.androidx.workmanager)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.appwidget.preview)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.glance.preview)
    implementation(libs.google.material)
    implementation(libs.slimber)
    implementation(libs.w2sv.androidutils)
    implementation(libs.w2sv.kotlinutils)
}
