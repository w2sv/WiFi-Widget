plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
    alias(libs.plugins.wifiwidget.hilt.work)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.paparazzi)
}

android {
    buildFeatures {
        compose = true
    }
}

kotlin { compilerOptions { optIn.add("androidx.glance.preview.ExperimentalGlancePreviewApi") } }

val previewTestClass = "WifiGlancePaparazziPreviewTest"

val exportGlancePreviews = tasks.register<Sync>("exportGlancePreviews") {
    group = "verification"
    description = "Renders Glance previews to PNG."

    dependsOn("testDebugUnitTest")

    // Paparazzi's normal test run writes its rendered report under build/reports/paparazzi.
    from(layout.buildDirectory.dir("reports/paparazzi")) {
        include("**/*.png")
    }

    into(layout.buildDirectory.dir("generated/glance-previews"))
}

gradle.taskGraph.whenReady {
    tasks.named<Test>("testDebugUnitTest").configure {
        if (hasTask(exportGlancePreviews.get())) {
            // Export invocation: run ONLY the Glance preview test.
            filter { includeTestsMatching("*/$previewTestClass") }
        } else {
            // Normal unit-test invocation: never run it.
            exclude("**/$previewTestClass.class")
        }
    }
}


dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.networking)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.appwidget.preview)
    implementation(libs.androidx.glance.preview)
    implementation(libs.google.android.material)
    implementation(libs.w2sv.androidutils.core)

    testImplementation(libs.androidx.glance.appwidget.testing)
    testImplementation(libs.androidx.glance.testing)
    testImplementation(libs.paparazzi)
    testImplementation(libs.bundles.unitTest)
}
