plugins {
    id("worldcountries.android.library")
    id("worldcountries.android.hilt")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.vamsi.worldcountriesinformation.feature.widget"

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core modules
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))

    // Domain
    implementation(project(":domain"))

    // Glance - Widgets
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose (for widget previews and Material 3 colors)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Coil for image loading
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // Logging
    implementation(libs.timber)

    // WorkManager (for periodic updates)
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

