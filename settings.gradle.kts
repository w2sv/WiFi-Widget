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
include(":benchmarking")
include(":core:datastore")
include(":core:domain")
include(":core:common")
include(":core:networking")
include(":core:widget")
