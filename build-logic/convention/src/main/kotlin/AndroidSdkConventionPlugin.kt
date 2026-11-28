import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.TestExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidSdkConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val targetSdkVersion = 37

            pluginManager.withPlugin("com.android.application") {
                extensions.configure<ApplicationExtension> {
                    configureSdk()
                    defaultConfig.targetSdk = targetSdkVersion
                }
            }
            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryExtension> {
                    configureSdk()
                }
            }
            pluginManager.withPlugin("com.android.test") {
                extensions.configure<TestExtension> {
                    configureSdk()
                    defaultConfig.targetSdk = targetSdkVersion
                }
            }
        }
    }
}

private fun CommonExtension.configureSdk() {
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }
    defaultConfig.minSdk = 26
}
