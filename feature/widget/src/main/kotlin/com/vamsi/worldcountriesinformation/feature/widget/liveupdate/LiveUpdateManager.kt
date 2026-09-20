package com.vamsi.worldcountriesinformation.feature.widget.liveupdate

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.vamsi.worldcountriesinformation.feature.widget.R
import com.vamsi.worldcountriesinformation.feature.widget.data.WidgetDataSource
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
        // The user can revoke POST_NOTIFICATIONS at any time; posting without it throws on Android 13+.
        val permitted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        val country = widgetDataSource.getWidgetData().featuredCountry
        if (!isSupported() || !permitted || country == null) {
            Timber.d("Live update skipped: supported=${isSupported()}, permitted=$permitted, country=${country != null}")
            return
        }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                    setRequestPromotedOngoing(true)
                }
            }
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA &&
            !notification.hasPromotableCharacteristics()
        ) {
            Timber.d("Live update notification missing promotable characteristics")
        }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
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
