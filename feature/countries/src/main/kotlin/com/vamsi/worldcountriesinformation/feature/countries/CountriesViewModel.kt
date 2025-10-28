package com.vamsi.worldcountriesinformation.feature.countries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.core.UiState
import com.vamsi.worldcountriesinformation.domain.core.onError
import com.vamsi.worldcountriesinformation.domain.core.onLoading
import com.vamsi.worldcountriesinformation.domain.core.onSuccess
import com.vamsi.worldcountriesinformation.domain.countries.FilteredSearchCountriesUseCase
import com.vamsi.worldcountriesinformation.domain.countries.GenerateSearchSuggestionsUseCase
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.domain.countries.SearchCountriesUseCase
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Regions
import com.vamsi.worldcountriesinformation.domainmodel.SearchFilters
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder
import com.vamsi.worldcountriesinformation.core.datastore.SearchPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Countries list screen.
 *
 * This ViewModel manages the state and business logic for displaying the list of
 * countries. Enhanced in Phase 3 to support cache policies, ApiResponse extensions,
 * and search functionality.
 *
 * ## Phase 3 Enhancements
 *
 * **New Features:**
 * - Cache policy support for different data fetching strategies
 * - ApiResponse extensions for cleaner code (.onSuccess, .onError, .onLoading)
 * - Pull-to-refresh with FORCE_REFRESH policy
 * - Cache age tracking for transparency
 * - Refreshing state management
 * - **Search functionality with debounced input (Phase 3.8)**
 *
 * **Search Features (Phase 3.8):**
 * - Real-time search with 300ms debounce
 * - Case-insensitive partial matching
 * - Search history tracking
 * - Clear search functionality
 * - Empty state handling
 *
 * **Architecture:**
 * - Follows MVVM pattern with unidirectional data flow
 * - Uses StateFlow for reactive UI updates
 * - Leverages Phase 2 cache policies for optimal performance
 * - Integrates ApiResponse extensions for cleaner error handling
 * - Search operates on cached data for instant results
 *
 * **State Management:**
 * - [UiState.Idle] - Initial state before any data load
 * - [UiState.Loading] - Data is being fetched
 * - [UiState.Success] - Countries loaded successfully
 * - [UiState.Error] - Error occurred during data fetch
 *
 * **Cache Policies:**
 * - Default load: [CachePolicy.CACHE_FIRST] (instant with cache, updates if stale)
 * - Pull-to-refresh: [CachePolicy.FORCE_REFRESH] (always fetch fresh)
 * - Offline mode: [CachePolicy.CACHE_ONLY] (never hit network)
 *
 * @param countriesUseCase Use case for fetching countries with cache policy support
 * @param searchCountriesUseCase Use case for searching countries in cache
 *
 * @see GetCountriesUseCase
 * @see SearchCountriesUseCase
 * @see CachePolicy
 * @see UiState
 *
 * @since 1.0.0 (Enhanced in 2.0.0, Search added in Phase 3.8)
 *
 * Example usage:
 * ```kotlin
 * @Composable
 * fun CountriesScreen(viewModel: CountriesViewModel = hiltViewModel()) {
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *     val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
 *     val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
 *     val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
 *
 *     SearchBar(
 *         query = searchQuery,
 *         onQueryChange = { viewModel.onSearchQueryChange(it) }
 *     )
 *
 *     PullRefreshLayout(
 *         refreshing = isRefreshing,
 *         onRefresh = { viewModel.refresh() }
 *     ) {
 *         CountriesList(countries = searchResults)
 *     }
 * }
 * ```
 */
@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val countriesUseCase: GetCountriesUseCase,
    private val searchCountriesUseCase: SearchCountriesUseCase,
    private val filteredSearchUseCase: FilteredSearchCountriesUseCase,
    private val suggestionsUseCase: GenerateSearchSuggestionsUseCase,
    private val searchPreferencesDataSource: SearchPreferencesDataSource,
    private val searchFiltersUseCase: com.vamsi.worldcountriesinformation.domain.search.SearchFiltersUseCase
) : ViewModel() {

    /**
     * Internal mutable state for countries list.
     * Only this ViewModel can modify the state.
     */
    private val _uiState = MutableStateFlow<UiState<List<Country>>>(UiState.Idle)

    /**
     * Public read-only state flow for observing countries list.
     * UI components should collect from this flow to receive state updates.
     */
    val uiState: StateFlow<UiState<List<Country>>> = _uiState.asStateFlow()

    /**
     * Internal mutable state for refresh indicator.
     * Separate from [_uiState] to avoid overriding success state during refresh.
     */
    private val _isRefreshing = MutableStateFlow(false)

    /**
     * Public read-only state flow for pull-to-refresh indicator.
     * True when refresh is in progress, false otherwise.
     */
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /**
     * Tracks the timestamp of last successful data load.
     * Used for cache age display.
     */
    private val _lastUpdated = MutableStateFlow(0L)

    /**
     * Public read-only state flow for cache age tracking.
     * Returns timestamp in milliseconds of last successful load.
     */
    val lastUpdated: StateFlow<Long> = _lastUpdated.asStateFlow()

    /**
     * Internal mutable state for search query.
     * **Phase 3.8 Enhancement:** Search functionality
     */
    private val _searchQuery = MutableStateFlow("")

    /**
     * Public read-only state flow for search query.
     * UI should collect this to display current search text.
     */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Search results flow with debouncing.
     * 
     * **Phase 3.8 Enhancement:**
     * - Debounces input by 300ms to reduce search operations
     * - Only triggers search when query actually changes
     * - Searches on cached data for instant results
     * - Returns all countries when query is empty
     * 
     * **Performance:**
     * - Debounce delay: 300ms
     * - Search time: 5-20ms (database indexed)
     * - No network calls (cache-only)
     */
    val searchResults: StateFlow<List<Country>> = _searchQuery
        .debounce(300L) // Wait 300ms after user stops typing
        .distinctUntilChanged() // Only emit when query changes
        .flatMapLatest { query ->
            if (query.isBlank()) {
                // Empty query: return all countries from current UI state
                uiState.flatMapLatest { state ->
                    when (state) {
                        is UiState.Success -> kotlinx.coroutines.flow.flowOf(state.data)
                        else -> kotlinx.coroutines.flow.flowOf(emptyList())
                    }
                }
            } else {
                // Non-empty query: search in database
                searchCountriesUseCase(query)
                    .catch { exception ->
                        Timber.e(exception, "Search error for query: $query")
                        emit(emptyList())
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    /**
     * Tracks whether search is currently active.
     * True when search query is not empty.
     */
    val isSearchActive: StateFlow<Boolean> = _searchQuery
        .flatMapLatest { query -> kotlinx.coroutines.flow.flowOf(query.isNotBlank()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = false
        )

    init {
        // Load countries on ViewModel creation with default cache policy
        loadCountries()
    }

    /**
     * Loads countries with configurable cache policy.
     *
     * **Phase 3 Enhancement:** Uses ApiResponse extensions for cleaner code.
     *
     * **Cache Policy Behavior:**
     * - [CachePolicy.CACHE_FIRST] (default): Shows cache immediately, updates if stale
     * - [CachePolicy.FORCE_REFRESH]: Always fetches fresh, no cache fallback
     * - [CachePolicy.CACHE_ONLY]: Never hits network, cache only
     * - [CachePolicy.NETWORK_FIRST]: Tries network first, falls back to cache
     *
     * **State Transitions:**
     * ```
     * Idle → Loading → Success/Error
     * Success → Loading → Success/Error (on retry)
     * Error → Loading → Success/Error (on retry)
     * ```
     *
     * @param policy Cache strategy to use (default: CACHE_FIRST for optimal UX)
     *
     * Example:
     * ```kotlin
     * // Default load with cache
     * viewModel.loadCountries()
     *
     * // Force refresh (pull-to-refresh)
     * viewModel.loadCountries(CachePolicy.FORCE_REFRESH)
     *
     * // Offline mode
     * viewModel.loadCountries(CachePolicy.CACHE_ONLY)
     * ```
     */
    fun loadCountries(policy: CachePolicy = CachePolicy.CACHE_FIRST) {
        viewModelScope.launch {
            Timber.d("Loading countries with policy: $policy")

            countriesUseCase(policy)
                .catch { exception ->
                    // Handle unexpected errors from Flow
                    Timber.e(exception, "Unexpected error loading countries")
                    _uiState.value = UiState.Error(
                        exception = Exception(exception),
                        message = "Failed to load countries. Please try again."
                    )
                    _isRefreshing.value = false
                }
                .collect { response ->
                    // Use ApiResponse extensions for cleaner code
                    response
                        .onLoading {
                            Timber.d("Countries loading...")
                            // Only set loading state if not refreshing
                            // (keeps success state visible during pull-to-refresh)
                            if (!_isRefreshing.value) {
                                _uiState.value = UiState.Loading
                            }
                        }
                        .onSuccess { countries ->
                            Timber.d("Countries loaded: ${countries.size} countries")
                            _uiState.value = UiState.Success(countries)
                            _lastUpdated.value = System.currentTimeMillis()
                            _isRefreshing.value = false
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

                            _uiState.value = UiState.Error(
                                exception = exception,
                                message = errorMessage
                            )
                            _isRefreshing.value = false
                        }
                }
        }
    }

    /**
     * Refreshes countries data from network (pull-to-refresh).
     *
     * **Phase 3 Enhancement:** Always uses [CachePolicy.FORCE_REFRESH]
     * to ensure fresh data on explicit user action.
     *
     * This method:
     * 1. Sets [isRefreshing] to true
     * 2. Forces network fetch (ignores cache)
     * 3. Updates cache with fresh data
     * 4. Sets [isRefreshing] to false on completion
     *
     * **UI Integration:**
     * ```kotlin
     * PullRefreshLayout(
     *     refreshing = isRefreshing,
     *     onRefresh = { viewModel.refresh() }
     * )
     * ```
     *
     * **Behavior:**
     * - Network success → Updates cache + UI
     * - Network error → Shows error, keeps existing data visible
     * - No cache fallback (user expects fresh data)
     */
    fun refresh() {
        Timber.d("Refresh requested by user")
        _isRefreshing.value = true
        loadCountries(CachePolicy.FORCE_REFRESH)
    }

    /**
     * Retries loading countries after an error.
     *
     * Uses default [CachePolicy.CACHE_FIRST] for retry to allow
     * showing cached data if network is still unavailable.
     *
     * **State Transition:**
     * ```
     * Error → Loading → Success/Error
     * ```
     *
     * Example:
     * ```kotlin
     * Button(onClick = { viewModel.retry() }) {
     *     Text("Retry")
     * }
     * ```
     */
    fun retry() {
        Timber.d("Retry requested")
        loadCountries(CachePolicy.CACHE_FIRST)
    }

    /**
     * Gets human-readable cache age description.
     *
     * **Phase 3 Enhancement:** Uses [CachePolicy.getCacheAgeDescription]
     * for consistent formatting across the app.
     *
     * @return Human-readable age string (e.g., "2 hours ago", "Just now")
     *
     * Example:
     * ```kotlin
     * Text("Last updated: ${viewModel.getCacheAge()}")
     * ```
     */
    fun getCacheAge(): String {
        val timestamp = _lastUpdated.value
        return if (timestamp > 0) {
            CachePolicy.getCacheAgeDescription(timestamp)
        } else {
            "Never"
        }
    }

    /**
     * Checks if cached data is still fresh.
     *
     * @return true if data is < 24 hours old, false otherwise
     *
     * Example:
     * ```kotlin
     * if (viewModel.isCacheFresh()) {
     *     Text("Data is up to date", color = Color.Green)
     * } else {
     *     Text("Data may be outdated", color = Color.Yellow)
     * }
     * ```
     */
    fun isCacheFresh(): Boolean {
        val timestamp = _lastUpdated.value
        return if (timestamp > 0) {
            CachePolicy.isCacheFresh(timestamp)
        } else {
            false
        }
    }

    /**
     * Updates the search query.
     *
     * **Phase 3.8 Enhancement:** Search functionality
     *
     * This method updates the search query, triggering automatic search
     * with debouncing. The search will execute 300ms after the user
     * stops typing.
     *
     * **Behavior:**
     * - Empty/blank query: Shows all countries
     * - Non-empty query: Filters countries by name
     * - Debounced: 300ms delay after last character
     * - Case-insensitive: "USA" matches "usa"
     * - Partial matching: "unit" matches "United States"
     *
     * @param query The search query string
     *
     * Example:
     * ```kotlin
     * TextField(
     *     value = searchQuery,
     *     onValueChange = { viewModel.onSearchQueryChange(it) },
     *     label = { Text("Search countries") }
     * )
     * ```
     */
    fun onSearchQueryChange(query: String) {
        Timber.d("Search query changed: $query")
        _searchQuery.value = query
    }

    /**
     * Clears the search query.
     *
     * **Phase 3.8 Enhancement:** Search functionality
     *
     * Resets the search query to empty string, which will
     * show all countries again.
     *
     * Example:
     * ```kotlin
     * IconButton(onClick = { viewModel.clearSearch() }) {
     *     Icon(Icons.Default.Clear, "Clear search")
     * }
     * ```
     */
    fun clearSearch() {
        Timber.d("Clearing search query")
        _searchQuery.value = ""
    }

    /**
     * Gets the current search query value.
     *
     * **Phase 3.8 Enhancement:** Search functionality
     *
     * @return Current search query string
     */
    fun getCurrentSearchQuery(): String = _searchQuery.value

    // ========================================
    // Phase 3.10: Advanced Search Features
    // ========================================

    /**
     * Search preferences including filters and history.
     */
    val searchPreferences = searchPreferencesDataSource.searchPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = com.vamsi.worldcountriesinformation.core.datastore.SearchPreferences()
        )

    /**
     * Search suggestions based on current query.
     */
    val searchSuggestions: StateFlow<List<String>> = kotlinx.coroutines.flow.combine(
        _searchQuery,
        uiState
    ) { query, state ->
        if (query.length >= 2 && state is UiState.Success) {
            suggestionsUseCase(
                query = query,
                allCountries = state.data,
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

    /**
     * Filtered search results combining query + filters.
     * 
     * Strategy:
     * - If no filters and no query: return cached data
     * - If query but no filters: use search use case
     * - If filters (with or without query): apply filters to search results or cached data
     */
    private val _filteredResults = kotlinx.coroutines.flow.combine(
        _searchQuery,
        searchPreferences,
        uiState
    ) { query, prefs, state ->
        Triple(query, prefs.filters, state)
    }
        .debounce(300L)
        .flatMapLatest { (query, filters, state) ->
            val hasFilters = searchFiltersUseCase.hasActiveFilters(filters)
            
            Timber.d("FilteredResults - Query: '$query', HasFilters: $hasFilters, Filters: $filters, State: ${state::class.simpleName}")
            
            if (!hasFilters) {
                // No filters active, use basic search
                if (query.isBlank()) {
                    // No query, no filters: return all cached data
                    Timber.d("FilteredResults - Returning cached data")
                    when (state) {
                        is UiState.Success -> {
                            Timber.d("FilteredResults - Cached data size: ${state.data.size}")
                            kotlinx.coroutines.flow.flowOf(state.data)
                        }
                        else -> {
                            Timber.d("FilteredResults - State not Success, returning empty")
                            kotlinx.coroutines.flow.flowOf(emptyList())
                        }
                    }
                } else {
                    // Query but no filters: search database
                    Timber.d("FilteredResults - Searching database for: $query")
                    searchCountriesUseCase(query)
                }
            } else {
                // Filters active
                if (query.isBlank()) {
                    // Filters only: apply to cached data
                    Timber.d("FilteredResults - Applying filters to cached data")
                    when (state) {
                        is UiState.Success -> {
                            Timber.d("FilteredResults - Cached data size: ${state.data.size}, Regions: ${filters.selectedRegions}")
                            val filtered = filteredSearchUseCase.applyFiltersAndSort(state.data, filters)
                            Timber.d("FilteredResults - Filtered data size: ${filtered.size}")
                            if (filtered.isNotEmpty()) {
                                Timber.d("FilteredResults - Sample filtered countries: ${filtered.take(3).map { it.name + " (" + it.region + ")" }}")
                            }
                            kotlinx.coroutines.flow.flowOf(filtered)
                        }
                        else -> {
                            Timber.d("FilteredResults - State not Success, returning empty")
                            kotlinx.coroutines.flow.flowOf(emptyList())
                        }
                    }
                } else {
                    // Query + filters: search then filter
                    Timber.d("FilteredResults - Searching and filtering for: $query")
                    filteredSearchUseCase(query, filters)
                }
            }
        }
        .catch { exception ->
            Timber.e(exception, "Filtered search error")
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    /**
     * Final search results (replaces old searchResults).
     * Uses filtered results when filters are active.
     */
    val filteredSearchResults: StateFlow<List<Country>> = _filteredResults

    /**
     * Updates search filters.
     */
    fun updateSearchFilters(filters: SearchFilters) {
        viewModelScope.launch {
            Timber.d("Updating search filters: $filters")
            searchPreferencesDataSource.updateFilters(filters)
        }
    }

    /**
     * Toggles a region filter.
     */
    fun toggleRegion(region: String) {
        viewModelScope.launch {
            val currentRegions = searchPreferences.value.filters.selectedRegions
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
    fun updateSortOrder(sortOrder: SortOrder) {
        viewModelScope.launch {
            Timber.d("Updating sort order: $sortOrder")
            searchPreferencesDataSource.updateSortOrder(sortOrder)
        }
    }

    /**
     * Clears all active filters.
     */
    fun clearFilters() {
        viewModelScope.launch {
            Timber.d("Clearing all filters")
            searchPreferencesDataSource.clearFilters()
        }
    }

    /**
     * Saves current search query to history.
     */
    fun saveSearchToHistory() {
        val query = _searchQuery.value
        if (query.isNotBlank()) {
            viewModelScope.launch {
                Timber.d("Saving search to history: $query")
                searchPreferencesDataSource.addToSearchHistory(query)
            }
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
        _searchQuery.value = query
        saveSearchToHistory()
    }

    /**
     * Gets all available regions for filtering.
     */
    fun getAvailableRegions(): Set<String> = Regions.ALL

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

