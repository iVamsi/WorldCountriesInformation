plugins {
    id("worldcountries.android.library")
    id("worldcountries.android.hilt")
}

android {
    namespace = "com.vamsi.worldcountriesinformation.core.network"
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:model"))

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // JSON - Moshi
    implementation(libs.moshi)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
