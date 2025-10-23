package com.vamsi.worldcountriesinformation.feature.countries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.core.UiState
import com.vamsi.worldcountriesinformation.domain.core.onError
import com.vamsi.worldcountriesinformation.domain.core.onLoading
import com.vamsi.worldcountriesinformation.domain.core.onSuccess
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.domainmodel.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Countries list screen.
 *
 * This ViewModel manages the state and business logic for displaying the list of
 * countries. Enhanced in Phase 3 to support cache policies and ApiResponse extensions.
 *
 * ## Phase 3 Enhancements
 *
 * **New Features:**
 * - Cache policy support for different data fetching strategies
 * - ApiResponse extensions for cleaner code (.onSuccess, .onError, .onLoading)
 * - Pull-to-refresh with FORCE_REFRESH policy
 * - Cache age tracking for transparency
 * - Refreshing state management
 *
 * **Architecture:**
 * - Follows MVVM pattern with unidirectional data flow
 * - Uses StateFlow for reactive UI updates
 * - Leverages Phase 2 cache policies for optimal performance
 * - Integrates ApiResponse extensions for cleaner error handling
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
 *
 * @see GetCountriesUseCase
 * @see CachePolicy
 * @see UiState
 *
 * @since 1.0.0 (Enhanced in 2.0.0)
 *
 * Example usage:
 * ```kotlin
 * @Composable
 * fun CountriesScreen(viewModel: CountriesViewModel = hiltViewModel()) {
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *     val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
 *
 *     PullRefreshLayout(
 *         refreshing = isRefreshing,
 *         onRefresh = { viewModel.refresh() }
 *     ) {
 *         // Render based on uiState
 *     }
 * }
 * ```
 */
@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val countriesUseCase: GetCountriesUseCase
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
}
