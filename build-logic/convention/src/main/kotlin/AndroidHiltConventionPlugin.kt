import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("dagger.hilt.android.plugin")
            }

            // Hilt's unit-test pipeline can leave compiled output without any @Test entries. Gradle 9 then
            // fails with failOnNoDiscoveredTests for library modules that intentionally have no unit tests.
            pluginManager.withPlugin("com.android.library") {
                tasks.withType<Test>().configureEach {
                    failOnNoDiscoveredTests.set(false)
                }
            }

            val libs = project.extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
                .named("libs")
            dependencies {
                "implementation"(libs.findLibrary("hilt.android").get())
                "ksp"(libs.findLibrary("hilt.compiler").get())
                "kspAndroidTest"(libs.findLibrary("hilt.compiler").get())
                // Override Hilt's bundled kotlin-metadata-jvm so Kotlin 2.4 @Metadata parses.
                "ksp"(libs.findLibrary("kotlin.metadata.jvm").get())
            }
        }
    }
}
