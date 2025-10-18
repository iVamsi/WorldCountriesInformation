package com.vamsi.worldcountriesinformation.data.countries.repository

import com.vamsi.worldcountriesinformation.core.database.dao.CountryDao
import com.vamsi.worldcountriesinformation.core.network.WorldCountriesApi
import com.vamsi.worldcountriesinformation.data.countries.mapper.toCountries
import com.vamsi.worldcountriesinformation.data.countries.mapper.toDomain
import com.vamsi.worldcountriesinformation.data.countries.mapper.toDomainList
import com.vamsi.worldcountriesinformation.data.countries.mapper.toEntityList
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.countries.CountriesRepository
import com.vamsi.worldcountriesinformation.domainmodel.Country
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * Repository implementation with offline-first architecture
 * Strategy: Database is the single source of truth
 * - First, emit cached data from database
 * - Then, fetch fresh data from network
 * - Update database with fresh data
 * - Database changes automatically propagate to UI via Flow
 */
class CountriesRepositoryImpl @Inject constructor(
    private val countriesApi: WorldCountriesApi,
    private val countryDao: CountryDao
) : CountriesRepository {

    override fun getCountries(): Flow<ApiResponse<List<Country>>> {
        return flow {
            // Emit loading state
            emit(ApiResponse.Loading)

            // First, try to get cached data from database
            val cachedCountries = countryDao.getAllCountriesOnce()

            if (cachedCountries.isNotEmpty()) {
                // Emit cached data immediately for instant UI
                Timber.d("Emitting ${cachedCountries.size} cached countries")
                emit(ApiResponse.Success(cachedCountries.toDomainList()))
            }

            // Then fetch fresh data from network
            try {
                Timber.d("Fetching fresh countries from network")
                val networkCountries = countriesApi.fetchWorldCountriesInformation()
                val domainCountries = networkCountries.toCountries()

                // Update database with fresh data
                countryDao.refreshCountries(domainCountries.toEntityList())
                Timber.d("Updated database with ${domainCountries.size} countries")

                // Emit fresh data
                emit(ApiResponse.Success(domainCountries))

            } catch (exception: Exception) {
                Timber.e(exception, "Failed to fetch countries from network")

                // If we have cached data, don't emit error (offline mode)
                if (cachedCountries.isEmpty()) {
                    emit(ApiResponse.Error(exception))
                } else {
                    Timber.d("Using cached data due to network error")
                    // We already emitted cached data above, so just log
                }
            }
        }
    }

    /**
     * Retrieves a single country by its three-letter code.
     *
     * This method implements an efficient single-country lookup strategy:
     * 1. Query database using primary key index (O(1) lookup)
     * 2. Return cached data immediately if available
     * 3. Skip network call to avoid unnecessary data transfer
     *
     * **Why no network call?**
     * - Single country data is part of the full countries dataset
     * - Full list is periodically refreshed via getCountries()
     * - Avoids redundant network requests
     * - Improves performance and reduces data usage
     *
     * **Error Scenarios:**
     * - Country not found in database → Returns [ApiResponse.Error]
     * - Database query fails → Returns [ApiResponse.Error]
     * - Invalid country code format → Returns [ApiResponse.Error]
     *
     * @param code The three-letter country code (ISO 3166-1 alpha-3)
     *            Will be normalized to uppercase for consistent lookup
     *
     * @return Flow emitting country data or error
     *
     * @see getCountries for full list refresh strategy
     *
     * Example:
     * ```kotlin
     * repositoryImpl.getCountryByCode("USA")
     *     .collect { response ->
     *         when (response) {
     *             is ApiResponse.Success -> {
     *                 // Country found: response.data
     *             }
     *             is ApiResponse.Error -> {
     *                 // Country not found or error occurred
     *             }
     *             is ApiResponse.Loading -> {
     *                 // Show loading indicator
     *             }
     *         }
     *     }
     * ```
     */
    override fun getCountryByCode(code: String): Flow<ApiResponse<Country>> {
        return flow {
            // Emit loading state
            emit(ApiResponse.Loading)
            
            try {
                // Normalize country code to uppercase
                val normalizedCode = code.uppercase().trim()
                
                Timber.d("Fetching country by code: $normalizedCode")
                
                // Query database using primary key index
                val countryEntity = countryDao.getCountryByCodeOnce(normalizedCode)
                
                if (countryEntity != null) {
                    // Country found in database
                    val country = countryEntity.toDomain()
                    Timber.d("Country found: ${country.name}")
                    emit(ApiResponse.Success(country))
                } else {
                    // Country not found in database
                    Timber.w("Country not found for code: $normalizedCode")
                    emit(
                        ApiResponse.Error(
                            Exception("Country with code '$normalizedCode' not found")
                        )
                    )
                }
            } catch (exception: Exception) {
                Timber.e(exception, "Failed to fetch country by code: $code")
                emit(ApiResponse.Error(exception))
            }
        }
    }

    /**
     * Get countries as a reactive Flow (observes database changes)
     */
    fun getCountriesFlow(): Flow<List<Country>> {
        return countryDao.getAllCountries().map { entities ->
            entities.toDomainList()
        }
    }

    /**
     * Search countries by name
     */
    fun searchCountries(query: String): Flow<List<Country>> {
        return countryDao.searchCountries(query).map { entities ->
            entities.toDomainList()
        }
    }

    /**
     * Get countries by region
     */
    fun getCountriesByRegion(region: String): Flow<List<Country>> {
        return countryDao.getCountriesByRegion(region).map { entities ->
            entities.toDomainList()
        }
    }

    /**
     * Force refresh from network
     */
    suspend fun forceRefresh(): Result<Unit> {
        return try {
            val networkCountries = countriesApi.fetchWorldCountriesInformation()
            val domainCountries = networkCountries.toCountries()
            countryDao.refreshCountries(domainCountries.toEntityList())
            Result.success(Unit)
        } catch (exception: Exception) {
            Timber.e(exception, "Force refresh failed")
            Result.failure(exception)
        }
    }
}
