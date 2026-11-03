@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        includeBuild("build-logic")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google {
            content {
                includeGroupByRegex("androidx(\\..*)?")
                includeGroupByRegex("com\\.android(\\..*)?")
                includeGroupByRegex("com\\.google(\\..*)?")
            }
        }

        mavenCentral()

        exclusiveContent {
            forRepository { maven("https://jitpack.io") }
            filter {
                // Required by compose-color-picker
                includeGroup("com.github.SmartToolFactory")
                includeModule("com.github.PaulWoitaschek", "Slimber")
                includeModule("com.github.w2sv", "compose-wheel-picker")
            }
        }
    }
}

rootProject.name = "WiFi_Widget"

include(":app")
include(":benchmarking")
include(":core:datastore")
include(":core:datastore-proto")
include(":core:domain")
include(":core:common")
include(":core:networking")
include(":core:resources")
include(":core:widget")
include(":store-screenshots")
include(":testing:launcher")
include(":ui")
include(":widget-integration-tests")
