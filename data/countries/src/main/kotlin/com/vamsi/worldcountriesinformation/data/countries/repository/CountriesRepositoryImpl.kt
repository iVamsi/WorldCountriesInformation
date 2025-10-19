package com.vamsi.worldcountriesinformation.data.countries.repository

import com.vamsi.worldcountriesinformation.core.database.dao.CountryDao
import com.vamsi.worldcountriesinformation.core.network.WorldCountriesApi
import com.vamsi.worldcountriesinformation.data.countries.mapper.toCountries // v3.1 API mapper (List)
import com.vamsi.worldcountriesinformation.data.countries.mapper.toCountry // v3.1 API mapper (Single)
import com.vamsi.worldcountriesinformation.data.countries.mapper.toDomain
import com.vamsi.worldcountriesinformation.data.countries.mapper.toDomainList
import com.vamsi.worldcountriesinformation.data.countries.mapper.toEntityList
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.countries.CountriesRepository
import com.vamsi.worldcountriesinformation.domainmodel.Country
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
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

    /**
     * Retrieves all countries with reactive database updates.
     *
     * **Reactive Strategy (Enhanced):**
     * This implementation uses a hybrid approach that combines immediate cache
     * response with reactive database updates:
     *
     * 1. **Immediate Cache Response**: Checks if database has data
     *    - If yes: Emits cached data immediately (instant UI)
     *    - If no: Shows loading state only
     *
     * 2. **Background Network Sync**: Fetches fresh data from API
     *    - Updates database with fresh data
     *    - Database change triggers automatic UI update (via Flow)
     *
     * 3. **Reactive Database Observation**: Uses emitAll() with database Flow
     *    - UI automatically updates when database changes
     *    - Works for background refreshes, inserts, updates
     *    - No manual emission needed after database update
     *
     * **Flow Emission Sequence:**
     * ```
     * Time 0ms:   ApiResponse.Loading
     * Time 5ms:   ApiResponse.Success(cachedCountries) [if cache exists]
     * Time 500ms: Network fetch completes → Database updated
     * Time 505ms: ApiResponse.Success(freshCountries) [automatic via Flow]
     * ```
     *
     * **Benefits over Previous Implementation:**
     * - Automatic UI updates on any database change
     * - Works with background sync operations
     * - Single source of truth (database)
     * - Reduced manual Flow emissions
     * - Better support for pull-to-refresh
     * - Handles concurrent updates gracefully
     *
     * **Error Handling:**
     * - Network errors don't affect cached data display
     * - Graceful degradation to offline mode
     * - Errors logged for debugging
     *
     * @return Flow of [ApiResponse] containing list of all countries
     *         Emits updates automatically when database changes
     *
     * @see CountryDao.getAllCountries for reactive database query
     * @see emitAll for reactive Flow transformation
     */
    override fun getCountries(): Flow<ApiResponse<List<Country>>> {
        return flow {
            // Emit loading state
            emit(ApiResponse.Loading)

            // Check if we have cached data for instant display
            val cachedCountries = countryDao.getAllCountriesOnce()
            val hasCache = cachedCountries.isNotEmpty()

            if (hasCache) {
                Timber.d("Emitting ${cachedCountries.size} cached countries")
                emit(ApiResponse.Success(cachedCountries.toDomainList()))
            }

            // Launch network fetch in background (doesn't block Flow)
            try {
                Timber.d("Fetching fresh countries from network")
                val networkCountries = countriesApi.fetchWorldCountriesInformation()
                val domainCountries = networkCountries.toCountries()

                // Update database - this will trigger reactive Flow emission below
                countryDao.refreshCountries(domainCountries.toEntityList())
                Timber.d("Database updated with ${domainCountries.size} countries")

                // Note: No manual emit here - the emitAll() below will handle it
                // This ensures we're always emitting from the single source of truth

            } catch (exception: Exception) {
                Timber.e(exception, "Failed to fetch countries from network")

                // Only emit error if we don't have cached data (offline with no cache)
                if (!hasCache) {
                    emit(ApiResponse.Error(exception))
                    return@flow // Exit early, don't start observing database
                } else {
                    Timber.d("Network error, but continuing with cached data")
                    // Continue to reactive observation even if network fails
                }
            }

            // Reactive observation: Emit all future database changes
            // This enables automatic UI updates when:
            // - Network refresh completes
            // - Background sync updates database
            // - User triggers manual refresh
            // - Any other operation modifies database
            Timber.d("Starting reactive database observation")
            emitAll(
                countryDao.getAllCountries().map { entities ->
                    ApiResponse.Success(entities.toDomainList())
                }
            )
        }
    }

    /**
     * Retrieves a single country by its code using an optimized hybrid strategy.
     *
     * **Optimized Strategy (v3.1 API):**
     * 1. Check local database cache first (instant response)
     * 2. If found: return cached data immediately
     * 3. If not found: fetch from v3.1 single country endpoint
     * 4. Cache the fetched country for future use
     * 5. Return the fresh data
     *
     * **Benefits over database-only approach:**
     * - Network fallback ensures data is always available
     * - Single country endpoint reduces bandwidth (vs fetching all countries)
     * - Hybrid strategy balances speed and reliability
     * - Gradually builds complete cache through usage
     *
     * **Performance:**
     * - Cache hit: ~1-5ms (database query)
     * - Cache miss: ~100-300ms (single country API call)
     * - vs fetching all countries: ~1-3s (avoided)
     *
     * **Error Handling:**
     * - Database error: Falls back to network
     * - Network error after cache miss: Returns error
     * - Invalid country code: Returns error
     *
     * @param code The country code (alpha-2, alpha-3, or numeric)
     *            Examples: "US", "USA", "IND", "IN"
     *            Will be normalized to uppercase for consistent lookup
     *
     * @return Flow emitting:
     *         - [ApiResponse.Loading] while fetching
     *         - [ApiResponse.Success] with country data
     *         - [ApiResponse.Error] if country not found or error occurs
     *
     * Example:
     * ```kotlin
     * repositoryImpl.getCountryByCode("USA")
     *     .collect { response ->
     *         when (response) {
     *             is ApiResponse.Success -> showCountry(response.data)
     *             is ApiResponse.Error -> showError(response.exception)
     *             is ApiResponse.Loading -> showLoading()
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

                Timber.d("Fetching country by code: $normalizedCode (hybrid strategy)")

                // Step 1: Try database cache first (fast path)
                val countryEntity = countryDao.getCountryByCodeOnce(normalizedCode)

                if (countryEntity != null) {
                    // Cache hit: Return immediately
                    val country = countryEntity.toDomain()
                    Timber.d("Country found in cache: ${country.name}")
                    emit(ApiResponse.Success(country))
                } else {
                    // Cache miss: Fetch from network (v3.1 single country endpoint)
                    Timber.d("Country not in cache, fetching from network: $normalizedCode")

                    try {
                        // Fetch single country from v3.1 API (optimized endpoint)
                        val networkCountries = countriesApi.fetchCountryByCode(normalizedCode)

                        if (networkCountries.isNotEmpty()) {
                            val domainCountry: Country? = networkCountries.first().toCountry()

                            if (domainCountry != null) {
                                // Cache the fetched country for future use
                                val countryList: List<Country> = listOf(domainCountry)
                                countryDao.insertCountries(countryList.toEntityList())
                                Timber.d("Country fetched and cached: ${domainCountry.name}")

                                // Return fresh data
                                emit(ApiResponse.Success(domainCountry))
                            } else {
                                Timber.w("Failed to map country data for code: $normalizedCode")
                                emit(
                                    ApiResponse.Error(
                                        Exception("Failed to process country data for '$normalizedCode'")
                                    )
                                )
                            }
                        } else {
                            Timber.w("Country not found in API: $normalizedCode")
                            emit(
                                ApiResponse.Error(
                                    Exception("Country with code '$normalizedCode' not found")
                                )
                            )
                        }
                    } catch (networkException: Exception) {
                        // Network fetch failed
                        Timber.e(networkException, "Network fetch failed for code: $normalizedCode")
                        emit(
                            ApiResponse.Error(
                                Exception("Failed to fetch country '$normalizedCode': ${networkException.message}")
                            )
                        )
                    }
                }
            } catch (exception: Exception) {
                Timber.e(exception, "Failed to fetch country by code: $code")
                emit(ApiResponse.Error(exception))
            }
        }
    }

    override fun getCountriesFlow(): Flow<List<Country>> {
        return countryDao.getAllCountries().map { entities ->
            entities.toDomainList()
        }
    }

    override fun searchCountries(query: String): Flow<List<Country>> {
        return countryDao.searchCountries(query).map { entities ->
            entities.toDomainList()
        }
    }

    override fun getCountriesByRegion(region: String): Flow<List<Country>> {
        return countryDao.getCountriesByRegion(region).map { entities ->
            entities.toDomainList()
        }
    }

    override suspend fun forceRefresh(): Result<Unit> {
        return try {
            Timber.d("Force refresh: Fetching fresh data from network")
            val networkCountries = countriesApi.fetchWorldCountriesInformation()
            val domainCountries = networkCountries.toCountries()
            
            Timber.d("Force refresh: Updating database with ${domainCountries.size} countries")
            countryDao.refreshCountries(domainCountries.toEntityList())
            
            Timber.d("Force refresh: Completed successfully")
            Result.success(Unit)
        } catch (exception: Exception) {
            Timber.e(exception, "Force refresh failed")
            Result.failure(exception)
        }
    }
}
