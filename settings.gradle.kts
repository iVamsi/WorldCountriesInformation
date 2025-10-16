pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "World Countries Information"
include(
    ":app",
    ":domain",
    ":tests-shared",
    ":core:common",
    ":core:model",
    ":core:designsystem",
    ":core:navigation",
    ":core:network",
    ":core:database",
    ":data:countries",
    ":feature:countries",
    ":feature:countrydetails"
)
