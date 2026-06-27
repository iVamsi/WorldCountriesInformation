plugins {
    id("worldcountries.android.library")
    id("worldcountries.android.hilt")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    id("worldcountries.kover")
}

android {
    namespace = "com.vamsi.worldcountriesinformation.feature.countrydetails"

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core modules
    implementation(project(":core:ai"))
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":core:model"))

    // Domain
    implementation(project(":domain"))

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.snapnotify)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Image loading
    implementation(libs.coil.compose)

    // OpenStreetMap
    implementation(libs.osmdroid.android)
    implementation(libs.kotlinx.serialization.json)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Logging
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(project(":tests-shared"))
}
