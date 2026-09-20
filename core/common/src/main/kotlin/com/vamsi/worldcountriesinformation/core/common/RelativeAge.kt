package com.vamsi.worldcountriesinformation.core.common

import android.content.res.Resources
import java.util.concurrent.TimeUnit

/** "3 days ago", "2 hours ago", "Just now": how long ago something happened, in the user's language. */
fun Resources.relativeAge(ageMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ageMs)
    val hours = TimeUnit.MILLISECONDS.toHours(ageMs)
    val days = TimeUnit.MILLISECONDS.toDays(ageMs)
    return when {
        days > 0 -> getQuantityString(R.plurals.age_days, days.toInt(), days)
        hours > 0 -> getQuantityString(R.plurals.age_hours, hours.toInt(), hours)
        minutes > 0 -> getQuantityString(R.plurals.age_minutes, minutes.toInt(), minutes)
        else -> getString(R.string.age_just_now)
    }
}
