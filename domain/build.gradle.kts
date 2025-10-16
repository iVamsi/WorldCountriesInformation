plugins {
    id("worldcountries.jvm.library")
}

dependencies {
    api(project(":core:model"))
    implementation(project(":tests-shared"))

    // Dependency Injection annotations
    api(libs.javax.inject)

    // Coroutines
    api(libs.kotlinx.coroutines.android)
    testImplementation(libs.kotlinx.coroutines.test)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
