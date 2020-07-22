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
    implementation(project(":common"))
    implementation(Libs.KOTLIN_STDLIB)

    implementation(Libs.COROUTINES)

    implementation(Libs.DAGGER)
    kapt(Libs.DAGGER_COMPILER)
}