package com.vamsi.worldcountriesinformation.feature.widget.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val WORK_NAME = "country_of_day_notification"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val initialDelayMs = delayUntilNextMorningMs()
        val periodicRequest = PeriodicWorkRequestBuilder<CountryOfDayNotificationWorker>(
            24,
            TimeUnit.HOURS,
        )
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest,
        )

        val immediateRequest = OneTimeWorkRequestBuilder<CountryOfDayNotificationWorker>()
            .setConstraints(constraints)
            .addTag(WORK_NAME)
            .build()
        WorkManager.getInstance(context).enqueue(immediateRequest)

        Timber.d("Scheduled daily country notification (initial delay ${initialDelayMs}ms)")
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        Timber.d("Cancelled daily country notification")
    }

    private fun delayUntilNextMorningMs(): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now) || equals(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return next.timeInMillis - now.timeInMillis
    }
}
