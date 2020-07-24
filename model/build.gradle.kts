plugins {
    id("java-library")
    id("kotlin")
    kotlin("kapt")
}

dependencies {
    implementation(Libs.CORE_KTX)

    implementation(Libs.KOTLIN_STDLIB)

    // Moshi
    implementation(Libs.MOSHI)
    implementation(Libs.MOSHI_CODEGEN)
    kapt(Libs.MOSHI_CODEGEN)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}