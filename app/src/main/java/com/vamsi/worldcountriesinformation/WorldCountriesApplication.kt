package com.vamsi.worldcountriesinformation

import android.app.Application
import android.os.StrictMode
import com.vamsi.worldcountriesinformation.startup.CachePreloader
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class for World Countries Information app.
 */
@HiltAndroidApp
class WorldCountriesApplication : Application() {

    @Inject lateinit var cachePreloader: CachePreloader

    override fun onCreate() {
        // Enable strict mode before Dagger creates graph
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        cachePreloader.warm()
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
