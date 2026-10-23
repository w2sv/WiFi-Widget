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
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "WiFi_Widget"

include(":app")
include(":ui")
include(":benchmarking")
include(":store-screenshots")
include(":widget-integration-tests")
include(":core:datastore")
include(":core:datastore-proto")
include(":core:domain")
include(":core:common")
include(":core:networking")
include(":core:widget")
