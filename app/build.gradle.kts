import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("worldcountries.android.application")
    id("worldcountries.android.hilt")
    alias(libs.plugins.navigation.safeargs)
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

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    // Module dependencies
    implementation(project(":domain"))
    implementation(project(":model"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)

    // UI Components
    implementation(libs.material.components)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.flexbox)
    implementation(libs.lottie)

    // Architecture Components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    ksp(libs.androidx.lifecycle.compiler)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.androidx.room.testing)

    // Image Loading
    implementation(libs.glide)
    ksp(libs.glide.compiler)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // JSON
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)
    implementation(libs.gson)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Logging
    implementation(libs.timber)

    // Google Play Services
    implementation(libs.play.services.maps)

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
