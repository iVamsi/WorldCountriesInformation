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

    buildTypes {
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }
}

dependencies {
    // Feature modules (bring in UI, ViewModels, domain dependencies transitively)
    implementation(project(":feature:countries"))
    implementation(project(":feature:countrydetails"))
    implementation(project(":feature:compare"))
    implementation(project(":feature:quiz"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:widget"))
    implementation(project(":feature:wear"))

    // Domain (for DI bindings)
    implementation(project(":domain"))

    // Data module (provides repository implementation)
    implementation(project(":data:countries"))

    // Core modules (for MainActivity)
    implementation(project(":core:designsystem"))
    implementation(project(":core:datastore"))
    implementation(project(":core:navigation"))

    // Core infrastructure (needed for Hilt DI)
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:ai"))

    // Compose (for MainActivity)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3.adaptive)
    implementation(libs.androidx.material3.adaptive.layout)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.snapnotify)

    // AndroidX Core (for MainActivity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Baseline Profile
    implementation(libs.androidx.profileinstaller)

    // Logging (for Application class)
    implementation(libs.timber)

    // Open source licenses
    implementation(libs.play.services.oss.licenses)

    // WorkManager + Hilt workers
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.hilt.compiler)

    // AppFunctions (assistant integration) — manual factory in WorldCountriesApplication;
    // KSP compiler omitted: multi-module ':' paths break FunctionComponentRegistry generation.
    implementation(libs.androidx.appfunctions)
    implementation(libs.androidx.appfunctions.service)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(project(":core:common"))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    kspAndroidTest(libs.hilt.compiler)
}
