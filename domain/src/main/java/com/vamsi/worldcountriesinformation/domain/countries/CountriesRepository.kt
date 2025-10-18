package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
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
     * Retrieves all countries from the repository.
     *
     * **Strategy:**
     * - Returns cached data immediately if available
     * - Fetches fresh data from network in background
     * - Updates cache with fresh data
     * - Emits updates as they become available
     *
     * **Flow Emissions:**
     * 1. [ApiResponse.Loading] - Initial loading state
     * 2. [ApiResponse.Success] with cached data (if available)
     * 3. [ApiResponse.Success] with fresh data (after network fetch)
     * 4. [ApiResponse.Error] if both cache and network fail
     *
     * @return Flow of [ApiResponse] containing list of all countries
     *
     * Example:
     * ```kotlin
     * repository.getCountries()
     *     .collect { response ->
     *         when (response) {
     *             is ApiResponse.Success -> showCountries(response.data)
     *             is ApiResponse.Error -> showError(response.exception)
     *             is ApiResponse.Loading -> showLoading()
     *         }
     *     }
     * ```
     */
    fun getCountries(): Flow<ApiResponse<List<Country>>>
    
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
}