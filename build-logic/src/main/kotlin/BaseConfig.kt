import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal fun Project.baseConfig() {
    pluginManager.apply {
        apply(libs.findPluginId("ktlint"))
    }

    extensions.apply {
        configure<KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
        configure<BaseExtension> {
            // Set namespace to com.w2sv.<module-name>
            namespace = "com.w2sv." + path.removePrefix(":").replace(':', '.')

            defaultConfig {
                minSdk = libs.findVersionInt("minSdk")
                targetSdk = libs.findVersionInt("compileSdk")
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileSdkVersion(libs.findVersionInt("compileSdk"))
            testOptions {
                unitTests {
                    isReturnDefaultValues = true
                    isIncludeAndroidResources = true
                }
                animationsDisabled = true
            }
            packagingOptions {
                resources {
                    excludes.add("/META-INF/*")
                }
            }
        }
    }
}
