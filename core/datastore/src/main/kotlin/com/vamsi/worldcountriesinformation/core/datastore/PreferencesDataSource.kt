package com.vamsi.worldcountriesinformation.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.preferences.ThemeMode
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferences
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferencesPort
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences",
)

@Singleton
class PreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesPort {

    private object PreferencesKeys {
        val CACHE_POLICY = stringPreferencesKey("cache_policy")
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val LAST_CACHE_CLEAR = longPreferencesKey("last_cache_clear_timestamp")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val AI_SUMMARY_ENABLED = booleanPreferencesKey("ai_summary_enabled")
        val DAILY_NOTIFICATION_ENABLED = booleanPreferencesKey("daily_notification_enabled")
        val SHOW_MAP_BORDERS = booleanPreferencesKey("show_map_borders")
        val FAVORITE_COUNTRY_CODES = stringSetPreferencesKey("favorite_country_codes")
    }

    override val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map(::mapPreferences)

    override suspend fun updateCachePolicy(policy: CachePolicy) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CACHE_POLICY] = policy.name
        }
    }

    override suspend fun updateOfflineMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OFFLINE_MODE] = enabled
        }
    }

    override suspend fun updateLastCacheClear(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_CACHE_CLEAR] = timestamp
        }
    }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    override suspend fun updateUseDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLOR] = enabled
        }
    }

    override suspend fun updateAiSummaryEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_SUMMARY_ENABLED] = enabled
        }
    }

    override suspend fun updateDailyNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_NOTIFICATION_ENABLED] = enabled
        }
    }

    override suspend fun updateMapBordersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_MAP_BORDERS] = enabled
        }
    }

    override suspend fun toggleFavorite(countryCode: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.FAVORITE_COUNTRY_CODES].orEmpty()
            val normalized = countryCode.uppercase()
            preferences[PreferencesKeys.FAVORITE_COUNTRY_CODES] = if (normalized in current) {
                current - normalized
            } else {
                current + normalized
            }
        }
    }

    override suspend fun clearPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun mapPreferences(preferences: Preferences): UserPreferences {
        val cachePolicy = parseCachePolicy(preferences[PreferencesKeys.CACHE_POLICY])
        val themeMode = parseThemeMode(preferences[PreferencesKeys.THEME_MODE])

        return UserPreferences(
            cachePolicy = cachePolicy,
            offlineMode = preferences[PreferencesKeys.OFFLINE_MODE] ?: false,
            lastCacheClearTimestamp = preferences[PreferencesKeys.LAST_CACHE_CLEAR] ?: 0L,
            themeMode = themeMode,
            useDynamicColor = preferences[PreferencesKeys.USE_DYNAMIC_COLOR] ?: true,
            aiSummaryEnabled = preferences[PreferencesKeys.AI_SUMMARY_ENABLED] ?: false,
            dailyNotificationEnabled = preferences[PreferencesKeys.DAILY_NOTIFICATION_ENABLED] ?: false,
            showMapBorders = preferences[PreferencesKeys.SHOW_MAP_BORDERS] ?: true,
            favoriteCountryCodes = preferences[PreferencesKeys.FAVORITE_COUNTRY_CODES]
                .orEmpty()
                .map { it.uppercase() }
                .toSet(),
        )
    }

    private fun parseCachePolicy(raw: String?): CachePolicy {
        if (raw == null) return CachePolicy.CACHE_FIRST
        if (raw == LEGACY_NETWORK_ONLY) return CachePolicy.FORCE_REFRESH
        return try {
            CachePolicy.valueOf(raw)
        } catch (_: IllegalArgumentException) {
            CachePolicy.CACHE_FIRST
        }
    }

    private fun parseThemeMode(raw: String?): ThemeMode {
        if (raw == null) return ThemeMode.SYSTEM
        return try {
            ThemeMode.valueOf(raw)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    private companion object {
        /** Legacy DataStore value before domain [CachePolicy] unification. */
        const val LEGACY_NETWORK_ONLY = "NETWORK_ONLY"
    }
}
