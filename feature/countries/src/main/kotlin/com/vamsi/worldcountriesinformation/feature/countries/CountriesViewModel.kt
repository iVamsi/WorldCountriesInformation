package com.vamsi.worldcountriesinformation.feature.countries

import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIViewModel
import com.vamsi.worldcountriesinformation.core.datastore.SearchPreferences
import com.vamsi.worldcountriesinformation.core.datastore.SearchPreferencesDataSource
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.core.onError
import com.vamsi.worldcountriesinformation.domain.core.onLoading
import com.vamsi.worldcountriesinformation.domain.core.onSuccess
import com.vamsi.worldcountriesinformation.domain.countries.FilteredSearchCountriesUseCase
import com.vamsi.worldcountriesinformation.domain.countries.GenerateSearchSuggestionsUseCase
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.domain.countries.SearchCountriesUseCase
import com.vamsi.worldcountriesinformation.domain.search.SearchFiltersUseCase
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.RecentlyViewedEntry
import com.vamsi.worldcountriesinformation.domainmodel.SearchFilters
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Central state holder for the countries experience.
 *
 * Reacts to user intents, coordinates domain use cases, and emits a single immutable state
 * stream for the UI. Supports cache policies, advanced search, filters, suggestions, and
 * persisted preferences.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val getCountriesUseCase: GetCountriesUseCase,
    private val searchCountriesUseCase: SearchCountriesUseCase,
    private val filteredSearchUseCase: FilteredSearchCountriesUseCase,
    private val suggestionsUseCase: GenerateSearchSuggestionsUseCase,
    private val searchPreferencesDataSource: SearchPreferencesDataSource,
    private val searchFiltersUseCase: SearchFiltersUseCase,
) : MVIViewModel<CountriesContract.Intent, CountriesContract.State, CountriesContract.Effect>(
    initialState = CountriesContract.State()
) {
    private val searchPreferencesFlow = searchPreferencesDataSource.searchPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = SearchPreferences()
        )

    private val searchSuggestionsFlow: StateFlow<List<String>> = combine(
        state,
        state
    ) { currentState, _ ->
        val query = currentState.searchQuery
        val countries = currentState.countries
        if (query.length >= 2 && countries.isNotEmpty()) {
            suggestionsUseCase(
                query = query,
                allCountries = countries,
                maxSuggestions = 5
            )
        } else {
            emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    init {
        // Load countries on initialization
        processIntent(CountriesContract.Intent.LoadCountries)

        // Observe search preferences and update state
        viewModelScope.launch {
            searchPreferencesFlow.collect { prefs ->
                setState {
                    val recentCountries = mapRecentlyViewedCountries(
                        entries = prefs.recentlyViewed,
                        countries = countries
                    )
                    copy(
                        selectedRegions = prefs.filters.selectedRegions,
                        sortOrder = prefs.filters.sortOrder,
                        filteredCountries = applyFiltersAndSort(countries, prefs.filters),
                        searchHistory = prefs.searchHistory,
                        recentlyViewedEntries = prefs.recentlyViewed,
                        recentlyViewedCountries = recentCountries
                    )
                }
            }
        }

        // Observe search suggestions and update state
        viewModelScope.launch {
            searchSuggestionsFlow.collect { suggestions ->
                setState {
                    copy(searchSuggestions = suggestions)
                }
            }
        }
    }

    override fun handleIntent(intent: CountriesContract.Intent) {
        when (intent) {
            is CountriesContract.Intent.LoadCountries -> loadCountries()
            is CountriesContract.Intent.RetryLoading -> retry()
            is CountriesContract.Intent.SearchQueryChanged -> onSearchQueryChange(intent.query)
            is CountriesContract.Intent.ClearSearch -> clearSearch()
            is CountriesContract.Intent.ToggleRegion -> toggleRegion(intent.region)
            is CountriesContract.Intent.ChangeSortOrder -> updateSortOrder(intent.sortOrder)
            is CountriesContract.Intent.ClearFilters -> clearFilters()
            is CountriesContract.Intent.CountryClicked -> navigateToDetails(intent.countryCode)
            is CountriesContract.Intent.ToggleFavorite -> toggleFavorite(intent.countryCode)
            is CountriesContract.Intent.RefreshCountries -> refresh()
            is CountriesContract.Intent.ErrorShown -> clearError()
            is CountriesContract.Intent.SearchFocusChanged -> onSearchFocusChanged(intent.isFocused)
            is CountriesContract.Intent.SearchBackPressed -> onSearchBackPressed()
            is CountriesContract.Intent.SearchHistoryItemSelected -> onSearchHistoryItemSelected(intent.query)
            is CountriesContract.Intent.SearchSuggestionSelected -> selectSearchQuery(intent.suggestion)
            is CountriesContract.Intent.DeleteSearchHistoryItem -> deleteSearchHistoryEntry(intent.query)
            is CountriesContract.Intent.ClearSearchHistory -> clearSearchHistory()
        }
    }

    /**
     * Loads countries with configurable cache policy.
     */
    private fun loadCountries(policy: CachePolicy = CachePolicy.CACHE_FIRST) {
        viewModelScope.launch {
            Timber.d("Loading countries with policy: $policy")

            getCountriesUseCase(policy)
                .catch { exception ->
                    Timber.e(exception, "Unexpected error loading countries")
                    setState {
                        copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = "Failed to load countries. Please try again."
                        )
                    }
                    setEffect { CountriesContract.Effect.ShowError("Failed to load countries. Please try again.") }
                }
                .collect { response ->
                    response
                        .onLoading {
                            Timber.d("Countries loading...")
                            if (!state.value.isRefreshing) {
                                setState { copy(isLoading = true) }
                            }
                        }
                        .onSuccess { countries ->
                            Timber.d("Countries loaded: ${countries.size} countries")
                            val currentFilters = searchPreferencesFlow.value.filters
                            val recentlyViewed = searchPreferencesFlow.value.recentlyViewed
                            setState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    countries = countries,
                                    filteredCountries = applyFiltersAndSort(countries, currentFilters),
                                    recentlyViewedCountries = mapRecentlyViewedCountries(
                                        entries = recentlyViewed,
                                        countries = countries
                                    ),
                                    lastUpdated = System.currentTimeMillis(),
                                    errorMessage = null
                                )
                            }
                        }
                        .onError { exception ->
                            Timber.e(exception, "Error loading countries")
                            val errorMessage = when {
                                exception.message?.contains("No cached data", ignoreCase = true) == true -> {
                                    "No cached data available. Please connect to the internet."
                                }

                                exception.message?.contains("timeout", ignoreCase = true) == true -> {
                                    "Connection timeout. Please check your internet and try again."
                                }

                                exception.message?.contains("network", ignoreCase = true) == true -> {
                                    "Network error. Please check your connection and try again."
                                }

                                else -> {
                                    "Failed to load countries. Please try again."
                                }
                            }

                            setState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    errorMessage = errorMessage
                                )
                            }
                            setEffect { CountriesContract.Effect.ShowError(errorMessage) }
                        }
                }
        }
    }

    /**
     * Refreshes countries data from network (pull-to-refresh).
     */
    private fun refresh() {
        Timber.d("Refresh requested by user")
        setState { copy(isRefreshing = true) }
        loadCountries(CachePolicy.FORCE_REFRESH)
    }

    /**
     * Retries loading countries after an error.
     */
    private fun retry() {
        Timber.d("Retry requested")
        loadCountries(CachePolicy.CACHE_FIRST)
    }

    /**
     * Updates the search query with debouncing.
     */
    private fun onSearchQueryChange(query: String) {
        Timber.d("Search query changed: $query")
        setState {
            val currentlyFocused = isSearchFocused
            copy(
                searchQuery = query,
                isSearchActive = query.isNotBlank() || currentlyFocused
            )
        }

        // Trigger search with debouncing
        viewModelScope.launch {
            delay(300L)
            if (state.value.searchQuery == query) {
                performSearch(query)
            }
        }
    }

    /**
     * Performs the actual search operation.
     */
    private fun performSearch(query: String) {
        viewModelScope.launch {
            val currentFilters = searchPreferencesFlow.value.filters

            if (query.isBlank()) {
                // Empty query: show all countries with filters applied
                setState {
                    copy(filteredCountries = applyFiltersAndSort(countries, currentFilters))
                }
            } else {
                // Non-empty query: search with filters
                if (searchFiltersUseCase.hasActiveFilters(currentFilters)) {
                    // Use filtered search
                    filteredSearchUseCase(query, currentFilters)
                        .catch { exception ->
                            Timber.e(exception, "Search error for query: $query")
                        }
                        .collect { results ->
                            setState { copy(filteredCountries = results) }
                        }
                } else {
                    // Use basic search
                    searchCountriesUseCase(query)
                        .catch { exception ->
                            Timber.e(exception, "Search error for query: $query")
                        }
                        .collect { results ->
                            setState { copy(filteredCountries = results) }
                        }
                }
            }
        }
    }

    /**
     * Clear search query.
     */
    private fun clearSearch() {
        Timber.d("Clearing search query")
        setState {
            val currentFilters = searchPreferencesFlow.value.filters
            val focused = isSearchFocused
            copy(
                searchQuery = "",
                isSearchActive = focused,
                filteredCountries = applyFiltersAndSort(countries, currentFilters)
            )
        }
    }

    private fun onSearchFocusChanged(isFocused: Boolean) {
        val focusLabel = if (isFocused) "focused" else "unfocused"
        Timber.tag("Analytics").i("search_focus_$focusLabel")
        setState {
            val shouldBeActive = if (isFocused) {
                true
            } else {
                searchQuery.isNotBlank()
            }
            copy(
                isSearchFocused = isFocused,
                isSearchActive = shouldBeActive
            )
        }
    }

    private fun onSearchBackPressed() {
        Timber.tag("Analytics").i(
            "search_back_pressed query_length=${state.value.searchQuery.length}"
        )
    }

    /**
     * Toggle region filter.
     */
    private fun toggleRegion(region: String) {
        viewModelScope.launch {
            val currentRegions = searchPreferencesFlow.value.filters.selectedRegions
            val newRegions = if (currentRegions.contains(region)) {
                currentRegions - region
            } else {
                currentRegions + region
            }
            Timber.d("Toggling region $region. New regions: $newRegions")
            searchPreferencesDataSource.updateSelectedRegions(newRegions)
        }
    }

    /**
     * Updates sort order.
     */
    private fun updateSortOrder(sortOrder: SortOrder) {
        viewModelScope.launch {
            Timber.d("Updating sort order: $sortOrder")
            searchPreferencesDataSource.updateSortOrder(sortOrder)
        }
    }

    /**
     * Clears all active filters.
     */
    private fun clearFilters() {
        viewModelScope.launch {
            Timber.d("Clearing all filters")
            searchPreferencesDataSource.clearFilters()
            setEffect { CountriesContract.Effect.ShowToast("Filters cleared") }
        }
    }

    /**
     * Navigate to country details.
     */
    private fun navigateToDetails(countryCode: String) {
        Timber.d("Navigating to details for: $countryCode")

        // Save current search to history
        val query = state.value.searchQuery
        if (query.isNotBlank()) {
            viewModelScope.launch {
                searchPreferencesDataSource.addToSearchHistory(query)
            }
        }

        viewModelScope.launch {
            searchPreferencesDataSource.addToRecentlyViewedCountry(countryCode)
        }

        setEffect { CountriesContract.Effect.NavigateToDetails(countryCode) }
    }

    private fun onSearchHistoryItemSelected(query: String) {
        Timber.d("Search history item selected: $query")
        selectSearchQuery(query)
    }

    private fun deleteSearchHistoryEntry(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            Timber.d("Deleting search history entry: $query")
            searchPreferencesDataSource.removeFromSearchHistory(query)
        }
    }

    /**
     * Toggle favorite status of a country.
     */
    private fun toggleFavorite(countryCode: String) {
        setState {
            val newFavorites = if (favoriteCountryCodes.contains(countryCode)) {
                favoriteCountryCodes - countryCode
            } else {
                favoriteCountryCodes + countryCode
            }
            copy(favoriteCountryCodes = newFavorites)
        }

        val isFavorite = state.value.favoriteCountryCodes.contains(countryCode)
        val message = if (isFavorite) "Added to favorites" else "Removed from favorites"
        setEffect { CountriesContract.Effect.ShowToast(message) }
    }

    /**
     * Clear error message.
     */
    private fun clearError() {
        setState { copy(errorMessage = null) }
    }

    /**
     * Apply filters and sorting to countries list.
     */
    private fun applyFiltersAndSort(countries: List<Country>, filters: SearchFilters): List<Country> {
        return filteredSearchUseCase.applyFiltersAndSort(countries, filters)
    }

    private fun mapRecentlyViewedCountries(
        entries: List<RecentlyViewedEntry>,
        countries: List<Country>,
    ): List<Country> {
        if (entries.isEmpty() || countries.isEmpty()) return emptyList()
        val countryMap = countries.associateBy { it.threeLetterCode.uppercase() }
        return entries.mapNotNull { entry ->
            countryMap[entry.countryCode.uppercase()]
        }
    }

    /**
     * Gets human-readable cache age description.
     */
    fun getCacheAge(): String {
        val timestamp = state.value.lastUpdated
        return if (timestamp > 0) {
            CachePolicy.getCacheAgeDescription(timestamp)
        } else {
            "Never"
        }
    }

    /**
     * Checks if cached data is still fresh.
     */
    fun isCacheFresh(): Boolean {
        val timestamp = state.value.lastUpdated
        return if (timestamp > 0) {
            CachePolicy.isCacheFresh(timestamp)
        } else {
            false
        }
    }

    /**
     * Gets current search query value.
     */
    fun getCurrentSearchQuery(): String = state.value.searchQuery

    /**
     * Saves current search query to history.
     */
    fun saveSearchToHistory() {
        persistSearchQuery(state.value.searchQuery)
    }

    private fun persistSearchQuery(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            Timber.d("Saving search to history: $query")
            searchPreferencesDataSource.addToSearchHistory(query)
        }
    }

    /**
     * Clears search history.
     */
    fun clearSearchHistory() {
        viewModelScope.launch {
            Timber.d("Clearing search history")
            searchPreferencesDataSource.clearSearchHistory()
        }
    }

    /**
     * Selects a query from history or suggestions.
     */
    fun selectSearchQuery(query: String) {
        Timber.d("Selecting search query: $query")
        onSearchQueryChange(query)
        persistSearchQuery(query)
    }

    /**
     * Gets search suggestions for current query.
     */
    fun getSearchSuggestions(): List<String> {
        return searchSuggestionsFlow.value
    }

    /**
     * Gets search preferences including history and filters.
     */
    fun getSearchPreferences(): SearchPreferences {
        return searchPreferencesFlow.value
    }

    /**
     * Checks if filters are active.
     */
    fun hasActiveFilters(filters: SearchFilters): Boolean {
        return searchFiltersUseCase.hasActiveFilters(filters)
    }

    /**
     * Gets count of active filters.
     */
    fun getActiveFilterCount(filters: SearchFilters): Int {
        return searchFiltersUseCase.getActiveFilterCount(filters)
    }
}
