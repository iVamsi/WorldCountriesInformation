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
    implementation(Libs.CORE_KTX)

    implementation(Libs.KOTLIN_STDLIB)

    implementation(Libs.DAGGER)
    kapt(Libs.DAGGER_COMPILER)
}