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
 * Use case for retrieving all countries with configurable cache strategy.
 *
 * This use case provides a clean interface for fetching country data from the repository
 * with fine-grained control over caching behavior through [CachePolicy].
 *
 * ## Phase 3 Enhancement
 *
 * **Updated to support Phase 2.4 cache policies:**
 * - Parameter changed from `Boolean` to `CachePolicy`
 * - Supports all 4 cache strategies (CACHE_FIRST, NETWORK_FIRST, FORCE_REFRESH, CACHE_ONLY)
 * - Backward compatible with default [CachePolicy.CACHE_FIRST]
 *
 * ## Cache Policy Usage
 *
 * **Default behavior (CACHE_FIRST):**
 * ```kotlin
 * getCountriesUseCase(CachePolicy.CACHE_FIRST)
 *     .collect { response -> /* ... */ }
 * ```
 *
 * **Pull-to-refresh (FORCE_REFRESH):**
 * ```kotlin
 * // User explicitly requests fresh data
 * getCountriesUseCase(CachePolicy.FORCE_REFRESH)
 *     .collect { response -> /* Always fresh or error */ }
 * ```
 *
 * **Offline mode (CACHE_ONLY):**
 * ```kotlin
 * // Never hit network, use cache only
 * getCountriesUseCase(CachePolicy.CACHE_ONLY)
 *     .collect { response -> /* Cached data or error */ }
 * ```
 *
 * **Critical data (NETWORK_FIRST):**
 * ```kotlin
 * // Always try network first, fallback to cache
 * getCountriesUseCase(CachePolicy.NETWORK_FIRST)
 *     .collect { response -> /* Fresh or cached */ }
 * ```
 *
 * @param repository Repository for country data access
 * @param ioDispatcher IO dispatcher for background operations
 *
 * @see CachePolicy for detailed policy descriptions
 * @see CountriesRepository.getCountries
 *
 * @since 1.0.0 (Enhanced in 2.0.0 with cache policies)
 */
open class GetCountriesUseCase @Inject constructor(
    private val repository: CountriesRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : FlowUseCase<CachePolicy, List<Country>>(ioDispatcher) {

    /**
     * Executes the use case to retrieve all countries.
     *
     * @param parameters Cache policy to use (default: CACHE_FIRST)
     * @return Flow of [ApiResponse] containing list of countries
     */
    override fun execute(parameters: CachePolicy): Flow<ApiResponse<List<Country>>> {
        return repository.getCountries(policy = parameters)
    }
}