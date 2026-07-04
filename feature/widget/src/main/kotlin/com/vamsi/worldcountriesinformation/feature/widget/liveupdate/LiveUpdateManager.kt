package com.vamsi.worldcountriesinformation.feature.widget.liveupdate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vamsi.worldcountriesinformation.feature.widget.R
import com.vamsi.worldcountriesinformation.feature.widget.data.WidgetDataSource
import com.vamsi.worldcountriesinformation.feature.widget.notification.notifyIfAllowed
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface LiveUpdateManager {
    suspend fun publishCountryOfDayUpdate()
    fun cancelUpdate()
    fun isSupported(): Boolean
}

@Singleton
class LiveUpdateManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetDataSource: WidgetDataSource,
) : LiveUpdateManager {

    override fun isSupported(): Boolean = Build.VERSION.SDK_INT >= 35

    override suspend fun publishCountryOfDayUpdate() {
        if (!isSupported()) return

        val country = widgetDataSource.getWidgetData().featuredCountry ?: return
        ensureChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle("Country of the Day")
            .setContentText(country.name)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${country.name} — ${country.capital}, ${country.region}"),
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .apply {
                if (Build.VERSION.SDK_INT >= 35) {
                    setRequestPromotedOngoing(true)
                }
            }
            .build()

        if (Build.VERSION.SDK_INT >= 36 &&
            !notification.hasPromotableCharacteristics()
        ) {
            Timber.d("Live update notification missing promotable characteristics")
        }

        if (!context.notifyIfAllowed(NOTIFICATION_ID, notification)) {
            Timber.w("Skipping live update notification; permission not granted")
        }
    }

    override fun cancelUpdate() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Country of the Day",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Live country-of-the-day updates on supported devices"
        }
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "country_of_day_live_update"
        const val NOTIFICATION_ID = 9001
    }
}
