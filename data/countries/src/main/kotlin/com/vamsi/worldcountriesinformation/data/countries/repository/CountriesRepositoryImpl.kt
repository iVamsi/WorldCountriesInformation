package com.vamsi.worldcountriesinformation.data.countries.repository

import com.vamsi.worldcountriesinformation.core.database.dao.CountryDao
import com.vamsi.worldcountriesinformation.core.network.WorldCountriesApi
import com.vamsi.worldcountriesinformation.data.countries.mapper.toCountries
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
