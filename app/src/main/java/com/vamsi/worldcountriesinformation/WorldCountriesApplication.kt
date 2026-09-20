package com.vamsi.worldcountriesinformation

import android.app.Application
import android.os.StrictMode
import androidx.appfunctions.service.AppFunctionConfiguration
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.vamsi.worldcountriesinformation.appfunctions.CountryAppFunctions
import com.vamsi.worldcountriesinformation.data.countries.sync.CountriesSyncWorker
import com.vamsi.worldcountriesinformation.domain.di.IoDispatcher
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferencesPort
import com.vamsi.worldcountriesinformation.startup.CachePreloader
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    @Inject lateinit var userPreferencesPort: UserPreferencesPort

    @Inject @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    private val syncScope by lazy { CoroutineScope(SupervisorJob() + ioDispatcher) }

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
        scheduleCountriesSync()
    }

    /** Follows the Settings interval; UPDATE policy means a changed interval replans without a duplicate job. */
    private fun scheduleCountriesSync() {
        syncScope.launch {
            userPreferencesPort.userPreferences
                .map { it.refreshInterval }
                .distinctUntilChanged()
                .collect { interval -> CountriesSyncWorker.schedule(this@WorldCountriesApplication, interval) }
        }
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
