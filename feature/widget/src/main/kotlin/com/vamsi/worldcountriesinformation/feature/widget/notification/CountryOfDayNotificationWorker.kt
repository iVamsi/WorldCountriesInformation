package com.vamsi.worldcountriesinformation.feature.widget.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.FeaturedCountrySelector
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.feature.widget.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

@HiltWorker
class CountryOfDayNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val featuredCountrySelector: FeaturedCountrySelector,
    private val getCountriesUseCase: GetCountriesUseCase,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            ensureChannel()

            val response = getCountriesUseCase.invoke(CachePolicy.CACHE_FIRST)
                .firstOrNull { it !is ApiResponse.Loading }

            val countries = (response as? ApiResponse.Success)?.data.orEmpty()
            val country = featuredCountrySelector.select(countries) ?: return Result.success()

            val deepLinkUri = Uri.parse(
                "https://worldcountries.vamsi.dev/country/${country.threeLetterCode.lowercase()}",
            )
            val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                setPackage(context.packageName)
                putExtra(EXTRA_COUNTRY_CODE, country.threeLetterCode)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                country.threeLetterCode.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_error)
                .setContentTitle("Country of the Day")
                .setContentText(country.name)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("${country.name} — ${country.capital}, ${country.region}"),
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            Timber.d("Posted country-of-day notification for ${country.name}")
            Result.success()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.e(e, "Failed to post country-of-day notification")
            Result.retry()
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily Country",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Daily country-of-the-day notification"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_COUNTRY_CODE = "extra_country_code"
        private const val CHANNEL_ID = "country_of_day_daily"
        private const val NOTIFICATION_ID = 9002
    }
}
