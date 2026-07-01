// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.navigation.safeargs) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
    id("worldcountries.quality")
}

dependencies {
    kover(project(":domain"))
    kover(project(":data:countries"))
    kover(project(":feature:countries"))
    kover(project(":feature:countrydetails"))
    kover(project(":feature:compare"))
    kover(project(":feature:settings"))
    kover(project(":feature:widget"))
    kover(project(":feature:quiz"))
    kover(project(":feature:wear"))
}

kover {
    currentProject {
        createVariant("coverage") {
            add("jvm", optional = true)
            add("debug", optional = true)
        }
    }
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
                annotatedBy(
                    "androidx.compose.ui.tooling.preview.Preview",
                    "androidx.compose.runtime.Composable",
                )
                classes(
                    "*Hilt_*",
                    "*_*Factory",
                    "*_*FactoryImpl",
                    "*_*MembersInjector",
                    "dagger.hilt.*",
                    "*ComposableSingletons*",
                )
            }
        }
        variant("coverage") {
            verify {
                rule {
                    minBound(15)
                }
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin" && requested.name == "kotlin-metadata-jvm") {
                useVersion(libs.versions.kotlin.get())
                because("Hilt annotation processors must parse Kotlin ${libs.versions.kotlin.get()} metadata")
            }
        }
    }
}
