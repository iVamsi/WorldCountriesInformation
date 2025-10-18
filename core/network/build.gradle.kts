plugins {
    id("worldcountries.android.library")
    id("worldcountries.android.hilt")
}

android {
    namespace = "com.vamsi.worldcountriesinformation.core.network"
    
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:model"))

    // Networking - Use api for Hilt visibility
    api(libs.retrofit)
    api(libs.retrofit.converter.moshi)
    api(libs.okhttp)
    api(libs.okhttp.logging)

    // JSON - Moshi
    api(libs.moshi)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Testing
    testImplementation(libs.junit)
}
