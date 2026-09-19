package com.vamsi.worldcountriesinformation

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.testing.HiltTestApplication

class WorldCountriesTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application = super.newApplication(cl, HiltTestApplication::class.java.name, context)

    // HiltTestApplication replaces WorldCountriesApplication, which is the app's WorkManager
    // Configuration.Provider, and the manifest disables the auto-initializer.
    override fun callApplicationOnCreate(app: Application) {
        WorkManager.initialize(app, Configuration.Builder().build())
        super.callApplicationOnCreate(app)
    }
}
