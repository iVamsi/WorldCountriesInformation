package com.vamsi.worldcountriesinformation.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.core.datastore.CachePolicy
import com.vamsi.worldcountriesinformation.core.datastore.PreferencesDataSource
import com.vamsi.worldcountriesinformation.core.datastore.ThemeMode
import com.vamsi.worldcountriesinformation.core.datastore.UserPreferences
import com.vamsi.worldcountriesinformation.core.database.WorldCountriesDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 *
 * Manages user preferences and cache statistics. Provides state flows for
 * reactive UI updates and methods for updating preferences.
 *
 * Key features:
 * - Reactive preference updates via StateFlow
 * - Cache statistics calculation
 * - Cache clearing with timestamp tracking
 * - Integration with DataStore for persistence
 *
 * @property preferencesDataSource Data source for user preferences
 * @property database Database instance for cache statistics
 *
 * @see UserPreferences
 * @see PreferencesDataSource
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
    private val database: WorldCountriesDatabase
) : ViewModel() {

    /**
     * Current user preferences.
     *
     * Automatically updates when preferences change in DataStore.
     * UI should collect this flow to display current settings.
     *
     * Example:
     * ```kotlin
     * val preferences by viewModel.userPreferences.collectAsState()
     * Text("Cache Policy: ${preferences.cachePolicy}")
     * ```
     */
    val userPreferences: StateFlow<UserPreferences> = preferencesDataSource.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    /**
     * Cache statistics UI state.
     *
     * Contains calculated statistics about the cached data.
     */
    private val _cacheStats = MutableStateFlow(CacheStats())
    val cacheStats: StateFlow<CacheStats> = _cacheStats.asStateFlow()

    /**
     * Loading state for cache operations.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Error message state.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Load cache statistics on initialization
        loadCacheStatistics()
    }

    /**
     * Updates the cache policy preference.
     *
     * This will affect how the app fetches data in future requests.
     *
     * @param policy The new cache policy to apply
     *
     * Example:
     * ```kotlin
     * viewModel.updateCachePolicy(CachePolicy.NETWORK_FIRST)
     * ```
     */
    fun updateCachePolicy(policy: CachePolicy) {
        viewModelScope.launch {
            try {
                preferencesDataSource.updateCachePolicy(policy)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update cache policy: ${e.message}"
            }
        }
    }

    /**
     * Toggles offline mode on/off.
     *
     * When offline mode is enabled, the app will only use cached data
     * and not make any network requests.
     *
     * @param enabled Whether offline mode should be enabled
     *
     * Example:
     * ```kotlin
     * viewModel.updateOfflineMode(true)
     * ```
     */
    fun updateOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesDataSource.updateOfflineMode(enabled)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update offline mode: ${e.message}"
            }
        }
    }

    /**
     * Updates the theme mode preference.
     *
     * Reserved for future theme switching implementation.
     *
     * @param mode The new theme mode to apply
     */
    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                preferencesDataSource.updateThemeMode(mode)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update theme mode: ${e.message}"
            }
        }
    }

    /**
     * Clears all cached data from the database.
     *
     * This operation:
     * 1. Clears all countries from the database
     * 2. Updates the last cache clear timestamp
     * 3. Refreshes cache statistics
     *
     * Show a confirmation dialog before calling this method.
     *
     * Example:
     * ```kotlin
     * if (userConfirmed) {
     *     viewModel.clearCache()
     * }
     * ```
     */
    fun clearCache() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // Clear database
                database.countryDao().deleteAllCountries()
                
                // Update timestamp
                val timestamp = System.currentTimeMillis()
                preferencesDataSource.updateLastCacheClear(timestamp)
                
                // Reload statistics
                loadCacheStatistics()
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear cache: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Loads cache statistics from the database.
     *
     * Calculates:
     * - Number of cached countries
     * - Oldest cache entry age
     * - Estimated cache size
     *
     * Called automatically on initialization and after cache clearing.
     */
    fun loadCacheStatistics() {
        viewModelScope.launch {
            try {
                val countryCount = database.countryDao().getCountryCount()
                val oldestTimestamp = database.countryDao().getOldestTimestamp()
                
                val cacheAge = if (oldestTimestamp > 0) {
                    System.currentTimeMillis() - oldestTimestamp
                } else {
                    0L
                }
                
                // Rough estimate: ~2KB per country entry
                val estimatedSizeKB = countryCount * 2
                
                _cacheStats.value = CacheStats(
                    entryCount = countryCount,
                    oldestEntryAgeMs = cacheAge,
                    estimatedSizeKB = estimatedSizeKB
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cache statistics: ${e.message}"
            }
        }
    }

    /**
     * Clears the current error message.
     *
     * Call this after displaying an error to the user.
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

/**
 * Data class representing cache statistics.
 *
 * @property entryCount Number of cached entries (countries)
 * @property oldestEntryAgeMs Age of the oldest cache entry in milliseconds
 * @property estimatedSizeKB Estimated cache size in kilobytes
 */
data class CacheStats(
    val entryCount: Int = 0,
    val oldestEntryAgeMs: Long = 0L,
    val estimatedSizeKB: Int = 0
)
