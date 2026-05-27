plugins {
    id("worldcountries.android.library")
    id("worldcountries.android.hilt")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.vamsi.worldcountriesinformation.feature.wear"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":domain"))
    implementation(project(":feature:widget"))

    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.wear.tiles)
    implementation(libs.androidx.glance.material3)

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
}
