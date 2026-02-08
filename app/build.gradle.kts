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
    implementation(project(":feature:settings"))
    implementation(project(":feature:widget"))

    // Domain (for DI bindings)
    implementation(project(":domain"))

    // Data module (provides repository implementation)
    implementation(project(":data:countries"))

    // Core modules (for MainActivity and tests)
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":core:common"))

    // Core infrastructure (needed for Hilt DI)
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    // Compose (for MainActivity)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.snapnotify)

    // AndroidX Core (for MainActivity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Baseline Profile
    implementation(libs.androidx.profileinstaller)

    // Logging (for Application class)
    implementation(libs.timber)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)

    // Compose UI Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Coroutines testing for async operations
    androidTestImplementation(libs.kotlinx.coroutines.test)

    // Test runner and rules
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
}
