import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

/**
 * Applies Kover with a shared "coverage" variant for JVM and Android debug unit tests.
 */
class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlinx.kover")
            extensions.configure<KoverProjectExtension> {
                currentProject {
                    createVariant("coverage") {
                        add("jvm", optional = true)
                        add("debug", optional = true)
                    }
                }
            }
        }
    }
}
