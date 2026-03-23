import helpers.applyPlugins
import helpers.catalog
import helpers.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.applyPlugins("ksp", "google.dagger.hilt.android", catalog = catalog)

            dependencies {
                "implementation"(library("google.dagger.hilt.android"))
                "ksp"(library("google.dagger.hilt.android.compiler"))
            }
        }
    }
}
