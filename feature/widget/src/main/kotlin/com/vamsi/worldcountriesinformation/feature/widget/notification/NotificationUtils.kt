package com.vamsi.worldcountriesinformation.feature.widget.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

internal fun Context.canPostNotifications(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return false
    }
    return NotificationManagerCompat.from(this).areNotificationsEnabled()
}

@SuppressLint("MissingPermission")
internal fun Context.notifyIfAllowed(notificationId: Int, notification: Notification): Boolean {
    if (!canPostNotifications()) return false
    NotificationManagerCompat.from(this).notify(notificationId, notification)
    return true
}
