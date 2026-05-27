package com.vamsi.worldcountriesinformation.feature.settings

import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.core.common.error.AppError
import com.vamsi.worldcountriesinformation.core.common.error.toAppError
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIViewModel
import com.vamsi.worldcountriesinformation.core.datastore.CachePolicy
import com.vamsi.worldcountriesinformation.core.datastore.PreferencesDataSource
import com.vamsi.worldcountriesinformation.core.datastore.ThemeMode
import com.vamsi.worldcountriesinformation.domain.countries.CountriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Clock
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
    private val countriesRepository: CountriesRepository,
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
            preferencesDataSource.userPreferences
                .catch { /* defaults handled in data source */ }
                .collect { prefs ->
                    setState { copy(userPreferences = prefs) }
                }
        }
    }

    private fun updateCachePolicy(policy: CachePolicy) {
        viewModelScope.launch {
            try {
                preferencesDataSource.updateCachePolicy(policy)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun updateOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesDataSource.updateOfflineMode(enabled)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                preferencesDataSource.updateThemeMode(mode)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun updateUseDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesDataSource.updateUseDynamicColor(enabled)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun updateAiSummaryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesDataSource.updateAiSummaryEnabled(enabled)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun updateDailyNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesDataSource.updateDailyNotificationEnabled(enabled)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun updateMapBordersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesDataSource.updateMapBordersEnabled(enabled)
            } catch (e: IOException) {
                handlePreferenceError(e)
            }
        }
    }

    private fun clearCache() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                countriesRepository.clearCountryCache()
                try {
                    preferencesDataSource.updateLastCacheClear(clock.millis())
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
            try {
                val snapshot = countriesRepository.getCountryCacheSnapshot()
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
            } catch (e: android.database.sqlite.SQLiteException) {
                setState { copy(error = e.toAppError()) }
            }
        }
    }

    private fun handlePreferenceError(e: IOException) {
        val error = e.toAppError()
        setState { copy(error = error) }
        setEffect { SettingsContract.Effect.ShowError(error) }
    }
}
