plugins {
    id("worldcountries.android.application")
    id("worldcountries.android.hilt")
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
}

android {
    namespace = "com.vamsi.worldcountriesinformation"

    defaultConfig {
        applicationId = "com.vamsi.worldcountriesinformation"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.vamsi.worldcountriesinformation.WorldCountriesTestRunner"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    // Feature modules (bring in UI, ViewModels, domain dependencies transitively)
    implementation(project(":feature:countries"))
    implementation(project(":feature:countrydetails"))

    // Domain (for DI bindings)
    implementation(project(":domain"))

    // Data module (provides repository implementation)
    implementation(project(":data:countries"))

    // Core modules (for MainActivity)
    implementation(project(":core:designsystem"))

    // Core infrastructure (needed for Hilt DI)
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    // Compose (for MainActivity)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    // AndroidX Core (for MainActivity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Logging (for Application class)
    implementation(libs.timber)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
}
