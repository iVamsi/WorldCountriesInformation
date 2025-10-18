/**
 * Core Navigation module build configuration.
 *
 * This module provides:
 * - Navigation route definitions (Screen sealed class)
 * - Navigation extension functions
 * - Type-safe navigation utilities
 *
 * Note: This is an Android library module (not pure JVM) because it uses
 * androidx.navigation APIs which are Android-specific.
 */
plugins {
    id("worldcountries.android.library")
}

android {
    namespace = "com.vamsi.worldcountriesinformation.core.navigation"
}

dependencies {
    // Navigation Compose - for NavController and navigation APIs
    api(libs.androidx.navigation.compose)
    
    // Testing
    testImplementation(libs.junit)
}
