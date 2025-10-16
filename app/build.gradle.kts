import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

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

        // Read the API key from ./local.properties
        val mapsApiKey: String = gradleLocalProperties(rootDir, providers).getProperty("mapsApiKey") ?: ""
        manifestPlaceholders["GRADLE_MAPS_API_KEY"] = mapsApiKey

        //noinspection WrongGradleMethod
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }
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
    // Feature modules
    implementation(project(":feature:countries"))
    implementation(project(":feature:countrydetails"))

    // Domain and Data modules
    implementation(project(":domain"))
    implementation(project(":data:countries"))

    // Core modules
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.ktx)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Material Components (required for theme compatibility)
    implementation(libs.material.components)

    // Architecture Components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    ksp(libs.androidx.lifecycle.compiler)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.androidx.room.testing)

    // Image Loading - Coil for Compose
    implementation(libs.coil.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // JSON - Moshi for JSON serialization
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Logging
    implementation(libs.timber)

    // Google Play Services
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.hamcrest)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)

    // Android Testing
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.contrib)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.hilt.android.testing)
}
