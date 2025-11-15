package com.vamsi.worldcountriesinformation.feature.countrydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.core.UiState
import com.vamsi.worldcountriesinformation.domain.core.onError
import com.vamsi.worldcountriesinformation.domain.core.onLoading
import com.vamsi.worldcountriesinformation.domain.core.onSuccess
import com.vamsi.worldcountriesinformation.domain.countries.CountryByCodeParams
import com.vamsi.worldcountriesinformation.domain.countries.GetCountryByCodeUseCase
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
 * ViewModel for the Country Details screen.
 *
 * Responsibilities:
 * - Load a single country by ISO code using [GetCountryByCodeUseCase]
 * - Expose [UiState] plus pull-to-refresh and cache age indicators
 * - Surface clear error messages, retry hooks, and cache policy controls
 *
 * @param getCountryByCodeUseCase Use case for fetching a single country
 *
 * @see GetCountryByCodeUseCase
 * @see UiState
 * @see Country
 * @see CachePolicy
 * @see CountryByCodeParams
 *
 * @since 1.1.0 (Enhanced in 2.0.0)
 */
@HiltViewModel
class CountryDetailsViewModel @Inject constructor(
    private val getCountryByCodeUseCase: GetCountryByCodeUseCase,
) : ViewModel() {

    /**
     * Internal mutable state for country details.
     * Only this ViewModel can modify the state.
     */
    private val _uiState = MutableStateFlow<UiState<Country>>(UiState.Idle)

    /**
     * Public read-only state flow for observing country details.
     * UI components should collect from this flow to receive state updates.
     *
     * @see UiState for possible states
     */
    val uiState: StateFlow<UiState<Country>> = _uiState.asStateFlow()

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
     * Loads country details for the specified country code with configurable cache policy.
     *
     * This method initiates an asynchronous country fetch operation. It:
     * 1. Sets state to [UiState.Loading] (unless refreshing)
     * 2. Calls [GetCountryByCodeUseCase] with the provided code and policy
     * 3. Uses ApiResponse extensions (.onSuccess, .onError, .onLoading)
     * 4. Handles errors gracefully with policy-specific messages
     *
     * **State Transitions:**
     * ```
     * Idle/Error → Loading → Success/Error
     * Success → Loading → Success/Error (on retry/refresh)
     * ```
     *
     * **Cache Policy Behavior:**
     * - [CachePolicy.CACHE_FIRST] (default): Shows cache immediately
     * - [CachePolicy.FORCE_REFRESH]: Always fetches fresh (pull-to-refresh)
     * - [CachePolicy.CACHE_ONLY]: Never hits network (offline mode)
     * - [CachePolicy.NETWORK_FIRST]: Tries network first, falls back to cache
     *
     * **Thread Safety:**
     * - Executes on viewModelScope (main-safe)
     * - Use case executes on IO dispatcher
     * - State updates on main thread
     *
     * **Error Handling:**
     * - Network errors: "Failed to load country details"
     * - Country not found: "Country '{code}' not found"
     * - No cache (CACHE_ONLY): "No cached data available"
     * - Timeout: "Connection timeout"
     *
     * @param countryCode The three-letter country code (ISO 3166-1 alpha-3)
     *                    Examples: "USA", "GBR", "JPN"
     *                    Case-insensitive (will be normalized)
     * @param policy Cache strategy to use (default: CACHE_FIRST)
     *
     * Example:
     * ```kotlin
     * // Default load with cache
     * viewModel.loadCountryDetails("USA")
     *
     * // Force refresh
     * viewModel.loadCountryDetails("USA", CachePolicy.FORCE_REFRESH)
     *
     * // Offline mode
     * viewModel.loadCountryDetails("USA", CachePolicy.CACHE_ONLY)
     * ```
     */
    fun loadCountryDetails(
        countryCode: String,
        policy: CachePolicy = CachePolicy.CACHE_FIRST,
    ) {
        viewModelScope.launch {
            Timber.d("Loading country details for: $countryCode with policy: $policy")

            getCountryByCodeUseCase(CountryByCodeParams(countryCode, policy))
                .catch { exception ->
                    // Handle unexpected errors from Flow
                    val error = exception as? Exception
                        ?: Exception(exception.message ?: "Unknown error occurred")

                    Timber.e(error, "Unexpected error loading country: $countryCode")

                    _uiState.value = UiState.Error(
                        exception = error,
                        message = "Failed to load country details. Please try again."
                    )
                    _isRefreshing.value = false
                }
                .collect { response ->
                    // Use ApiResponse extensions for cleaner code
                    response
                        .onLoading {
                            Timber.d("Country details loading...")
                            // Only set loading state if not refreshing
                            if (!_isRefreshing.value) {
                                _uiState.value = UiState.Loading
                            }
                        }
                        .onSuccess { country ->
                            Timber.d("Country details loaded: ${country.name}")
                            _uiState.value = UiState.Success(country)
                            _lastUpdated.value = System.currentTimeMillis()
                            _isRefreshing.value = false
                        }
                        .onError { exception ->
                            Timber.e(exception, "Error loading country: $countryCode")

                            val errorMessage = when {
                                exception.message?.contains("No cached data", ignoreCase = true) == true -> {
                                    "No cached data available. Please connect to the internet."
                                }

                                exception.message?.contains("not found", ignoreCase = true) == true -> {
                                    "Country with code '$countryCode' not found"
                                }

                                exception.message?.contains("timeout", ignoreCase = true) == true -> {
                                    "Connection timeout. Please check your internet and try again."
                                }

                                exception.message?.contains("network", ignoreCase = true) == true -> {
                                    "Network error. Please check your connection and try again."
                                }

                                else -> {
                                    "Failed to load country details. Please try again."
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
     * Refreshes country details from network (pull-to-refresh).
     *
     * Always uses [CachePolicy.FORCE_REFRESH] to ensure fresh data on explicit
     * user action.
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
     *     onRefresh = { viewModel.refresh(countryCode) }
     * )
     * ```
     *
     * **Behavior:**
     * - Network success → Updates cache + UI
     * - Network error → Shows error, keeps existing data visible
     * - No cache fallback (user expects fresh data)
     *
     * @param countryCode The three-letter country code to refresh
     */
    fun refresh(countryCode: String) {
        Timber.d("Refresh requested for country: $countryCode")
        _isRefreshing.value = true
        loadCountryDetails(countryCode, CachePolicy.FORCE_REFRESH)
    }

    /**
     * Retries loading country details after an error.
     *
     * Uses default [CachePolicy.CACHE_FIRST] for retry to allow
     * showing cached data if network is still unavailable.
     *
     * **State Transition:**
     * ```
     * Error → Loading → Success/Error
     * ```
     *
     * @param countryCode The three-letter country code to retry
     *
     * Example:
     * ```kotlin
     * Button(onClick = { viewModel.retry(countryCode) }) {
     *     Text("Retry")
     * }
     * ```
     */
    fun retry(countryCode: String) {
        Timber.d("Retry requested for country: $countryCode")
        loadCountryDetails(countryCode, CachePolicy.CACHE_FIRST)
    }

    /**
     * Gets human-readable cache age description.
     *
     * Uses [CachePolicy.getCacheAgeDescription] for consistent formatting
     * across the app.
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

