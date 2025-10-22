package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domainmodel.Country
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing country data operations.
 *
 * This interface defines the contract for country data access, abstracting the
 * underlying data sources (network, database, cache) from the domain layer.
 * It follows the Repository pattern and Dependency Inversion Principle.
 *
 * **Architecture:**
 * - Part of the Clean Architecture domain layer
 * - Implementation resides in the data layer
 * - Uses reactive Flow for observable data
 * - Supports offline-first architecture
 *
 * **Data Sources:**
 * - Local: Room database for offline access
 * - Remote: REST API for fresh data
 * - Cache: In-memory cache for frequent access
 *
 * **Error Handling:**
 * All methods return [ApiResponse] wrapper that includes:
 * - [ApiResponse.Success] with data
 * - [ApiResponse.Error] with exception
 * - [ApiResponse.Loading] for loading state
 *
 * @see CountriesRepositoryImpl for implementation details
 * @see Country for the domain model
 *
 * @since 1.0.0
 */
interface CountriesRepository {
    
    /**
     * Retrieves all countries from the repository with configurable cache strategy.
     *
     * **Strategy:**
     * The behavior depends on the [policy] parameter:
     * - [CachePolicy.CACHE_FIRST]: Returns cached data if fresh, fetches if stale
     * - [CachePolicy.NETWORK_FIRST]: Always tries network first, fallback to cache
     * - [CachePolicy.FORCE_REFRESH]: Always fetches from network
     * - [CachePolicy.CACHE_ONLY]: Returns only cached data, never fetches
     *
     * **Flow Emissions (CACHE_FIRST):**
     * 1. [ApiResponse.Loading] - Initial loading state
     * 2. [ApiResponse.Success] with cached data (if available and fresh)
     * 3. [ApiResponse.Success] with fresh data (after network fetch, if stale)
     * 4. [ApiResponse.Error] if both cache and network fail
     *
     * **Flow Emissions (NETWORK_FIRST):**
     * 1. [ApiResponse.Loading] - Initial loading state
     * 2. [ApiResponse.Success] with fresh data (if network succeeds)
     * 3. [ApiResponse.Success] with cached data (if network fails, fallback)
     * 4. [ApiResponse.Error] if both network and cache fail
     *
     * **Flow Emissions (FORCE_REFRESH):**
     * 1. [ApiResponse.Loading] - Initial loading state
     * 2. [ApiResponse.Success] with fresh data (if network succeeds)
     * 3. [ApiResponse.Error] if network fails (no cache fallback)
     *
     * **Flow Emissions (CACHE_ONLY):**
     * 1. [ApiResponse.Loading] - Initial loading state
     * 2. [ApiResponse.Success] with cached data (if available)
     * 3. [ApiResponse.Error] if cache is empty
     *
     * **Cache Staleness:**
     * - Data is considered stale after 24 hours
     * - Staleness only checked for [CachePolicy.CACHE_FIRST]
     * - Updated timestamp on every successful network fetch
     *
     * @param policy Cache strategy to use (default: [CachePolicy.CACHE_FIRST])
     * @return Flow of [ApiResponse] containing list of all countries
     *
     * Example:
     * ```kotlin
     * // Default: use cache first
     * repository.getCountries()
     *     .collect { response ->
     *         when (response) {
     *             is ApiResponse.Success -> showCountries(response.data)
     *             is ApiResponse.Error -> showError(response.exception)
     *             is ApiResponse.Loading -> showLoading()
     *         }
     *     }
     *
     * // Pull-to-refresh: force fresh data
     * repository.getCountries(CachePolicy.FORCE_REFRESH)
     *     .collect { response ->
     *         // Always gets fresh data or error
     *     }
     *
     * // Offline mode: cache only
     * repository.getCountries(CachePolicy.CACHE_ONLY)
     *     .collect { response ->
     *         // Never hits network
     *     }
     * ```
     *
     * @see CachePolicy for detailed policy descriptions
     * @since 2.4.0 (added cache policy support)
     */
    fun getCountries(policy: CachePolicy = CachePolicy.CACHE_FIRST): Flow<ApiResponse<List<Country>>>
    
    /**
     * Retrieves a single country by its three-letter ISO code.
     *
     * This method provides efficient single-country lookup without loading
     * the entire countries list. Uses indexed database query for O(1) lookup.
     *
     * **Strategy:**
     * - Checks local database first (indexed query)
     * - Returns cached data if available and not stale
     * - Optionally refreshes from network if data is stale
     *
     * **Flow Emissions:**
     * 1. [ApiResponse.Loading] - Initial loading state
     * 2. [ApiResponse.Success] with country data (if found)
     * 3. [ApiResponse.Error] if country not found or error occurs
     *
     * @param code The three-letter country code (ISO 3166-1 alpha-3)
     *            Examples: "USA", "GBR", "JPN", "IND"
     *            Case-insensitive (will be normalized)
     *
     * @return Flow of [ApiResponse] containing the requested country
     *
     * @throws IllegalArgumentException if code format is invalid
     *
     * Example:
     * ```kotlin
     * repository.getCountryByCode("USA")
     *     .collect { response ->
     *         when (response) {
     *             is ApiResponse.Success -> showCountryDetails(response.data)
     *             is ApiResponse.Error -> showNotFound()
     *             is ApiResponse.Loading -> showLoading()
     *         }
     *     }
     * ```
     *
     * @since 1.1.0
     */
    fun getCountryByCode(code: String): Flow<ApiResponse<Country>>
    
    /**
     * Observes all countries as a reactive Flow.
     *
     * This method provides a reactive stream of countries that automatically
     * updates when the underlying database changes. Unlike [getCountries()],
     * this method doesn't wrap the result in [ApiResponse] and doesn't trigger
     * network refreshes - it purely observes the database.
     *
     * **Use Cases:**
     * - Real-time UI updates
     * - Observing database changes from background operations
     * - Building derived data streams (search, filter, sort)
     * - Combining multiple data sources
     *
     * **Flow Characteristics:**
     * - Emits immediately with current database state
     * - Emits new values whenever database changes
     * - Never completes (continuous observation)
     * - Cold Flow (starts when collected)
     *
     * **Difference from getCountries():**
     * ```
     * getCountries():     Loading → Success(cache) → Success(network)
     * getCountriesFlow(): [country1, country2] → [updated list] → ...
     * ```
     *
     * @return Flow that emits list of countries whenever database changes
     *
     * Example:
     * ```kotlin
     * repository.getCountriesFlow()
     *     .collect { countries ->
     *         // Update UI with latest countries
     *         updateCountryList(countries)
     *     }
     * ```
     *
     * @since 2.0.0
     */
    fun getCountriesFlow(): Flow<List<Country>>
    
    /**
     * Searches countries by name with reactive updates.
     *
     * Performs a case-insensitive search on country names and returns
     * results as a reactive Flow. The search automatically updates when
     * the database changes.
     *
     * **Search Behavior:**
     * - Case-insensitive partial matching
     * - Matches anywhere in country name
     * - Results sorted alphabetically by name
     * - Empty query returns all countries
     *
     * **Examples:**
     * ```
     * Query: "united"
     * Results: United States, United Kingdom, United Arab Emirates
     *
     * Query: "stan"
     * Results: Afghanistan, Kazakhstan, Kyrgyzstan, Pakistan, etc.
     *
     * Query: "ISLAND"
     * Results: Iceland, Ireland, Marshall Islands, etc.
     * ```
     *
     * **Performance:**
     * - Database indexed search (fast)
     * - Results streamed as available
     * - Minimal memory footprint
     *
     * **Use Cases:**
     * - Search bar implementation
     * - Autocomplete suggestions
     * - Country picker with search
     * - Filtering large country lists
     *
     * @param query Search term (case-insensitive)
     *              Empty string returns all countries
     *
     * @return Flow emitting matching countries, updates on database changes
     *
     * Example:
     * ```kotlin
     * // Search with debouncing
     * searchQueryFlow
     *     .debounce(300)
     *     .flatMapLatest { query ->
     *         repository.searchCountries(query)
     *     }
     *     .collect { results ->
     *         showSearchResults(results)
     *     }
     * ```
     *
     * @since 2.0.0
     * @see SearchCountriesUseCase for a more feature-rich search implementation
     */
    fun searchCountries(query: String): Flow<List<Country>>
    
    /**
     * Retrieves countries filtered by region with reactive updates.
     *
     * Returns all countries in the specified region as a reactive Flow.
     * Results automatically update when the database changes.
     *
     * **Supported Regions:**
     * - Africa
     * - Americas (North and South America)
     * - Asia
     * - Europe
     * - Oceania
     * - Antarctic (Antarctica and surrounding territories)
     *
     * **Region Matching:**
     * - Exact match (case-sensitive)
     * - No partial matching
     * - Invalid regions return empty list
     *
     * **Results:**
     * - Sorted alphabetically by country name
     * - Includes all countries in the region
     * - Updates automatically on database changes
     *
     * **Examples:**
     * ```
     * Region: "Europe"
     * Results: Albania, Andorra, Austria, Belgium, ..., United Kingdom
     *
     * Region: "Asia"
     * Results: Afghanistan, Armenia, Azerbaijan, ..., Yemen
     *
     * Region: "Oceania"
     * Results: Australia, Fiji, Kiribati, ..., Vanuatu
     * ```
     *
     * **Use Cases:**
     * - Region-based filtering
     * - Geographic data visualization
     * - Regional statistics
     * - Continent selectors
     *
     * **Performance:**
     * - Database indexed query (fast)
     * - Typical result: 40-50 countries per region
     * - Minimal memory overhead
     *
     * @param region The region name (must match exactly, case-sensitive)
     *              Examples: "Africa", "Americas", "Asia", "Europe", "Oceania"
     *
     * @return Flow emitting countries in the specified region
     *         Empty list if region not found or invalid
     *
     * Example:
     * ```kotlin
     * repository.getCountriesByRegion("Europe")
     *     .collect { europeanCountries ->
     *         displayCountries(europeanCountries)
     *         updateStatistics(europeanCountries)
     *     }
     * ```
     *
     * @since 2.0.0
     * @see GetCountriesByRegionUseCase for a more feature-rich implementation
     */
    fun getCountriesByRegion(region: String): Flow<List<Country>>
    
    /**
     * Forces a refresh of country data from the network.
     *
     * This method bypasses the cache and fetches fresh data directly from
     * the API, then updates the local database. Use this for user-triggered
     * refreshes (pull-to-refresh) or when you need to ensure data is current.
     *
     * **Refresh Strategy:**
     * 1. Fetch fresh data from network API
     * 2. Clear existing database entries
     * 3. Insert fresh data into database
     * 4. Return success or failure
     *
     * **Behavior:**
     * - Suspending function (runs on background thread)
     * - Blocks until completion
     * - Clears ALL existing data
     * - Atomic operation (transaction)
     *
     * **Result:**
     * - Success: Data refreshed successfully
     * - Failure: Network error, parsing error, or database error
     *
     * **Use Cases:**
     * - Pull-to-refresh gesture
     * - Manual "Refresh" button
     * - Forced sync after app update
     * - Recovery from stale data
     *
     * **Side Effects:**
     * - Triggers [getCountriesFlow] emissions
     * - Updates all active observers
     * - Clears cache completely
     * - Network request overhead
     *
     * **Error Handling:**
     * ```kotlin
     * repository.forceRefresh().fold(
     *     onSuccess = { showSuccessMessage() },
     *     onFailure = { error ->
     *         when (error) {
     *             is IOException -> showNetworkError()
     *             is HttpException -> showServerError()
     *             else -> showGenericError()
     *         }
     *     }
     * )
     * ```
     *
     * **Performance Considerations:**
     * - Network request: ~1-3 seconds
     * - Database operation: ~100-200ms
     * - Total: ~1.5-3.5 seconds
     * - Should run in background with loading indicator
     *
     * @return Result indicating success or failure with exception
     *         Success: Unit (no data returned, observers updated automatically)
     *         Failure: Exception with error details
     *
     * Example:
     * ```kotlin
     * // With coroutines
     * viewModelScope.launch {
     *     repository.forceRefresh().fold(
     *         onSuccess = { _uiState.value = UiState.RefreshSuccess },
     *         onFailure = { error ->
     *             _uiState.value = UiState.Error(error.message)
     *         }
     *     )
     * }
     * ```
     *
     * @since 2.0.0
     * @see RefreshCountriesUseCase for a more feature-rich implementation
     */
    suspend fun forceRefresh(): Result<Unit>
}