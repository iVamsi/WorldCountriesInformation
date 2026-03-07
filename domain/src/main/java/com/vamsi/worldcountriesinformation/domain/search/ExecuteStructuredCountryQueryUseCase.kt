package com.vamsi.worldcountriesinformation.domain.search

import com.vamsi.worldcountriesinformation.domain.countries.CountriesRepository
import com.vamsi.worldcountriesinformation.domain.countries.FilteredSearchCountriesUseCase
import com.vamsi.worldcountriesinformation.domainmodel.Country
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Executes structured country queries against the local repository data.
 */
class ExecuteStructuredCountryQueryUseCase @Inject constructor(
    private val countriesRepository: CountriesRepository,
    private val filteredSearchCountriesUseCase: FilteredSearchCountriesUseCase,
) {

    operator fun invoke(query: StructuredCountryQuery): Flow<List<Country>> {
        return countriesRepository.getCountriesFlow()
            .map { countries ->
                val textFilteredCountries = filterByTextQuery(
                    countries = countries,
                    textQuery = query.textQuery,
                )
                val filteredCountries = filteredSearchCountriesUseCase.applyFiltersAndSort(
                    countries = textFilteredCountries,
                    filters = query.filters,
                )
                val rankedCountries = applyDirectionalRanking(
                    countries = filteredCountries,
                    directionalRanking = query.directionalRanking,
                )
                limitResults(
                    countries = rankedCountries,
                    limit = query.limit,
                )
            }
    }

    private fun filterByTextQuery(
        countries: List<Country>,
        textQuery: String,
    ): List<Country> {
        if (textQuery.isBlank()) return countries

        val normalizedQuery = textQuery.trim().lowercase()

        return countries.filter { country ->
            listOf(
                country.name,
                country.capital,
                country.region,
                country.subregion,
                country.callingCode,
            ).any { value ->
                value.lowercase().contains(normalizedQuery)
            } || country.languages.any { language ->
                language.name.orEmpty().lowercase().contains(normalizedQuery) ||
                    language.nativeName.orEmpty().lowercase().contains(normalizedQuery)
            } || country.currencies.any { currency ->
                currency.code.orEmpty().lowercase().contains(normalizedQuery) ||
                    currency.name.orEmpty().lowercase().contains(normalizedQuery) ||
                    currency.symbol.orEmpty().lowercase().contains(normalizedQuery)
            }
        }
    }

    private fun applyDirectionalRanking(
        countries: List<Country>,
        directionalRanking: DirectionalRanking?,
    ): List<Country> {
        return when (directionalRanking) {
            DirectionalRanking.NORTHERNMOST -> countries.sortedByDescending { it.latitude }
            DirectionalRanking.SOUTHERNMOST -> countries.sortedBy { it.latitude }
            DirectionalRanking.EASTERNMOST -> countries.sortedByDescending { it.longitude }
            DirectionalRanking.WESTERNMOST -> countries.sortedBy { it.longitude }
            null -> countries
        }
    }

    private fun limitResults(
        countries: List<Country>,
        limit: Int?,
    ): List<Country> {
        if (limit == null) return countries
        if (limit <= 0) return emptyList()
        return countries.take(limit)
    }
}
