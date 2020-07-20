plugins {
    id("com.android.application")
    id("androidx.navigation.safeargs")
    id("dagger.hilt.android.plugin")
    kotlin("android")
    kotlin("kapt")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.vamsi.worldcountriesinformation"
        minSdkVersion(24)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.vamsi.worldcountriesinformation.WorldCountriesTestRunner"
        vectorDrawables.useSupportLibrary = true

        useLibrary("android.test.runner")
        useLibrary("android.test.base")
        useLibrary("android.test.mock")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures.dataBinding = true
    buildFeatures.viewBinding = true

    kotlinOptions {
        jvmTarget = "1.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(Libs.kotlinStdLib)
    implementation(Libs.appCompat)
    implementation(Libs.constraintLayout)
    implementation(Libs.viewpager2)

    implementation(Libs.coreCtx)

    implementation(Libs.viewModelKtx)
    implementation(Libs.lifecycleExt)
    implementation(Libs.lifecycleKtx)
    kapt(Libs.lifecycleCompiler)

    implementation(Libs.navigationFragmentKtx)
    implementation(Libs.navigationUiKtx)

    implementation(Libs.material)
    implementation(Libs.roomRuntime)
    implementation(Libs.roomKtx)
    kapt(Libs.roomCompiler)

    implementation(Libs.coroutinesCore)
    implementation(Libs.coroutinesAndroid)

    implementation(Libs.hilt)
    implementation(Libs.hiltJetpack)
    kapt(Libs.hiltCompiler)
    kapt(Libs.hiltJetpackCompiler)

    implementation(Libs.retrofit)
    implementation(Libs.retrofitMoshi)

    implementation(Libs.moshi)
    kapt(Libs.moshiCodeGen)

    implementation(Libs.glide)
    kapt(Libs.glideCompiler)

    implementation(Libs.circleIndicator)

    testImplementation(Libs.coroutinesCore)
    testImplementation(Libs.mockito)
    testImplementation(Libs.mockitoKotlin)
    testImplementation(Libs.coreTesting)
    testImplementation(Libs.junit)
    testImplementation(Libs.truth)
    testImplementation(Libs.coroutinesTest)
    androidTestImplementation(Libs.testRunner)
    androidTestImplementation(Libs.espresso)
    androidTestImplementation(Libs.hiltAndroidTesting)
    kaptAndroidTest(Libs.hiltAndroidTestingCompiler)
}
