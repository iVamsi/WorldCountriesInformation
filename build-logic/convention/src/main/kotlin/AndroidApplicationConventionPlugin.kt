import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

private fun Project.sdkVersion(name: String): Int {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
    return libs.findVersion(name).get().requiredVersion.toInt()
}

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = sdkVersion("compileSdk")

                defaultConfig {
                    minSdk = sdkVersion("minSdk")
                    targetSdk = sdkVersion("targetSdk")

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    vectorDrawables.useSupportLibrary = true
                }

                buildTypes {
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                    debug {
                        isDebuggable = true
                        isMinifyEnabled = false
                    }
                }

                compileOptions {
                    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_21
                    targetCompatibility = org.gradle.api.JavaVersion.VERSION_21
                }

                packaging {
                    resources {
                        excludes += listOf(
                            "META-INF/DEPENDENCIES",
                            "META-INF/LICENSE",
                            "META-INF/LICENSE.txt",
                            "META-INF/license.txt",
                            "META-INF/NOTICE",
                            "META-INF/NOTICE.txt",
                            "META-INF/notice.txt",
                            "META-INF/ASL2.0",
                            "META-INF/*.kotlin_module",
                            "/META-INF/{AL2.0,LGPL2.1}"
                        )
                    }
                }

                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                    }
                }

                buildFeatures {
                    buildConfig = true
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }
}
