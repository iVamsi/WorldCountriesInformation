package com.vamsi.worldcountriesinformation.domain.core

/**
 * Cache policy strategy for data fetching operations.
 *
 * Defines how the repository should handle cached data versus fresh network requests.
 * This enum is part of the offline-first architecture, allowing fine-grained control
 * over data freshness vs performance tradeoffs.
 *
 * ## Architecture Pattern
 *
 * This follows the **Cache-Aside Pattern** where:
 * 1. Application checks cache first
 * 2. If cache miss or stale, fetch from source
 * 3. Update cache with fresh data
 * 4. Return data to caller
 *
 * ## Usage in Repository
 *
 * ```kotlin
 * interface CountriesRepository {
 *     fun getCountries(
 *         policy: CachePolicy = CachePolicy.CACHE_FIRST
 *     ): Flow<ApiResponse<List<Country>>>
 * }
 * ```
 *
 * ## Example Usage
 *
 * **User-Triggered Refresh (Pull-to-Refresh):**
 * ```kotlin
 * fun onRefresh() {
 *     viewModelScope.launch {
 *         repository.getCountries(CachePolicy.FORCE_REFRESH)
 *             .collect { response ->
 *                 // Always gets fresh data from network
 *             }
 *     }
 * }
 * ```
 *
 * **Normal Screen Load:**
 * ```kotlin
 * fun loadCountries() {
 *     viewModelScope.launch {
 *         repository.getCountries(CachePolicy.CACHE_FIRST)
 *             .collect { response ->
 *                 // Uses cache if fresh, otherwise fetches from network
 *             }
 *     }
 * }
 * ```
 *
 * **Guaranteed Fresh Data:**
 * ```kotlin
 * fun loadCriticalData() {
 *     viewModelScope.launch {
 *         repository.getCountries(CachePolicy.NETWORK_FIRST)
 *             .collect { response ->
 *                 // Always tries network first, fallback to cache
 *             }
 *     }
 * }
 * ```
 *
 * **Offline Mode:**
 * ```kotlin
 * fun loadOffline() {
 *     viewModelScope.launch {
 *         repository.getCountries(CachePolicy.CACHE_ONLY)
 *             .collect { response ->
 *                 // Never hits network, returns cached data or error
 *             }
 *     }
 * }
 * ```
 *
 * ## Performance Considerations
 *
 * | Policy | Network Calls | Best For | Trade-off |
 * |--------|---------------|----------|-----------|
 * | CACHE_FIRST | Only if stale | Most screens | Best UX, may show stale data |
 * | NETWORK_FIRST | Always tries | Critical data | Fresh data, slower |
 * | FORCE_REFRESH | Always | Pull-to-refresh | Always fresh, uses data |
 * | CACHE_ONLY | Never | Offline mode | Fast, may be stale/missing |
 *
 * ## Staleness Policy
 *
 * Cache is considered stale if:
 * - Data is older than 24 hours (default)
 * - Can be configured per repository
 * - Based on `lastUpdated` timestamp in entities
 *
 * ## Error Handling
 *
 * Each policy handles errors differently:
 * - **CACHE_FIRST**: Network error → return cached data
 * - **NETWORK_FIRST**: Network error → fallback to cached data
 * - **FORCE_REFRESH**: Network error → return error (no fallback)
 * - **CACHE_ONLY**: Cache miss → return error
 *
 * @see CountriesRepository
 * @see ApiResponse
 *
 * @since 2.0.0
 */
enum class CachePolicy {
    /**
     * Check cache first, only fetch from network if cache is stale or missing.
     *
     * **Strategy:**
     * 1. Check local database
     * 2. If data exists and is fresh (< 24 hours) → return cached data
     * 3. If data is stale or missing → fetch from network
     * 4. Update cache with fresh data
     * 5. Return fresh data
     *
     * **Use Cases:**
     * - Default strategy for most screens
     * - List screens (countries list, search results)
     * - Non-critical data where slight staleness is acceptable
     * - Screens opened frequently (benefits from caching)
     *
     * **Advantages:**
     * - Fast initial load (cached data shown immediately)
     * - Reduced network usage
     * - Works offline (if cache exists)
     * - Best user experience (instant feedback)
     *
     * **Disadvantages:**
     * - May show stale data for up to 24 hours
     * - Requires manual refresh for latest updates
     *
     * **Example:**
     * ```kotlin
     * // Default policy for countries list
     * repository.getCountries(CachePolicy.CACHE_FIRST)
     *     .collect { response ->
     *         when (response) {
     *             is ApiResponse.Success -> {
     *                 // Shows cached data immediately if available
     *                 // Updates to fresh data when available
     *             }
     *         }
     *     }
     * ```
     *
     * **Flow Emissions:**
     * - Emission 1: `Loading`
     * - Emission 2: `Success(cachedData)` [if exists and fresh]
     * - Emission 3: `Success(freshData)` [after network fetch, if stale]
     */
    CACHE_FIRST,

    /**
     * Always try to fetch from network first, fallback to cache on failure.
     *
     * **Strategy:**
     * 1. Attempt network fetch immediately
     * 2. If network succeeds → update cache → return fresh data
     * 3. If network fails → check cache
     * 4. If cache exists → return cached data (even if stale)
     * 5. If no cache → return error
     *
     * **Use Cases:**
     * - Critical data that must be current
     * - Financial data, prices, availability
     * - User profiles, settings
     * - Data with high change frequency
     *
     * **Advantages:**
     * - Always attempts to get latest data
     * - Still works offline (cache fallback)
     * - Good for time-sensitive data
     *
     * **Disadvantages:**
     * - Slower initial load (waits for network)
     * - More network usage
     * - Higher data costs
     * - Shows loading state longer
     *
     * **Example:**
     * ```kotlin
     * // For country details that need to be current
     * repository.getCountryByCode(code, CachePolicy.NETWORK_FIRST)
     *     .collect { response ->
     *         when (response) {
     *             is ApiResponse.Success -> {
     *                 // Shows fresh data from network
     *                 // Falls back to cache if offline
     *             }
     *         }
     *     }
     * ```
     *
     * **Flow Emissions:**
     * - Emission 1: `Loading`
     * - Emission 2: `Success(freshData)` [if network succeeds]
     * - OR Emission 2: `Success(cachedData)` [if network fails but cache exists]
     * - OR Emission 2: `Error` [if network fails and no cache]
     */
    NETWORK_FIRST,

    /**
     * Always fetch from network, ignore cache completely, update cache after.
     *
     * **Strategy:**
     * 1. Always fetch from network (no cache check)
     * 2. If network succeeds → update cache → return fresh data
     * 3. If network fails → return error (no cache fallback)
     *
     * **Use Cases:**
     * - Pull-to-refresh user action
     * - Explicit refresh button
     * - After data modification (POST/PUT/DELETE)
     * - When user explicitly requests fresh data
     *
     * **Advantages:**
     * - Guaranteed fresh data
     * - User sees immediate feedback from their action
     * - Cache updated for future CACHE_FIRST requests
     *
     * **Disadvantages:**
     * - Doesn't work offline
     * - Always uses network/data
     * - Slower than cache
     * - May fail if no connectivity
     *
     * **Example:**
     * ```kotlin
     * // Pull-to-refresh handler
     * fun onRefresh() {
     *     repository.getCountries(CachePolicy.FORCE_REFRESH)
     *         .collect { response ->
     *             when (response) {
     *                 is ApiResponse.Success -> {
     *                     // Always fresh from network
     *                     showSuccessMessage()
     *                 }
     *                 is ApiResponse.Error -> {
     *                     // Network failed, show error
     *                     showErrorMessage()
     *                 }
     *             }
     *         }
     * }
     * ```
     *
     * **Flow Emissions:**
     * - Emission 1: `Loading`
     * - Emission 2: `Success(freshData)` [if network succeeds]
     * - OR Emission 2: `Error` [if network fails - no cache fallback]
     */
    FORCE_REFRESH,

    /**
     * Only use cached data, never fetch from network.
     *
     * **Strategy:**
     * 1. Check local database only
     * 2. If data exists → return cached data (regardless of age)
     * 3. If data missing → return error
     * 4. Never attempts network request
     *
     * **Use Cases:**
     * - Explicit offline mode
     * - Testing/development
     * - No network permission scenarios
     * - Reading archived/historical data
     * - Very large datasets that rarely change
     *
     * **Advantages:**
     * - Extremely fast (no network wait)
     * - Works without internet
     * - Zero data usage
     * - Predictable behavior
     *
     * **Disadvantages:**
     * - May return very stale data
     * - Fails if cache empty
     * - No automatic updates
     * - User must manually switch to online mode
     *
     * **Example:**
     * ```kotlin
     * // Offline mode toggle
     * fun loadCountries(offlineMode: Boolean) {
     *     val policy = if (offlineMode) {
     *         CachePolicy.CACHE_ONLY
     *     } else {
     *         CachePolicy.CACHE_FIRST
     *     }
     *     
     *     repository.getCountries(policy)
     *         .collect { response ->
     *             when (response) {
     *                 is ApiResponse.Success -> showData(response.data)
     *                 is ApiResponse.Error -> showOfflineError()
     *             }
     *         }
     * }
     * ```
     *
     * **Flow Emissions:**
     * - Emission 1: `Loading`
     * - Emission 2: `Success(cachedData)` [if cache exists]
     * - OR Emission 2: `Error("No cached data available")` [if cache empty]
     */
    CACHE_ONLY;

    /**
     * Checks if this policy should check network for fresh data.
     *
     * @return true if network fetch is required, false if cache-only
     */
    fun shouldFetchFromNetwork(): Boolean = when (this) {
        CACHE_FIRST -> false // Only if cache is stale/missing
        NETWORK_FIRST -> true
        FORCE_REFRESH -> true
        CACHE_ONLY -> false
    }

    /**
     * Checks if this policy allows cache fallback on network failure.
     *
     * @return true if cache can be used as fallback, false if must fail
     */
    fun allowsCacheFallback(): Boolean = when (this) {
        CACHE_FIRST -> true
        NETWORK_FIRST -> true
        FORCE_REFRESH -> false // Fail on network error
        CACHE_ONLY -> true
    }

    /**
     * Checks if this policy requires cache staleness checks.
     *
     * @return true if staleness should be checked, false if age doesn't matter
     */
    fun requiresStalenessCheck(): Boolean = when (this) {
        CACHE_FIRST -> true // Check if cache is fresh
        NETWORK_FIRST -> false // Always fetch network anyway
        FORCE_REFRESH -> false // Always fetch network
        CACHE_ONLY -> false // Return any cached data
    }

    companion object {
        /**
         * Default cache validity period in milliseconds (24 hours).
         *
         * Data older than this is considered stale and will trigger a refresh
         * when using [CACHE_FIRST] policy.
         */
        const val DEFAULT_CACHE_VALIDITY_MS = 24 * 60 * 60 * 1000L // 24 hours

        /**
         * Checks if cached data is still fresh based on lastUpdated timestamp.
         *
         * @param lastUpdated Timestamp when data was last updated (milliseconds)
         * @param validityPeriodMs Cache validity period (default 24 hours)
         * @return true if data is still fresh, false if stale
         */
        fun isCacheFresh(
            lastUpdated: Long,
            validityPeriodMs: Long = DEFAULT_CACHE_VALIDITY_MS
        ): Boolean {
            val age = System.currentTimeMillis() - lastUpdated
            return age < validityPeriodMs
        }

        /**
         * Gets the age of cached data in milliseconds.
         *
         * @param lastUpdated Timestamp when data was last updated
         * @return Age in milliseconds
         */
        fun getCacheAge(lastUpdated: Long): Long {
            return System.currentTimeMillis() - lastUpdated
        }

        /**
         * Gets a human-readable description of cache age.
         *
         * @param lastUpdated Timestamp when data was last updated
         * @return Human-readable string (e.g., "2 hours ago", "3 days ago")
         */
        fun getCacheAgeDescription(lastUpdated: Long): String {
            val ageMs = getCacheAge(lastUpdated)
            val ageSeconds = ageMs / 1000
            val ageMinutes = ageSeconds / 60
            val ageHours = ageMinutes / 60
            val ageDays = ageHours / 24

            return when {
                ageDays > 0 -> "$ageDays day${if (ageDays != 1L) "s" else ""} ago"
                ageHours > 0 -> "$ageHours hour${if (ageHours != 1L) "s" else ""} ago"
                ageMinutes > 0 -> "$ageMinutes minute${if (ageMinutes != 1L) "s" else ""} ago"
                else -> "Just now"
            }
        }
    }
}
