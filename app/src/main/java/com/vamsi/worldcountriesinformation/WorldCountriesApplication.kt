package com.vamsi.worldcountriesinformation

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for World Countries Information app.
 */
@HiltAndroidApp
class WorldCountriesApplication : Application() {

    override fun onCreate() {
        // Enable strict mode before Dagger creates graph
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
    }
}
