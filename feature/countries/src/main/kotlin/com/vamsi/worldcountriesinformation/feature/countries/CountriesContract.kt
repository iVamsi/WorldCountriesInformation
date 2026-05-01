package com.vamsi.worldcountriesinformation.feature.countries

import com.vamsi.worldcountriesinformation.core.common.error.AppError
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIEffect
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIIntent
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIState
import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import com.vamsi.worldcountriesinformation.domainmodel.RecentlyViewedEntry
import com.vamsi.worldcountriesinformation.domainmodel.SearchHistoryEntry
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

        /**
         * User tapped a search history item.
         */
        data class SearchHistoryItemSelected(val query: String) : Intent

        /**
         * User tapped a search suggestion item.
         */
        data class SearchSuggestionSelected(val suggestion: String) : Intent

        /**
         * User dismissed a history entry via swipe delete.
         */
        data class DeleteSearchHistoryItem(val query: String) : Intent

        /**
         * User cleared the entire history list.
         */
        data object ClearSearchHistory : Intent

        /**
         * User toggled compare-selection mode (e.g. via long-press).
         */
        data object ToggleSelectionMode : Intent

        /**
         * User added or removed a country from the current compare selection.
         */
        data class ToggleCompareSelection(val countryCode: String) : Intent

        /**
         * User cleared the active compare selection.
         */
        data object ClearCompareSelection : Intent

        /**
         * User confirmed the current compare selection.
         */
        data object ConfirmCompare : Intent
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
        val countries: List<CountrySummary> = emptyList(),
        val filteredCountries: List<CountrySummary> = emptyList(),

        // Search
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
        val isSearchFocused: Boolean = false,
        val searchHistory: List<SearchHistoryEntry> = emptyList(),
        val searchSuggestions: List<String> = emptyList(),

        // Recently viewed
        val recentlyViewedEntries: List<RecentlyViewedEntry> = emptyList(),
        val recentlyViewedCountries: List<CountrySummary> = emptyList(),

        // Filters
        val selectedRegions: Set<String> = emptySet(),
        val sortOrder: SortOrder = SortOrder.NAME_ASC,

        // Favorites
        val favoriteCountryCodes: Set<String> = emptySet(),

        // Error state — UI translates to a localized string via Context.message(error)
        val error: AppError? = null,

        // Cache info
        val lastUpdated: Long = 0L,

        // Compare-selection mode
        val isSelecting: Boolean = false,
        val compareSelection: List<String> = emptyList(),
    ) : MVIState {

        /**
         * True when at least 2 (and at most [MAX_COMPARE]) countries are selected.
         */
        val canConfirmCompare: Boolean
            get() = compareSelection.size in MIN_COMPARE..MAX_COMPARE

        /**
         * True when the cap has been hit and additional selections will be ignored.
         */
        val compareSelectionAtCap: Boolean
            get() = compareSelection.size >= MAX_COMPARE

        companion object {
            const val MIN_COMPARE = 2
            const val MAX_COMPARE = 3
        }

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
            get() = countries.isNotEmpty() && !isLoading && error == null

        /**
         * True if should show empty search results.
         */
        val showEmptySearchResults: Boolean
            get() = hasData && (isSearchActive || hasActiveFilters) && filteredCountries.isEmpty()

        /**
         * True if should show error state.
         */
        val showError: Boolean
            get() = error != null && !isLoading

        /**
         * True when search history panel should be visible.
         */
        val shouldShowSearchHistory: Boolean
            get() = searchHistory.isNotEmpty() && searchQuery.isBlank() && isSearchFocused

        /**
         * True when search suggestions panel should be visible.
         */
        val shouldShowSearchSuggestions: Boolean
            get() = searchSuggestions.isNotEmpty() && isSearchFocused && searchQuery.isNotBlank()
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
         * Show an error snackbar. The UI layer localizes the error.
         */
        data class ShowError(val error: AppError) : Effect

        /**
         * Show success message.
         */
        data class ShowSuccess(val message: String) : Effect

        /**
         * Navigate to the compare screen with the selected three-letter codes.
         */
        data class NavigateToCompare(val codes: List<String>) : Effect
    }
}
