package com.vamsi.worldcountriesinformation.feature.widget.data

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.domainmodel.Country
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import javax.inject.Inject

/**
 * Data source for widget that fetches country information from the domain layer
 */
class WidgetDataSource @Inject constructor(
    private val getCountriesUseCase: GetCountriesUseCase,
) {
    /**
     * Get a random featured country for the widget
     *
     * Skips Loading states and waits for Success or Error response
     */
    suspend fun getFeaturedCountry(): Country? {
        return try {
            Timber.d("Widget: Fetching featured country")

            // Skip Loading states and wait for actual data (Success or Error)
            val response = getCountriesUseCase.invoke(CachePolicy.CACHE_FIRST)
                .firstOrNull { it !is ApiResponse.Loading }

            if (response == null) {
                Timber.w("Widget: No response received (still loading)")
                return null
            }

            Timber.d("Widget: Received response type: ${response::class.simpleName}")

            when (response) {
                is ApiResponse.Success -> {
                    val countries = response.data
                    Timber.d("Widget: Got ${countries.size} countries from use case")
                    when {
                        countries.isEmpty() -> {
                            Timber.w("Widget: Countries list is empty")
                            null
                        }

                        else -> {
                            // Get a random country, but use a deterministic approach based on day
                            // so the same country shows all day
                            val dayOfYear = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
                            val index = (dayOfYear % countries.size).toInt()
                            val country = countries[index]
                            Timber.d("Widget: Selected country: ${country.name} (index $index of ${countries.size})")
                            country
                        }
                    }
                }

                is ApiResponse.Error -> {
                    Timber.e(response.exception, "Widget: Error response from use case")
                    null
                }

                is ApiResponse.Loading -> {
                    // Should never reach here due to firstOrNull filter
                    Timber.w("Widget: Received Loading state unexpectedly")
                    null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching featured country for widget")
            null
        }
    }

    /**
     * Get total count of countries
     *
     * Skips Loading states and waits for Success or Error response
     */
    suspend fun getTotalCountriesCount(): Int {
        return try {
            Timber.d("Widget: Fetching total countries count")

            // Skip Loading states and wait for actual data
            val response = getCountriesUseCase.invoke(CachePolicy.CACHE_FIRST)
                .firstOrNull { it !is ApiResponse.Loading }

            val count = when (response) {
                is ApiResponse.Success -> response.data.size
                else -> 0
            }

            Timber.d("Widget: Total countries count: $count")
            count
        } catch (e: Exception) {
            Timber.e(e, "Error fetching total countries count")
            0
        }
    }

    /**
     * Get widget data combining featured country and count
     */
    suspend fun getWidgetData(): WidgetData {
        return try {
            val featuredCountry = getFeaturedCountry()
            val totalCount = getTotalCountriesCount()

            WidgetData(
                featuredCountry = featuredCountry,
                totalCountries = totalCount,
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            Timber.e(e, "Error fetching widget data")
            WidgetData(
                featuredCountry = null,
                totalCountries = 0,
                isLoading = false,
                error = e.message ?: "Unknown error"
            )
        }
    }
}



