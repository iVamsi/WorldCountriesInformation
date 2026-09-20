package com.vamsi.worldcountriesinformation.data.countries.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.CountriesRepository
import com.vamsi.worldcountriesinformation.domain.preferences.RefreshInterval
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferences
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferencesPort
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Checks the country sources at the interval the user picked in Settings and rewrites the cache
 * only when the data changed (see [CountriesRepository.sync]).
 */
@HiltWorker
class CountriesSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CountriesRepository,
    private val userPreferencesPort: UserPreferencesPort,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!shouldSync(userPreferencesPort.userPreferences.first())) {
            Timber.d("Countries sync skipped: offline mode or cache-only policy")
            return Result.success()
        }
        return repository.sync().fold(
            onSuccess = { outcome ->
                Timber.d("Countries sync finished: $outcome")
                Result.success()
            },
            onFailure = { error ->
                Timber.w(error, "Countries sync failed; will retry")
                Result.retry()
            },
        )
    }

    companion object {
        const val WORK_NAME = "countries-sync"

        /** The user asked the app to stay off the network; honour that in the background too. */
        fun shouldSync(preferences: UserPreferences): Boolean {
            val cacheOnly = preferences.cachePolicy == CachePolicy.CACHE_ONLY
            return !preferences.offlineMode && !cacheOnly
        }

        /** (Re)schedules the periodic check; UPDATE keeps the next run time when only the interval changes. */
        fun schedule(context: Context, interval: RefreshInterval) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<CountriesSyncWorker>(interval.millis, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag(WORK_NAME)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
            Timber.d("Scheduled countries sync every ${interval.name.lowercase()}")
        }
    }
}
