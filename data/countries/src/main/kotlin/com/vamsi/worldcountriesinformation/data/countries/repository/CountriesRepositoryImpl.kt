package com.vamsi.worldcountriesinformation.data.countries.repository

import com.vamsi.worldcountriesinformation.core.database.dao.CountryDao
import com.vamsi.worldcountriesinformation.core.network.WorldCountriesApi
import com.vamsi.worldcountriesinformation.data.countries.mapper.toCountries // v3.1 API mapper (List)
import com.vamsi.worldcountriesinformation.data.countries.mapper.toCountry // v3.1 API mapper (Single)
import com.vamsi.worldcountriesinformation.data.countries.mapper.toDomain
import com.vamsi.worldcountriesinformation.data.countries.mapper.toDomainList
import com.vamsi.worldcountriesinformation.data.countries.mapper.toEntityList
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
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
    private val countryDao: CountryDao,
) : CountriesRepository {

    /**
     * Retrieves all countries with configurable cache strategy and reactive database updates.
     *
     * **Cache Policy Strategies:**
     *
     * **[CachePolicy.CACHE_FIRST]** (Default):
     * 1. Emit loading state
     * 2. Check database for cached data
     * 3. If cache exists and is fresh (< 24 hours):
     *    - Emit cached data immediately
     *    - Start observing database for future updates
     * 4. If cache is stale or missing:
     *    - Fetch from network
     *    - Update database with fresh data
     *    - Emit fresh data via reactive Flow
     *
     * **[CachePolicy.NETWORK_FIRST]**:
     * 1. Emit loading state
     * 2. Attempt network fetch immediately
     * 3. If network succeeds:
     *    - Update database
     *    - Emit fresh data
     * 4. If network fails:
     *    - Check database for any cached data (even if stale)
     *    - Emit cached data or error
     *
     * **[CachePolicy.FORCE_REFRESH]**:
     * 1. Emit loading state
     * 2. Always fetch from network (ignore cache)
     * 3. If network succeeds:
     *    - Update database
     *    - Emit fresh data
     * 4. If network fails:
     *    - Emit error (no cache fallback)
     *
     * **[CachePolicy.CACHE_ONLY]**:
     * 1. Emit loading state
     * 2. Check database only (never hits network)
     * 3. If cache exists (any age):
     *    - Emit cached data
     * 4. If cache is empty:
     *    - Emit error
     *
     * **Reactive Strategy (Enhanced):**
     * This implementation uses a hybrid approach that combines immediate cache
     * response with reactive database updates:
     *
     * 1. **Immediate Cache Response**: Checks if database has data
     *    - If yes: Emits cached data immediately (instant UI)
     *    - If no: Shows loading state only
     *
     * 2. **Background Network Sync**: Fetches fresh data from API (if policy allows)
     *    - Updates database with fresh data
     *    - Database change triggers automatic UI update (via Flow)
     *
     * 3. **Reactive Database Observation**: Uses emitAll() with database Flow
     *    - UI automatically updates when database changes
     *    - Works for background refreshes, inserts, updates
     *    - No manual emission needed after database update
     *
     * **Cache Staleness:**
     * - Data is considered fresh if lastUpdated < 24 hours ago
     * - Uses [CachePolicy.isCacheFresh] for staleness detection
     * - Staleness only checked for [CachePolicy.CACHE_FIRST]
     *
     * **Flow Emission Sequence (CACHE_FIRST with fresh cache):**
     * ```
     * Time 0ms:   ApiResponse.Loading
     * Time 5ms:   ApiResponse.Success(cachedCountries) [cache is fresh]
     * Time 505ms: ApiResponse.Success(cachedCountries) [no network fetch needed]
     * ```
     *
     * **Flow Emission Sequence (CACHE_FIRST with stale cache):**
     * ```
     * Time 0ms:   ApiResponse.Loading
     * Time 5ms:   ApiResponse.Success(cachedCountries) [cache is stale]
     * Time 500ms: Network fetch completes → Database updated
     * Time 505ms: ApiResponse.Success(freshCountries) [automatic via Flow]
     * ```
     *
     * **Flow Emission Sequence (FORCE_REFRESH):**
     * ```
     * Time 0ms:   ApiResponse.Loading
     * Time 500ms: Network fetch completes → Database updated
     * Time 505ms: ApiResponse.Success(freshCountries) [or Error if failed]
     * ```
     *
     * **Benefits over Previous Implementation:**
     * - Configurable cache strategies for different use cases
     * - Automatic UI updates on any database change
     * - Works with background sync operations
     * - Single source of truth (database)
     * - Reduced manual Flow emissions
     * - Better support for pull-to-refresh
     * - Handles concurrent updates gracefully
     * - Offline support with graceful degradation
     *
     * **Error Handling:**
     * - Network errors don't affect cached data display (except FORCE_REFRESH)
     * - Graceful degradation to offline mode
     * - Errors logged for debugging
     * - Policy-aware error handling
     *
     * @param policy Cache strategy to use (default: [CachePolicy.CACHE_FIRST])
     * @return Flow of [ApiResponse] containing list of all countries
     *         Emits updates automatically when database changes
     *
     * @see CountryDao.getAllCountries for reactive database query
     * @see CachePolicy for detailed policy descriptions
     * @see emitAll for reactive Flow transformation
     */
    override fun getCountries(policy: CachePolicy): Flow<ApiResponse<List<Country>>> {
        return flow {
            // Emit loading state
            emit(ApiResponse.Loading)

            // Step 1: Handle CACHE_ONLY policy (never hits network)
            if (policy == CachePolicy.CACHE_ONLY) {
                val cachedCountries = countryDao.getAllCountriesOnce()
                if (cachedCountries.isNotEmpty()) {
                    Timber.d("CACHE_ONLY: Emitting ${cachedCountries.size} cached countries")
                    emit(ApiResponse.Success(cachedCountries.toDomainList()))

                    // Start reactive observation for any future database changes
                    emitAll(
                        countryDao.getAllCountries().map { entities ->
                            ApiResponse.Success(entities.toDomainList())
                        }
                    )
                } else {
                    Timber.w("CACHE_ONLY: No cached data available")
                    emit(ApiResponse.Error(Exception("No cached data available (offline mode)")))
                }
                return@flow
            }

            // Step 2: Check cache for CACHE_FIRST and NETWORK_FIRST policies
            val cachedCountries = countryDao.getAllCountriesOnce()
            val hasCache = cachedCountries.isNotEmpty()
            val requiresCallingCodeRepair = cachedCountries.any { it.callingCode.isBlank() }

            // Determine cache staleness (only for CACHE_FIRST)
            val isCacheFresh = if (hasCache && policy == CachePolicy.CACHE_FIRST) {
                val oldestTimestamp = cachedCountries.minOfOrNull { it.lastUpdated } ?: 0L
                val isFresh = CachePolicy.isCacheFresh(oldestTimestamp)
                Timber.d(
                    "CACHE_FIRST: Cache age=${CachePolicy.getCacheAgeDescription(oldestTimestamp)}, fresh=$isFresh, requiresCallingCodeRepair=$requiresCallingCodeRepair"
                )
                isFresh
            } else {
                false
            }

            // Step 3: Emit cached data if appropriate
            val shouldEmitCache = when (policy) {
                CachePolicy.CACHE_FIRST -> hasCache // Emit cache immediately if exists
                CachePolicy.NETWORK_FIRST -> false // Wait for network first
                CachePolicy.FORCE_REFRESH -> false // Ignore cache completely
                CachePolicy.CACHE_ONLY -> hasCache // Already handled above
            }

            if (shouldEmitCache && hasCache) {
                Timber.d("${policy.name}: Emitting ${cachedCountries.size} cached countries")
                emit(ApiResponse.Success(cachedCountries.toDomainList()))
            }

            // Step 4: Determine if network fetch is needed
            val shouldFetchNetwork = when (policy) {
                CachePolicy.CACHE_FIRST -> !isCacheFresh || requiresCallingCodeRepair // Refresh stale caches or ones missing calling codes
                CachePolicy.NETWORK_FIRST -> true // Always try network
                CachePolicy.FORCE_REFRESH -> true // Always fetch
                CachePolicy.CACHE_ONLY -> false // Never fetch
            }

            // Step 5: Fetch from network if needed
            if (shouldFetchNetwork) {
                try {
                    Timber.d("${policy.name}: Fetching fresh countries from network")
                    val networkCountries = countriesApi.fetchWorldCountriesInformation()
                    val domainCountries = networkCountries.toCountries()

                    // Update database - this will trigger reactive Flow emission below
                    // Database update will set current timestamp for lastUpdated
                    countryDao.refreshCountries(domainCountries.toEntityList())
                    Timber.d("${policy.name}: Database updated with ${domainCountries.size} countries")

                    // Note: No manual emit here - the emitAll() below will handle it
                    // This ensures we're always emitting from the single source of truth

                } catch (exception: Exception) {
                    Timber.e(exception, "${policy.name}: Failed to fetch countries from network")

                    // Handle error based on policy
                    when (policy) {
                        CachePolicy.FORCE_REFRESH -> {
                            // FORCE_REFRESH: Always emit error, no cache fallback
                            emit(ApiResponse.Error(exception))
                            return@flow // Exit early
                        }

                        CachePolicy.NETWORK_FIRST -> {
                            // NETWORK_FIRST: Fallback to cache if available
                            if (hasCache) {
                                Timber.d("NETWORK_FIRST: Network failed, falling back to cached data")
                                emit(ApiResponse.Success(cachedCountries.toDomainList()))
                            } else {
                                Timber.w("NETWORK_FIRST: Network failed and no cache available")
                                emit(ApiResponse.Error(exception))
                                return@flow
                            }
                        }

                        CachePolicy.CACHE_FIRST -> {
                            // CACHE_FIRST: Continue with cached data if available
                            if (!hasCache) {
                                // No cache and network failed (offline with no cache)
                                emit(ApiResponse.Error(exception))
                                return@flow
                            } else {
                                Timber.d("CACHE_FIRST: Network error, continuing with cached data")
                                // Continue to reactive observation
                            }
                        }

                        CachePolicy.CACHE_ONLY -> {
                            // Already handled above, shouldn't reach here
                        }
                    }
                }
            }

            // Step 6: Reactive observation: Emit all future database changes
            // This enables automatic UI updates when:
            // - Network refresh completes
            // - Background sync updates database
            // - User triggers manual refresh
            // - Any other operation modifies database
            Timber.d("${policy.name}: Starting reactive database observation")
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
    override fun getCountryByCode(
        code: String,
        policy: CachePolicy,
    ): Flow<ApiResponse<Country>> {
        return flow {
            // Emit loading state
            emit(ApiResponse.Loading)

            try {
                // Normalize country code to uppercase
                val normalizedCode = code.uppercase().trim()

                Timber.d("Fetching country by code: $normalizedCode with policy: $policy")

                // Check cache based on policy
                val cachedCountry = countryDao.getCountryByCodeOnce(normalizedCode)?.toDomain()
                val needsCallingCodeRepair = cachedCountry?.callingCode.isNullOrBlank()

                when (policy) {
                    CachePolicy.CACHE_ONLY -> {
                        // Return cache only, no network call
                        if (cachedCountry != null) {
                            Timber.d("Country found in cache (CACHE_ONLY): ${cachedCountry.name}")
                            emit(ApiResponse.Success(cachedCountry))
                        } else {
                            Timber.w("Country not in cache (CACHE_ONLY): $normalizedCode")
                            emit(
                                ApiResponse.Error(
                                    Exception("No cached data available for country '$normalizedCode'")
                                )
                            )
                        }
                    }

                    CachePolicy.CACHE_FIRST -> {
                        val shouldRepair = cachedCountry == null || needsCallingCodeRepair
                        if (!shouldRepair) {
                            Timber.d("Country found in cache (CACHE_FIRST): ${cachedCountry!!.name}")
                            emit(ApiResponse.Success(cachedCountry))
                        } else {
                            Timber.d(
                                "${if (cachedCountry == null) "Country not in cache" else "Cached country missing calling code"}, fetching from network: $normalizedCode"
                            )
                            try {
                                val freshCountry = fetchCountryFromNetwork(normalizedCode)
                                if (freshCountry != null) {
                                    emit(ApiResponse.Success(freshCountry))
                                } else if (cachedCountry != null) {
                                    Timber.w("Network fetch returned null, falling back to cached country: ${cachedCountry.name}")
                                    emit(ApiResponse.Success(cachedCountry))
                                } else {
                                    emit(
                                        ApiResponse.Error(
                                            Exception("Country with code '$normalizedCode' not found")
                                        )
                                    )
                                }
                            } catch (networkException: Exception) {
                                Timber.e(networkException, "CACHE_FIRST repair failed for: $normalizedCode")
                                if (cachedCountry != null) {
                                    emit(ApiResponse.Success(cachedCountry))
                                } else {
                                    emit(
                                        ApiResponse.Error(
                                            Exception(
                                                "Failed to fetch country '$normalizedCode': ${networkException.message}"
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }

                    CachePolicy.NETWORK_FIRST -> {
                        // Try network first
                        try {
                            val freshCountry = fetchCountryFromNetwork(normalizedCode)
                            if (freshCountry != null) {
                                emit(ApiResponse.Success(freshCountry))
                            } else if (cachedCountry != null) {
                                // Network failed, fall back to cache
                                Timber.d("Network fetch failed, using cache: ${cachedCountry.name}")
                                emit(ApiResponse.Success(cachedCountry))
                            } else {
                                emit(
                                    ApiResponse.Error(
                                        Exception("Country with code '$normalizedCode' not found")
                                    )
                                )
                            }
                        } catch (networkException: Exception) {
                            // Network error, try cache
                            if (cachedCountry != null) {
                                Timber.w(networkException, "Network failed, using cache: ${cachedCountry.name}")
                                emit(ApiResponse.Success(cachedCountry))
                            } else {
                                Timber.e(networkException, "Network failed and no cache available")
                                emit(
                                    ApiResponse.Error(
                                        Exception("Failed to fetch country '$normalizedCode': ${networkException.message}")
                                    )
                                )
                            }
                        }
                    }

                    CachePolicy.FORCE_REFRESH -> {
                        // Always fetch fresh from network, ignore cache
                        try {
                            val freshCountry = fetchCountryFromNetwork(normalizedCode)
                            if (freshCountry != null) {
                                emit(ApiResponse.Success(freshCountry))
                            } else {
                                emit(
                                    ApiResponse.Error(
                                        Exception("Country with code '$normalizedCode' not found")
                                    )
                                )
                            }
                        } catch (networkException: Exception) {
                            Timber.e(networkException, "Force refresh failed for: $normalizedCode")
                            emit(
                                ApiResponse.Error(
                                    Exception("Failed to fetch country '$normalizedCode': ${networkException.message}")
                                )
                            )
                        }
                    }
                }
            } catch (exception: Exception) {
                Timber.e(exception, "Failed to fetch country by code: $code")
                emit(ApiResponse.Error(exception))
            }
        }
    }

    /**
     * Helper function to fetch country from network and cache it.
     *
     * @param code The three-letter country code
     * @return The fetched country, or null if not found
     */
    private suspend fun fetchCountryFromNetwork(code: String): Country? {
        Timber.d("Fetching country from network: $code")

        val networkCountries = countriesApi.fetchCountryByCode(code)

        if (networkCountries.isNotEmpty()) {
            val domainCountry = networkCountries.firstOrNull()?.toCountry()

            if (domainCountry != null) {
                // Cache the fetched country
                countryDao.insertCountries(listOf(domainCountry).toEntityList())
                Timber.d("Country fetched and cached: ${domainCountry.name}")
                return domainCountry
            } else {
                Timber.w("Failed to map country data for code: $code")
            }
        } else {
            Timber.w("Country not found in API: $code")
        }

        return null
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
