plugins {
    id("worldcountries.android.library")
}

android {
    namespace = "com.vamsi.worldcountriesinformation.core.common"
}

dependencies {
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // ViewModel for MVI base class
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Compose runtime for MVI effect collector
    implementation(libs.androidx.lifecycle.runtime.compose)
}
