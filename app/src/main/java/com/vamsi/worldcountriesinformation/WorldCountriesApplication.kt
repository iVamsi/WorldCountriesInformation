package com.vamsi.worldcountriesinformation

import android.app.Application
import android.os.StrictMode
import androidx.appfunctions.service.AppFunctionConfiguration
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.vamsi.worldcountriesinformation.appfunctions.CountryAppFunctions
import com.vamsi.worldcountriesinformation.startup.CachePreloader
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class for World Countries Information app.
 */
@HiltAndroidApp
class WorldCountriesApplication :
    Application(),
    Configuration.Provider,
    AppFunctionConfiguration.Provider {

    @Inject lateinit var cachePreloader: CachePreloader

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var countryAppFunctions: CountryAppFunctions

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override val appFunctionConfiguration: AppFunctionConfiguration
        get() = AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(CountryAppFunctions::class.java) { countryAppFunctions }
            .build()

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
                .build(),
        )
    }
}
