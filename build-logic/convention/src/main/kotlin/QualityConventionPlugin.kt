import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        require(target == target.rootProject) {
            "QualityConventionPlugin must be applied to the root project"
        }

        target.subprojects {
            if (name == "convention") return@subprojects

            pluginManager.apply("io.gitlab.arturbosch.detekt")
            pluginManager.apply("com.diffplug.spotless")

            extensions.configure<DetektExtension> {
                config.setFrom(target.file("detekt.yml"))
                buildUponDefaultConfig = true
                parallel = true
                val baselineFile = file("detekt-baseline.xml")
                if (baselineFile.exists()) {
                    baseline = baselineFile
                }
            }

            extensions.configure<SpotlessExtension> {
                kotlin {
                    target("src/**/*.kt")
                    targetExclude("**/build/**")
                    ktlint().editorConfigOverride(
                        mapOf(
                            "ktlint_standard_max-line-length" to "160",
                            "ktlint_standard_no-wildcard-imports" to "disabled",
                            "ktlint_standard_no-consecutive-comments" to "disabled",
                            "ktlint_standard_function-naming" to "disabled",
                            "ktlint_standard_filename" to "disabled",
                            "ktlint_standard_package-name" to "disabled",
                            "ktlint_standard_backing-property-naming" to "disabled",
                        ),
                    )
                }
                kotlinGradle {
                    target("*.gradle.kts")
                    ktlint()
                }
            }

            tasks.withType<Detekt>().configureEach {
                reports {
                    html.required.set(true)
                    xml.required.set(true)
                }
            }
        }
    }
}
