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
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
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
    }
}
