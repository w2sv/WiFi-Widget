import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        allWarningsAsErrors = true
        extraWarnings = true
    }
}

ktlint {
    version.set(libs.versions.ktlintEngine)
}

dependencies {
    implementation(libs.ktlint.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.ksp.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("codeQuality") {
            id = "wifiwidget.code-quality"
            implementationClass = "CodeQualityConventionPlugin"
        }
        register("library") {
            id = "wifiwidget.library"
            implementationClass = "LibraryConventionPlugin"
        }
        register("application") {
            id = "wifiwidget.application"
            implementationClass = "ApplicationConventionPlugin"
        }
        register("hilt") {
            id = "wifiwidget.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("hilt.work") {
            id = "wifiwidget.hilt.work"
            implementationClass = "HiltWorkConventionPlugin"
        }
    }
}
