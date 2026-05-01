package com.vamsi.worldcountriesinformation.feature.widget.data

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.domain.di.IoDispatcher
import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Data source for widget that fetches country information from the domain layer
 */
class WidgetDataSource @Inject constructor(
    private val getCountriesUseCase: GetCountriesUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Get widget data combining featured country and count
     */
    suspend fun getWidgetData(): WidgetData = withContext(ioDispatcher) {
        try {
            Timber.d("Widget: Fetching widget data")

            val response = getCountriesUseCase.invoke(CachePolicy.CACHE_FIRST)
                .firstOrNull { it !is ApiResponse.Loading }

            if (response == null) {
                Timber.w("Widget: No response received (still loading)")
                return@withContext WidgetData(
                    featuredCountry = null,
                    totalCountries = 0,
                    isLoading = false,
                    error = null
                )
            }

            Timber.d("Widget: Received response type: ${response::class.simpleName}")

            when (response) {
                is ApiResponse.Success -> {
                    val countries = response.data
                    Timber.d("Widget: Got ${countries.size} countries from use case")
                    if (countries.isEmpty()) {
                        Timber.w("Widget: Countries list is empty")
                        WidgetData(
                            featuredCountry = null,
                            totalCountries = 0,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        val featured = pickFeaturedCountry(countries)
                        Timber.d(
                            "Widget: Selected country: ${featured?.name}, total=${countries.size}"
                        )
                        WidgetData(
                            featuredCountry = featured,
                            totalCountries = countries.size,
                            isLoading = false,
                            error = null
                        )
                    }
                }

                is ApiResponse.Error -> {
                    Timber.e(response.exception, "Widget: Error response from use case")
                    WidgetData(
                        featuredCountry = null,
                        totalCountries = 0,
                        isLoading = false,
                        error = null
                    )
                }

                is ApiResponse.Loading -> {
                    Timber.w("Widget: Received Loading state unexpectedly")
                    WidgetData(
                        featuredCountry = null,
                        totalCountries = 0,
                        isLoading = false,
                        error = null
                    )
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
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

internal fun pickFeaturedCountry(countries: List<CountrySummary>): CountrySummary? {
    if (countries.isEmpty()) return null
    val dayOfYear = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
    val index = (dayOfYear % countries.size).toInt()
    return countries[index]
}
