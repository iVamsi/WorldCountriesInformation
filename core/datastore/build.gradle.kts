plugins {
    id("worldcountries.android.library")
    id("worldcountries.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.vamsi.worldcountriesinformation.core.datastore"
}

dependencies {
    // Core modules
    implementation(project(":core:model"))
    
    // DataStore
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    
    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
