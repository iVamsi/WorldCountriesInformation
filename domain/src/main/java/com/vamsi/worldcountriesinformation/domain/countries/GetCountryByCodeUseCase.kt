package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.core.FlowUseCase
import com.vamsi.worldcountriesinformation.domain.di.IoDispatcher
import com.vamsi.worldcountriesinformation.domainmodel.Country
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Parameters for [GetCountryByCodeUseCase].
 *
 * @param code The three-letter ISO 3166-1 alpha-3 country code (e.g., "USA", "GBR")
 * @param policy The cache strategy to use (default: CACHE_FIRST)
 *
 * @since 2.0.0 (Phase 3)
 */
data class CountryByCodeParams(
    val code: String,
    val policy: CachePolicy = CachePolicy.CACHE_FIRST
)

/**
 * Use case for retrieving a single country by its three-letter code.
 *
 * ## Phase 3 Enhancement
 * 
 * Updated to support Phase 2.4 cache policies, allowing callers to control
 * data fetching strategy for country details:
 * - **CACHE_FIRST** (default): Instant with cache, updates if available
 * - **NETWORK_FIRST**: Prioritizes fresh data, falls back to cache
 * - **FORCE_REFRESH**: Always fetches fresh (pull-to-refresh)
 * - **CACHE_ONLY**: Offline mode, never hits network
 *
 * This use case provides efficient single-country lookup without loading the entire
 * countries list, improving performance and reducing memory usage for detail screens.
 *
 * **Architecture:**
 * - Follows the Single Responsibility Principle
 * - Part of the Clean Architecture domain layer
 * - Returns reactive Flow for observing country changes
 * - Handles both cached and fresh data scenarios
 * - Supports all 4 cache policies from Phase 2.4
 *
 * **Performance:**
 * - Database query with indexed lookup (O(1) complexity)
 * - No network call required if data is cached (CACHE_FIRST/CACHE_ONLY)
 * - Configurable refresh strategy based on cache policy
 *
 * **Error Handling:**
 * - Returns [ApiResponse.Error] if country not found
 * - Returns [ApiResponse.Error] if network/database fails
 * - Returns [ApiResponse.Loading] during data fetch
 * - Policy-specific error messages (e.g., "No cached data" for CACHE_ONLY)
 *
 * **Thread Safety:**
 * - Executes on IO dispatcher (configured via Hilt)
 * - Safe for concurrent calls
 *
 * @param repository The countries repository for data access
 * @param ioDispatcher The IO dispatcher for database/network operations
 *
 * @see CountriesRepository
 * @see FlowUseCase
 * @see Country
 * @see CachePolicy
 * @see CountryByCodeParams
 *
 * @since 1.1.0 (Enhanced in 2.0.0)
 *
 * Example usage:
 * ```kotlin
 * class CountryDetailsViewModel @Inject constructor(
 *     private val getCountryByCodeUseCase: GetCountryByCodeUseCase
 * ) : ViewModel() {
 *
 *     // Default load with cache-first strategy
 *     fun loadCountry(code: String) {
 *         viewModelScope.launch {
 *             getCountryByCodeUseCase(CountryByCodeParams(code))
 *                 .onSuccess { country ->
 *                     _uiState.value = UiState.Success(country)
 *                 }
 *                 .onError { exception ->
 *                     _uiState.value = UiState.Error(exception)
 *                 }
 *                 .collect()
 *         }
 *     }
 *
 *     // Force refresh on pull-to-refresh
 *     fun refresh(code: String) {
 *         viewModelScope.launch {
 *             getCountryByCodeUseCase(
 *                 CountryByCodeParams(code, CachePolicy.FORCE_REFRESH)
 *             ).collect { response -> /* ... */ }
 *         }
 *     }
 *
 *     // Offline mode
 *     fun loadOffline(code: String) {
 *         viewModelScope.launch {
 *             getCountryByCodeUseCase(
 *                 CountryByCodeParams(code, CachePolicy.CACHE_ONLY)
 *             ).collect { response -> /* ... */ }
 *         }
 *     }
 * }
 * ```
 */
open class GetCountryByCodeUseCase @Inject constructor(
    private val repository: CountriesRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : FlowUseCase<CountryByCodeParams, Country>(ioDispatcher) {

    /**
     * Executes the use case to retrieve a country by its three-letter code.
     *
     * **Phase 3 Enhancement:** Now accepts [CountryByCodeParams] with cache policy.
     *
     * **Flow Emissions:**
     * 1. [ApiResponse.Loading] - Initial loading state
     * 2. [ApiResponse.Success] - Country found and loaded
     * 3. [ApiResponse.Error] - Country not found or error occurred
     *
     * **Cache Policy Behavior:**
     * - **CACHE_FIRST**: Shows cache immediately, no background refresh for single item
     * - **NETWORK_FIRST**: Tries network first, falls back to cache on error
     * - **FORCE_REFRESH**: Always fetches fresh, no cache fallback
     * - **CACHE_ONLY**: Returns cache or error, never hits network
     *
     * @param parameters The country code and cache policy
     *
     * @return Flow emitting [ApiResponse] states with [Country] data
     *
     * @throws IllegalArgumentException if country code is empty or invalid format
     *
     * Example:
     * ```kotlin
     * // Default: cache-first
     * getCountryByCodeUseCase(CountryByCodeParams("USA"))
     *
     * // Force refresh
     * getCountryByCodeUseCase(
     *     CountryByCodeParams("USA", CachePolicy.FORCE_REFRESH)
     * )
     * ```
     */
    override fun execute(parameters: CountryByCodeParams): Flow<ApiResponse<Country>> {
        // Normalize country code to uppercase for consistent lookup
        val normalizedCode = parameters.code.uppercase().trim()
        
        // Validate country code format (should be 3 letters)
        require(normalizedCode.length == 3) {
            "Country code must be exactly 3 characters (ISO 3166-1 alpha-3)"
        }
        require(normalizedCode.all { it.isLetter() }) {
            "Country code must contain only letters"
        }
        
        return repository.getCountryByCode(normalizedCode, parameters.policy)
    }
}
