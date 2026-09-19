package com.vamsi.worldcountriesinformation.data.countries.repository

import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.preferences.RefreshInterval
import com.vamsi.worldcountriesinformation.domain.preferences.ThemeMode
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferences
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferencesPort
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory preferences for repository tests. */
class FakeUserPreferencesPort(
    initial: UserPreferences = UserPreferences(),
) : UserPreferencesPort {
    override val userPreferences = MutableStateFlow(initial)

    override suspend fun updateCachePolicy(policy: CachePolicy) = update { copy(cachePolicy = policy) }
    override suspend fun updateOfflineMode(enabled: Boolean) = update { copy(offlineMode = enabled) }
    override suspend fun updateLastCacheClear(timestamp: Long) = update { copy(lastCacheClearTimestamp = timestamp) }
    override suspend fun updateThemeMode(mode: ThemeMode) = update { copy(themeMode = mode) }
    override suspend fun updateUseDynamicColor(enabled: Boolean) = update { copy(useDynamicColor = enabled) }
    override suspend fun updateAiSummaryEnabled(enabled: Boolean) = update { copy(aiSummaryEnabled = enabled) }
    override suspend fun updateDailyNotificationEnabled(enabled: Boolean) = update { copy(dailyNotificationEnabled = enabled) }
    override suspend fun updateMapBordersEnabled(enabled: Boolean) = update { copy(showMapBorders = enabled) }
    override suspend fun updateRefreshInterval(interval: RefreshInterval) = update { copy(refreshInterval = interval) }
    override suspend fun toggleFavorite(countryCode: String) = update {
        val code = countryCode.uppercase()
        copy(favoriteCountryCodes = if (code in favoriteCountryCodes) favoriteCountryCodes - code else favoriteCountryCodes + code)
    }
    override suspend fun clearPreferences() = update { UserPreferences() }

    private fun update(block: UserPreferences.() -> UserPreferences) {
        userPreferences.value = userPreferences.value.block()
    }
}
