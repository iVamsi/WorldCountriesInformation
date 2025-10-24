package com.vamsi.worldcountriesinformation.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore instance for user preferences.
 * Created as an extension property on Context for proper lifecycle management.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

/**
 * Data source for managing user preferences using DataStore.
 *
 * This class provides a type-safe interface for reading and writing user preferences.
 * All operations are coroutine-based and emit updates via Flow for reactive UI updates.
 *
 * Key features:
 * - Type-safe preference keys
 * - Automatic error handling with default fallbacks
 * - Flow-based reactive updates
 * - Singleton scope for consistent state across app
 *
 * @property context Application context for DataStore access
 *
 * @see UserPreferences
 * @see CachePolicy
 */
@Singleton
class PreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Preference keys for DataStore.
     * Private to ensure type safety and prevent external manipulation.
     */
    private object PreferencesKeys {
        val CACHE_POLICY = stringPreferencesKey("cache_policy")
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val LAST_CACHE_CLEAR = longPreferencesKey("last_cache_clear_timestamp")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    /**
     * Flow of user preferences.
     *
     * Emits the current preferences and any updates. Automatically handles
     * errors by emitting default preferences.
     *
     * Example usage:
     * ```kotlin
     * preferencesDataSource.userPreferences.collect { prefs ->
     *     println("Cache policy: ${prefs.cachePolicy}")
     * }
     * ```
     */
    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            // If there's an error reading preferences, emit default values
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapPreferences(preferences)
        }

    /**
     * Updates the cache policy preference.
     *
     * @param policy The new cache policy to save
     *
     * Example:
     * ```kotlin
     * preferencesDataSource.updateCachePolicy(CachePolicy.NETWORK_FIRST)
     * ```
     */
    suspend fun updateCachePolicy(policy: CachePolicy) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CACHE_POLICY] = policy.name
        }
    }

    /**
     * Updates the offline mode preference.
     *
     * @param enabled Whether offline mode should be enabled
     *
     * Example:
     * ```kotlin
     * preferencesDataSource.updateOfflineMode(true)
     * ```
     */
    suspend fun updateOfflineMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OFFLINE_MODE] = enabled
        }
    }

    /**
     * Updates the last cache clear timestamp.
     *
     * Call this when the user manually clears the cache.
     *
     * @param timestamp The timestamp in milliseconds (e.g., System.currentTimeMillis())
     *
     * Example:
     * ```kotlin
     * preferencesDataSource.updateLastCacheClear(System.currentTimeMillis())
     * ```
     */
    suspend fun updateLastCacheClear(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_CACHE_CLEAR] = timestamp
        }
    }

    /**
     * Updates the theme mode preference.
     *
     * Reserved for future theme switching implementation.
     *
     * @param mode The new theme mode to save
     */
    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    /**
     * Clears all preferences, resetting them to default values.
     *
     * Use with caution - this will reset all user settings.
     *
     * Example:
     * ```kotlin
     * preferencesDataSource.clearPreferences()
     * ```
     */
    suspend fun clearPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Maps raw DataStore preferences to UserPreferences data class.
     *
     * Provides default values for missing preferences and handles enum parsing.
     *
     * @param preferences The raw preferences from DataStore
     * @return UserPreferences with values from DataStore or defaults
     */
    private fun mapPreferences(preferences: Preferences): UserPreferences {
        val cachePolicyString = preferences[PreferencesKeys.CACHE_POLICY]
        val cachePolicy = try {
            cachePolicyString?.let { CachePolicy.valueOf(it) } ?: CachePolicy.CACHE_FIRST
        } catch (e: IllegalArgumentException) {
            // If stored value is invalid, use default
            CachePolicy.CACHE_FIRST
        }

        val themeModeString = preferences[PreferencesKeys.THEME_MODE]
        val themeMode = try {
            themeModeString?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }

        return UserPreferences(
            cachePolicy = cachePolicy,
            offlineMode = preferences[PreferencesKeys.OFFLINE_MODE] ?: false,
            lastCacheClearTimestamp = preferences[PreferencesKeys.LAST_CACHE_CLEAR] ?: 0L,
            themeMode = themeMode
        )
    }
}
