import helpers.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltWorkConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                "implementation"(library("androidx.work.runtime.ktx"))
                "implementation"(library("androidx.hilt.work"))
                "ksp"(library("androidx.hilt.compiler"))
            }
        }
    }
}
