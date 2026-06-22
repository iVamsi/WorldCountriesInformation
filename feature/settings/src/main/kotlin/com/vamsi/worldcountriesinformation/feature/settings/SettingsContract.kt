package com.vamsi.worldcountriesinformation.feature.settings

import com.vamsi.worldcountriesinformation.core.common.error.AppError
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIEffect
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIIntent
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIState
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.preferences.ThemeMode
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferences

object SettingsContract {

    sealed interface Intent : MVIIntent {
        data object LoadCacheStats : Intent
        data class UpdateCachePolicy(val policy: CachePolicy) : Intent
        data class UpdateOfflineMode(val enabled: Boolean) : Intent
        data class UpdateThemeMode(val mode: ThemeMode) : Intent
        data class UpdateUseDynamicColor(val enabled: Boolean) : Intent
        data class UpdateAiSummaryEnabled(val enabled: Boolean) : Intent
        data class UpdateDailyNotificationEnabled(val enabled: Boolean) : Intent
        data class UpdateMapBordersEnabled(val enabled: Boolean) : Intent
        data object ClearCache : Intent
        data object ClearError : Intent
    }

    data class State(
        val userPreferences: UserPreferences = UserPreferences(),
        val cacheStats: CacheStats = CacheStats(),
        val isLoading: Boolean = false,
        val error: AppError? = null,
    ) : MVIState {
        val errorMessageRes: Int?
            get() = error?.messageRes
    }

    sealed interface Effect : MVIEffect {
        data class ShowError(val error: AppError) : Effect
    }
}

data class CacheStats(
    val entryCount: Int = 0,
    val oldestEntryAgeMs: Long = 0L,
    val estimatedSizeKB: Int = 0,
)
