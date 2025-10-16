plugins {
    id("worldcountries.jvm.library")
}

dependencies {
    api(project(":core:model"))

    // Coroutines
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.coroutines.test)

    // Testing
    api(libs.junit)
    api(libs.mockk)
}
