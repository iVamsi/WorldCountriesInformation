package com.vamsi.worldcountriesinformation.feature.settings

import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.core.common.error.AppError
import com.vamsi.worldcountriesinformation.core.common.error.toAppError
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIViewModel
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.ClearCacheUseCase
import com.vamsi.worldcountriesinformation.domain.countries.GetCacheStatsUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.ThemeMode
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferencesPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Clock
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesPort: UserPreferencesPort,
    private val getCacheStatsUseCase: GetCacheStatsUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val clock: Clock,
) : MVIViewModel<SettingsContract.Intent, SettingsContract.State, SettingsContract.Effect>(
    initialState = SettingsContract.State(),
) {

    init {
        observePreferences()
        processIntent(SettingsContract.Intent.LoadCacheStats)
    }

    override fun handleIntent(intent: SettingsContract.Intent) {
        when (intent) {
            SettingsContract.Intent.LoadCacheStats -> loadCacheStatistics()
            is SettingsContract.Intent.UpdateCachePolicy -> updateCachePolicy(intent.policy)
            is SettingsContract.Intent.UpdateOfflineMode -> updateOfflineMode(intent.enabled)
            is SettingsContract.Intent.UpdateThemeMode -> updateThemeMode(intent.mode)
            is SettingsContract.Intent.UpdateUseDynamicColor -> updateUseDynamicColor(intent.enabled)
            is SettingsContract.Intent.UpdateAiSummaryEnabled -> updateAiSummaryEnabled(intent.enabled)
            is SettingsContract.Intent.UpdateDailyNotificationEnabled ->
                updateDailyNotificationEnabled(intent.enabled)
            is SettingsContract.Intent.UpdateMapBordersEnabled -> updateMapBordersEnabled(intent.enabled)
            SettingsContract.Intent.ClearCache -> clearCache()
            SettingsContract.Intent.ClearError -> setState { copy(error = null) }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferencesPort.userPreferences
                .catch { /* defaults handled in data source */ }
                .collect { prefs ->
                    setState { copy(userPreferences = prefs) }
                }
        }
    }

    private fun updateCachePolicy(policy: CachePolicy) {
        viewModelScope.launch {
            try {
                userPreferencesPort.updateCachePolicy(policy)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun updateOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesPort.updateOfflineMode(enabled)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                userPreferencesPort.updateThemeMode(mode)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun updateUseDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesPort.updateUseDynamicColor(enabled)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun updateAiSummaryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesPort.updateAiSummaryEnabled(enabled)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun updateDailyNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesPort.updateDailyNotificationEnabled(enabled)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun updateMapBordersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesPort.updateMapBordersEnabled(enabled)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun clearCache() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                when (val result = clearCacheUseCase(Unit)) {
                    is ApiResponse.Error -> throw result.exception
                    is ApiResponse.Loading -> Unit
                    is ApiResponse.Success -> Unit
                }
                try {
                    userPreferencesPort.updateLastCacheClear(clock.millis())
                } catch (e: IOException) {
                    setState {
                        copy(
                            error = AppError.Generic(
                                com.vamsi.worldcountriesinformation.core.common.R.string.error_unknown,
                            ),
                        )
                    }
                }
                loadCacheStatistics()
            } catch (e: android.database.sqlite.SQLiteException) {
                setState { copy(error = e.toAppError()) }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun loadCacheStatistics() {
        viewModelScope.launch {
            when (val result = getCacheStatsUseCase(Unit)) {
                is ApiResponse.Success -> {
                    val snapshot = result.data
                    val countryCount = snapshot.entryCount
                    val oldestTimestamp = snapshot.oldestEntryLastUpdatedMs
                    val cacheAge = if (oldestTimestamp > 0) clock.millis() - oldestTimestamp else 0L
                    setState {
                        copy(
                            cacheStats = CacheStats(
                                entryCount = countryCount,
                                oldestEntryAgeMs = cacheAge,
                                estimatedSizeKB = countryCount * 2,
                            ),
                        )
                    }
                }
                is ApiResponse.Error -> setState { copy(error = result.exception.toAppError()) }
                is ApiResponse.Loading -> Unit
            }
        }
    }

    private fun handlePreferenceError(e: IOException) {
        val error = e.toAppError()
        setState { copy(error = error) }
        setEffect { SettingsContract.Effect.ShowError(error) }
    }
}
