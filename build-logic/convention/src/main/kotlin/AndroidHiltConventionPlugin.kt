import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("dagger.hilt.android.plugin")
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
