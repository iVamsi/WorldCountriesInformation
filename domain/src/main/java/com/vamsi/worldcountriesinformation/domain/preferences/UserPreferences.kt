package com.vamsi.worldcountriesinformation.domain.preferences

import com.vamsi.worldcountriesinformation.domain.core.CachePolicy

data class UserPreferences(
    val cachePolicy: CachePolicy = CachePolicy.CACHE_FIRST,
    val offlineMode: Boolean = false,
    val lastCacheClearTimestamp: Long = 0L,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val aiSummaryEnabled: Boolean = false,
    val dailyNotificationEnabled: Boolean = false,
    val showMapBorders: Boolean = true,
    val favoriteCountryCodes: Set<String> = emptySet(),
)
