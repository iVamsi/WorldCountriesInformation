plugins {
    id("worldcountries.jvm.library")
}

dependencies {
    api(project(":model"))
    implementation(project(":tests-shared"))

    // Dependency Injection annotations
    api(libs.javax.inject)

    // Coroutines
    api(libs.kotlinx.coroutines.android)
    testImplementation(libs.kotlinx.coroutines.test) {
        // conflicts with mockito due to direct inclusion of byte buddy
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-debug")
    }

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
}
