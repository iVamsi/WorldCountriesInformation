package com.vamsi.worldcountriesinformation.feature.countrydetails

import androidx.annotation.StringRes
import com.vamsi.worldcountriesinformation.core.common.error.AppError
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIEffect
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIIntent
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIState
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary

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

        /**
         * User clicked share button to share country information.
         */
        data object ShareCountry : Intent

        /**
         * User clicked "Open in Maps" to view the country location externally.
         */
        data object OpenInMaps : Intent

        /**
         * User clicked on a nearby country to navigate to its details.
         */
        data class NearbyCountryClicked(val countryCode: String) : Intent
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

        // Nearby countries
        val nearbyCountries: List<CountrySummary> = emptyList(),
        val isLoadingNearby: Boolean = false,

        // Favorite state
        val isFavorite: Boolean = false,

        // Error state — UI translates to a localized string via Context.message(error)
        val error: AppError? = null,

        // Cache info
        val lastUpdated: Long = 0L,
    ) : MVIState {

        /**
         * True if showing data (not loading or error).
         */
        val hasData: Boolean
            get() = country != null && !isLoading && error == null

        /**
         * True if should show error state.
         */
        val showError: Boolean
            get() = error != null && !isLoading

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
         * Show a localized one-time message via a string resource.
         */
        data class ShowMessage(
            @get:StringRes val messageRes: Int,
            val formatArgs: List<Any> = emptyList(),
        ) : Effect

        /**
         * Show an error snackbar. The UI layer localizes the error.
         *
         * Either an [AppError] (preferred) or an explicit string resource
         * with positional format args. Use the resource form for transient
         * UI errors that don't fit the AppError taxonomy.
         */
        data class ShowError(
            val error: AppError? = null,
            @get:StringRes val messageRes: Int? = null,
            val formatArgs: List<Any> = emptyList(),
        ) : Effect {
            init {
                require(error != null || messageRes != null) {
                    "ShowError requires either an AppError or a messageRes"
                }
            }
        }

        /**
         * Show success message.
         */
        data class ShowSuccess(val message: String) : Effect

        /**
         * Launch the Android share sheet with formatted country information.
         */
        data class ShareCountryCard(val shareText: String) : Effect

        /**
         * Launch an external maps application at the country's coordinates.
         */
        data class OpenInMaps(
            val latitude: Double,
            val longitude: Double,
            val countryName: String,
        ) : Effect

        /**
         * Navigate to another country's details screen.
         */
        data class NavigateToCountryDetails(val countryCode: String) : Effect
    }
}
