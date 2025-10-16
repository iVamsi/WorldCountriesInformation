plugins {
    id("worldcountries.jvm.library")
}

dependencies {
    api(project(":core:model"))

    // Coroutines
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.coroutines.test) {
        // conflicts with mockito due to direct inclusion of byte buddy
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-debug")
    }

    // Testing
    api(libs.junit)
    api(libs.mockito.core)
    api(libs.mockito.kotlin)
}
