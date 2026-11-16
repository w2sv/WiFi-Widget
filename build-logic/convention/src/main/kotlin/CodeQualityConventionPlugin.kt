import helpers.catalog
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jlleitschuh.gradle.ktlint.KtlintExtension

class CodeQualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")

            extensions.configure<KtlintExtension> {
                version.set(catalog.findVersion("ktlintEngine").get().requiredVersion)
            }

            tasks.withType<KotlinCompilationTask<*>>().configureEach {
                compilerOptions {
                    allWarningsAsErrors.set(true)
                    extraWarnings.set(true)
                }
            }
        }
    }
}
