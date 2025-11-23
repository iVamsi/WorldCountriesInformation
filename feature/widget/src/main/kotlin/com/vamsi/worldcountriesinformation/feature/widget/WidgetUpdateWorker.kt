package com.vamsi.worldcountriesinformation.feature.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Worker that periodically updates the widget
 * Updates every 6 hours to show a new "Country of the Day"
 */
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Updating Country Widget")

            // Update all widget instances
            CountryWidget().updateAll(context)

            Timber.d("Country Widget updated successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Failed to update Country Widget")
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "country_widget_update"
        private const val UPDATE_INTERVAL_HOURS = 6L

        /**
         * Schedule periodic widget updates
         */
        fun schedulePeriodicUpdates(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                UPDATE_INTERVAL_HOURS,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

            Timber.d("Scheduled periodic widget updates every $UPDATE_INTERVAL_HOURS hours")
        }

        /**
         * Cancel periodic widget updates
         */
        fun cancelPeriodicUpdates(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_NAME)
            Timber.d("Cancelled periodic widget updates")
        }

        /**
         * Trigger an immediate update of all widgets
         */
        suspend fun updateWidgetsNow(context: Context) {
            try {
                CountryWidget().updateAll(context)
                Timber.d("Triggered immediate widget update")
            } catch (e: Exception) {
                Timber.e(e, "Failed to trigger immediate widget update")
            }
        }
    }
}

