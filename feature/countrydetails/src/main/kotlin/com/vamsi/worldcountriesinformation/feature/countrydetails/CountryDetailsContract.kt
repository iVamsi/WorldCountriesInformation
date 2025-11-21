package com.vamsi.worldcountriesinformation.feature.countrydetails

import com.vamsi.worldcountriesinformation.core.common.mvi.MVIEffect
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIIntent
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIState
import com.vamsi.worldcountriesinformation.domainmodel.Country

/**
 * MVI Contract for Country Details feature.
 *
 * Defines all possible user interactions (Intents), UI state (State),
 * and one-time events (Effects) for the Country Details screen.
 */
object CountryDetailsContract {

    /**
     * Represents all possible user actions in the Country Details screen.
     */
    sealed interface Intent : MVIIntent {
        /**
         * Load country details for a specific country code.
         */
        data class LoadCountryDetails(val countryCode: String) : Intent

        /**
         * Retry loading after an error.
         */
        data class RetryLoading(val countryCode: String) : Intent

        /**
         * User triggered pull-to-refresh.
         */
        data class RefreshCountry(val countryCode: String) : Intent

        /**
         * User toggled favorite status.
         */
        data object ToggleFavorite : Intent

        /**
         * User clicked back button.
         */
        data object NavigateBack : Intent

        /**
         * Error message was shown (acknowledged).
         */
        data object ErrorShown : Intent
    }

    /**
     * Complete UI state for the Country Details screen.
     *
     * This single state object contains all data needed to render the UI.
     */
    data class State(
        // Loading states
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,

        // Data
        val country: Country? = null,

        // Favorite state
        val isFavorite: Boolean = false,

        // Error state
        val errorMessage: String? = null,

        // Cache info
        val lastUpdated: Long = 0L,
    ) : MVIState {

        /**
         * True if showing data (not loading or error).
         */
        val hasData: Boolean
            get() = country != null && !isLoading && errorMessage == null

        /**
         * True if should show error state.
         */
        val showError: Boolean
            get() = errorMessage != null && !isLoading

        /**
         * True if showing initial loading state.
         */
        val showLoading: Boolean
            get() = isLoading && country == null
    }

    /**
     * One-time events that don't belong in state.
     *
     * Effects are consumed once and trigger UI actions like navigation or toasts.
     */
    sealed interface Effect : MVIEffect {
        /**
         * Navigate back to previous screen.
         */
        data object NavigateBack : Effect

        /**
         * Show a toast message.
         */
        data class ShowToast(val message: String) : Effect

        /**
         * Show an error snackbar.
         */
        data class ShowError(val message: String) : Effect

        /**
         * Show success message.
         */
        data class ShowSuccess(val message: String) : Effect
    }
}
