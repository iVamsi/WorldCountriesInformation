plugins {
    id("worldcountries.android.library")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.vamsi.worldcountriesinformation.core.designsystem"

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Material Components for theme compatibility
    implementation(libs.material.components)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
}
