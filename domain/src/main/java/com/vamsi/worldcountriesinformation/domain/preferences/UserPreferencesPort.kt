package com.vamsi.worldcountriesinformation.domain.preferences

import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import kotlinx.coroutines.flow.Flow

interface UserPreferencesPort {
    val userPreferences: Flow<UserPreferences>

    suspend fun updateCachePolicy(policy: CachePolicy)

    suspend fun updateOfflineMode(enabled: Boolean)

    suspend fun updateLastCacheClear(timestamp: Long)

    suspend fun updateThemeMode(mode: ThemeMode)

    suspend fun updateUseDynamicColor(enabled: Boolean)

    suspend fun updateAiSummaryEnabled(enabled: Boolean)

    suspend fun updateDailyNotificationEnabled(enabled: Boolean)

    suspend fun updateMapBordersEnabled(enabled: Boolean)

    suspend fun toggleFavorite(countryCode: String)

    suspend fun clearPreferences()
}
