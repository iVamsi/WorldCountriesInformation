package com.vamsi.worldcountriesinformation.feature.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import timber.log.Timber

/**
 * Glance Widget Receiver for Country Widget
 * Handles widget updates and lifecycle events
 */
class CountryWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CountryWidget()

    override fun onEnabled(context: android.content.Context) {
        super.onEnabled(context)
        Timber.d("Country Widget enabled")

        // Schedule periodic updates using WorkManager
        WidgetUpdateWorker.schedulePeriodicUpdates(context)
    }

    override fun onDisabled(context: android.content.Context) {
        super.onDisabled(context)
        Timber.d("Country Widget disabled")

        // Cancel periodic updates when last widget is removed
        WidgetUpdateWorker.cancelPeriodicUpdates(context)
    }

    override fun onDeleted(context: android.content.Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Timber.d("Country Widget deleted: ${appWidgetIds.contentToString()}")
    }
}

