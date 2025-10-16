plugins {
    id("worldcountries.android.library")
    id("worldcountries.android.hilt")
}

android {
    namespace = "com.vamsi.worldcountriesinformation.data.countries"
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    // Domain modules - using :model instead of :core:model for now
    implementation(project(":model"))
    implementation(project(":domain"))

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Logging
    implementation(libs.timber)
}
