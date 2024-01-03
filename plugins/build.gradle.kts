plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("library") {
            id = "wifiwidget.library"
            implementationClass = "LibraryPlugin"
        }

        register("hilt") {
            id = "wifiwidget.hilt"
            implementationClass = "HiltPlugin"
        }
    }
}
