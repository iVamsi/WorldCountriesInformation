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

            dependencies {
                "implementation"(project.extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
                    .named("libs").findLibrary("hilt.android").get())
                "ksp"(project.extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
                    .named("libs").findLibrary("hilt.compiler").get())
                "kspAndroidTest"(project.extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
                    .named("libs").findLibrary("hilt.compiler").get())
            }
        }
    }
}
