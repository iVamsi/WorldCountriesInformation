package com.vamsi.worldcountriesinformation.feature.countries

import com.vamsi.worldcountriesinformation.core.common.mvi.MVIEffect
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIIntent
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIState
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder

/**
 * MVI Contract for Countries feature.
 *
 * Defines all possible user interactions (Intents), UI state (State),
 * and one-time events (Effects) for the Countries screen.
 */
object CountriesContract {

    // ============================================================================
    // INTENTS - User Actions & Events
    // ============================================================================

    /**
     * Represents all possible user actions in the Countries screen.
     */
    sealed interface Intent : MVIIntent {
        /**
         * Load countries from cache/network.
         */
        data object LoadCountries : Intent

        /**
         * Retry loading after an error.
         */
        data object RetryLoading : Intent

        /**
         * User changed search query.
         */
        data class SearchQueryChanged(val query: String) : Intent

        /**
         * User cleared the search.
         */
        data object ClearSearch : Intent

        /**
         * User toggled a region filter.
         */
        data class ToggleRegion(val region: String) : Intent

        /**
         * User changed sort order.
         */
        data class ChangeSortOrder(val sortOrder: SortOrder) : Intent

        /**
         * User cleared all filters.
         */
        data object ClearFilters : Intent

        /**
         * User clicked on a country.
         */
        data class CountryClicked(val countryCode: String) : Intent

        /**
         * User toggled favorite status of a country.
         */
        data class ToggleFavorite(val countryCode: String) : Intent

        /**
         * User triggered pull-to-refresh.
         */
        data object RefreshCountries : Intent

        /**
         * Error message was shown (acknowledged).
         */
        data object ErrorShown : Intent

        /**
         * Search field focus changed (for analytics/UX state).
         */
        data class SearchFocusChanged(val isFocused: Boolean) : Intent

        /**
         * User pressed the search back button.
         */
        data object SearchBackPressed : Intent
    }

    // ============================================================================
    // STATE - Complete UI State
    // ============================================================================

    /**
     * Complete UI state for the Countries screen.
     *
     * This single state object contains all data needed to render the UI.
     */
    data class State(
        // Loading states
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,

        // Data
        val countries: List<Country> = emptyList(),
        val filteredCountries: List<Country> = emptyList(),

        // Search
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,

        // Filters
        val selectedRegions: Set<String> = emptySet(),
        val sortOrder: SortOrder = SortOrder.NAME_ASC,

        // Favorites
        val favoriteCountryCodes: Set<String> = emptySet(),

        // Error state
        val errorMessage: String? = null,

        // Cache info
        val lastUpdated: Long = 0L,
    ) : MVIState {

        /**
         * True if there are any active filters.
         */
        val hasActiveFilters: Boolean
            get() = selectedRegions.isNotEmpty() || sortOrder != SortOrder.NAME_ASC

        /**
         * Count of active filters.
         */
        val activeFilterCount: Int
            get() = selectedRegions.size + if (sortOrder != SortOrder.NAME_ASC) 1 else 0

        /**
         * True if showing data (not loading or error).
         */
        val hasData: Boolean
            get() = countries.isNotEmpty() && !isLoading && errorMessage == null

        /**
         * True if should show empty search results.
         */
        val showEmptySearchResults: Boolean
            get() = hasData && (isSearchActive || hasActiveFilters) && filteredCountries.isEmpty()

        /**
         * True if should show error state.
         */
        val showError: Boolean
            get() = errorMessage != null && !isLoading
    }

    // ============================================================================
    // EFFECTS - One-Time Side Effects
    // ============================================================================

    /**
     * One-time events that don't belong in state.
     *
     * Effects are consumed once and trigger UI actions like navigation or toasts.
     */
    sealed interface Effect : MVIEffect {
        /**
         * Navigate to country details screen.
         */
        data class NavigateToDetails(val countryCode: String) : Effect

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
