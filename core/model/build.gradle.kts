plugins {
    id("worldcountries.jvm.library")
    alias(libs.plugins.ksp)
}

dependencies {
    // JSON - Moshi
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)
}
