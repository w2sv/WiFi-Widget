import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

internal fun Project.baseConfig() {
    with(extensions) {
        configure<KotlinProjectExtension> {
            jvmToolchain {
                languageVersion.set(JavaLanguageVersion.of(libs.findVersionInt("java")))
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
                unitTests.isReturnDefaultValues = true
                animationsDisabled = true
                unitTests.isIncludeAndroidResources = true
            }
            packagingOptions {
                resources {
                    excludes.add("/META-INF/*")
                }
            }
        }
    }
}