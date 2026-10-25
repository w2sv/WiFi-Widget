plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.w2sv.wifiwidget.ui"
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "androidx.compose.foundation.layout.ExperimentalLayoutApi"
        )
    }
}

composeCompiler {
    includeSourceInformation = true
    stabilityConfigurationFiles.add(project.layout.projectDirectory.file("compose_compiler_config.conf"))
    metricsDestination.set(project.layout.buildDirectory.dir("compose_compiler"))
    reportsDestination.set(project.layout.buildDirectory.dir("compose_compiler"))
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)

    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.androidutils.backpress)
    implementation(libs.w2sv.kotlinutils.core)
    implementation(libs.w2sv.kotlinutils.coroutines)
    implementation(libs.w2sv.colorpicker)
    implementation(libs.w2sv.composed.animation)
    implementation(libs.w2sv.composed.core)
    implementation(libs.w2sv.composed.material3)
    implementation(libs.w2sv.composed.navigation3)
    implementation(libs.w2sv.augmentedPermissions)
    implementation(libs.w2sv.composeWheelPicker)
    implementation(libs.w2sv.reversiblestate)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3.android)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.constraintlayout.compose)

    lintChecks(libs.slack.lint.compose.lint.checks)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.reorderable)
    implementation(libs.slimber)
    implementation(libs.flowExt)

    testImplementation(libs.bundles.unitTest)
    testImplementation(libs.androidx.compose.ui.test.junit4.android)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
