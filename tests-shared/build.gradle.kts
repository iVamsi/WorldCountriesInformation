plugins {
    id("java-library")
    id("kotlin")
    kotlin("kapt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":model"))

    implementation(Libs.KOTLIN_STDLIB)

    implementation(Libs.COROUTINES)
    implementation(Libs.COROUTINES_TEST) {
        // conflicts with mockito due to direct inclusion of byte buddy
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-debug")
    }

    implementation(Libs.DAGGER)
    kapt(Libs.DAGGER_COMPILER)

    implementation(Libs.JUNIT)
    implementation(Libs.MOCKITO_CORE)
    implementation(Libs.MOCKITO_KOTLIN)
    implementation(Libs.MOCKITO_INLINE)
}