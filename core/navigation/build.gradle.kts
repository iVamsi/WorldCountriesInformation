/**
 * Core Navigation module build configuration.
 *
 * This module provides:
 * - Navigation route definitions (NavKey implementations)
 * - Navigation state management (NavigationState)
 * - Navigator for handling navigation events
 * - Type-safe navigation utilities
 *
 * Note: This is an Android library module (not pure JVM) because it uses
 * Navigation 3 APIs which are Android-specific.
 */
plugins {
    id("worldcountries.android.library")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.vamsi.worldcountriesinformation.core.navigation"

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose BOM and runtime
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)

    // Navigation 3 - Core navigation APIs
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)

    // Lifecycle ViewModel for Navigation 3 (for scoped ViewModels)
    api(libs.androidx.lifecycle.viewmodel.navigation3)

    // KotlinX Serialization for NavKey serialization
    api(libs.kotlinx.serialization.core)

    // Testing
    testImplementation(libs.junit)
}
