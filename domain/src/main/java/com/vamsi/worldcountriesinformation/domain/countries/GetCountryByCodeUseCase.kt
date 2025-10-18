package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.FlowUseCase
import com.vamsi.worldcountriesinformation.domain.di.IoDispatcher
import com.vamsi.worldcountriesinformation.domainmodel.Country
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a single country by its three-letter code.
 *
 * This use case provides efficient single-country lookup without loading the entire
 * countries list, improving performance and reducing memory usage for detail screens.
 *
 * **Architecture:**
 * - Follows the Single Responsibility Principle
 * - Part of the Clean Architecture domain layer
 * - Returns reactive Flow for observing country changes
 * - Handles both cached and fresh data scenarios
 *
 * **Performance:**
 * - Database query with indexed lookup (O(1) complexity)
 * - No network call required if data is cached
 * - Optional network refresh for stale data
 *
 * **Error Handling:**
 * - Returns [ApiResponse.Error] if country not found
 * - Returns [ApiResponse.Error] if network/database fails
 * - Returns [ApiResponse.Loading] during data fetch
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
 *
 * @since 1.1.0
 *
 * Example usage:
 * ```kotlin
 * class CountryDetailsViewModel @Inject constructor(
 *     private val getCountryByCodeUseCase: GetCountryByCodeUseCase
 * ) : ViewModel() {
 *
 *     fun loadCountry(code: String) {
 *         viewModelScope.launch {
 *             getCountryByCodeUseCase(code)
 *                 .collect { response ->
 *                     when (response) {
 *                         is ApiResponse.Success -> showCountry(response.data)
 *                         is ApiResponse.Error -> showError(response.exception)
 *                         is ApiResponse.Loading -> showLoading()
 *                     }
 *                 }
 *         }
 *     }
 * }
 * ```
 */
open class GetCountryByCodeUseCase @Inject constructor(
    private val repository: CountriesRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, Country>(ioDispatcher) {

    /**
     * Executes the use case to retrieve a country by its three-letter code.
     *
     * **Flow Emissions:**
     * 1. [ApiResponse.Loading] - Initial loading state
     * 2. [ApiResponse.Success] - Country found and loaded
     * 3. [ApiResponse.Error] - Country not found or error occurred
     *
     * **Data Strategy:**
     * - First checks local database cache
     * - Returns cached data immediately if available
     * - Optionally refreshes from network if data is stale
     *
     * @param parameters The three-letter country code (e.g., "USA", "GBR", "JPN")
     *                   Case-insensitive, will be normalized to uppercase
     *
     * @return Flow emitting [ApiResponse] states with [Country] data
     *
     * @throws IllegalArgumentException if country code is empty or invalid format
     *
     * Example:
     * ```kotlin
     * getCountryByCodeUseCase("USA")
     *     .collect { response ->
     *         // Handle response
     *     }
     * ```
     */
    override fun execute(parameters: String): Flow<ApiResponse<Country>> {
        // Normalize country code to uppercase for consistent lookup
        val normalizedCode = parameters.uppercase().trim()
        
        // Validate country code format (should be 3 letters)
        require(normalizedCode.length == 3) {
            "Country code must be exactly 3 characters (ISO 3166-1 alpha-3)"
        }
        require(normalizedCode.all { it.isLetter() }) {
            "Country code must contain only letters"
        }
        
        return repository.getCountryByCode(normalizedCode)
    }
}
