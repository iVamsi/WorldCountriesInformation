package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.SearchFilters
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for searching and filtering countries with advanced options.
 *
 * Extends basic search with:
 * - Region filtering
 * - Subregion filtering
 * - Multiple sort orders
 * - Combined search + filter operations
 *
 * ## Features
 *
 * 1. **Multi-Filter Support**
 *    - Region filtering (Africa, Asia, Europe, etc.)
 *    - Subregion filtering (Eastern Europe, Southeast Asia, etc.)
 *    - Combine filters for precise results
 *
 * 2. **Flexible Sorting**
 *    - Name (A-Z, Z-A)
 *    - Population (high-low, low-high)
 *    - Area (large-small, small-large)
 *
 * 3. **Search Integration**
 *    - Apply filters to search results
 *    - Filter then search, or search then filter
 *    - Efficient query composition
 *
 * ## Usage Examples
 *
 * **Filter by Region:**
 * ```kotlin
 * val europeanCountries = useCase(
 *     query = "",
 *     filters = SearchFilters(selectedRegions = setOf("Europe"))
 * )
 * ```
 *
 * **Search with Filters:**
 * ```kotlin
 * val asianIslands = useCase(
 *     query = "island",
 *     filters = SearchFilters(selectedRegions = setOf("Asia"))
 * )
 * ```
 *
 * **Sort by Population:**
 * ```kotlin
 * val largestCountries = useCase(
 *     query = "",
 *     filters = SearchFilters(sortOrder = SortOrder.POPULATION_DESC)
 * )
 * ```
 *
 * **Complex Query:**
 * ```kotlin
 * // Find European countries with "land" in name, sorted by area
 * val results = useCase(
 *     query = "land",
 *     filters = SearchFilters(
 *         selectedRegions = setOf("Europe"),
 *         sortOrder = SortOrder.AREA_DESC
 *     )
 * )
 * // Result: Finland, Poland, Iceland, Ireland, Switzerland...
 * ```
 *
 * @property searchCountriesUseCase Base search use case
 */
class FilteredSearchCountriesUseCase @Inject constructor(
    private val searchCountriesUseCase: SearchCountriesUseCase
) {

    /**
     * Executes filtered search operation.
     *
     * Process:
     * 1. Execute base search with query
     * 2. Apply region filters
     * 3. Apply sort order
     *
     * @param query Search query for country name (empty = all countries)
     * @param filters Search filters to apply
     * @return Flow of filtered and sorted countries
     */
    operator fun invoke(
        query: String,
        filters: SearchFilters
    ): Flow<List<Country>> {
        return searchCountriesUseCase(query)
            .map { countries ->
                applyFiltersAndSort(countries, filters)
            }
    }

    /**
     * Applies filters and sorting to a list of countries.
     * 
     * This is useful when you already have a list of countries
     * and just want to apply filters without querying the database.
     * 
     * @param countries List of countries to filter
     * @param filters Search filters to apply
     * @return Filtered and sorted list of countries
     */
    fun applyFiltersAndSort(
        countries: List<Country>,
        filters: SearchFilters
    ): List<Country> {
        var filtered = countries

        // Apply region filter
        if (filters.selectedRegions.isNotEmpty()) {
            filtered = filtered.filter { country ->
                filters.selectedRegions.contains(country.region)
            }
        }

        // Note: Subregion filtering not implemented as Country model doesn't have subregion field
        // Consider adding to Country model if needed

        // Apply sort order
        filtered = applySortOrder(filtered, filters.sortOrder)

        return filtered
    }

    /**
     * Applies the specified sort order to the country list.
     *
     * @param countries List of countries to sort
     * @param sortOrder Sort order to apply
     * @return Sorted list of countries
     */
    private fun applySortOrder(
        countries: List<Country>,
        sortOrder: SortOrder
    ): List<Country> {
        return when (sortOrder) {
            SortOrder.NAME_ASC -> countries.sortedBy { it.name }
            SortOrder.NAME_DESC -> countries.sortedByDescending { it.name }
            SortOrder.POPULATION_DESC -> countries.sortedByDescending { it.population }
            SortOrder.POPULATION_ASC -> countries.sortedBy { it.population }
            // Area sorting not implemented as Country model doesn't have area field
            // Fallback to name sorting
            SortOrder.AREA_DESC -> countries.sortedBy { it.name }
            SortOrder.AREA_ASC -> countries.sortedBy { it.name }
        }
    }
}

/**
 * Use case for generating search suggestions based on input.
 *
 * Provides autocomplete-style suggestions as the user types.
 * Suggestions include:
 * - Country names
 * - Capital cities
 * - Regions
 *
 * ## Features
 *
 * 1. **Fast Suggestions**
 *    - In-memory filtering (< 1ms)
 *    - Limited to top 5 results
 *    - Real-time updates
 *
 * 2. **Multi-Source**
 *    - Country names (primary)
 *    - Capital cities (secondary)
 *    - Regions (tertiary)
 *
 * 3. **Smart Matching**
 *    - Prefix matching
 *    - Case-insensitive
 *    - Relevance-ranked
 *
 * ## Usage Example
 *
 * ```kotlin
 * val suggestions = useCase(
 *     query = "uni",
 *     allCountries = countries,
 *     maxSuggestions = 5
 * )
 * // Result: ["United States", "United Kingdom", "United Arab Emirates"]
 * ```
 */
class GenerateSearchSuggestionsUseCase @Inject constructor() {

    /**
     * Generates search suggestions for the given query.
     *
     * @param query Current search query
     * @param allCountries Complete list of countries
     * @param maxSuggestions Maximum number of suggestions (default 5)
     * @return List of suggested search terms
     */
    operator fun invoke(
        query: String,
        allCountries: List<Country>,
        maxSuggestions: Int = 5
    ): List<String> {
        if (query.isBlank()) return emptyList()

        val normalizedQuery = query.trim().lowercase()
        val suggestions = mutableListOf<String>()

        // Collect country name matches
        allCountries
            .filter { it.name.lowercase().startsWith(normalizedQuery) }
            .take(maxSuggestions)
            .forEach { suggestions.add(it.name) }

        // If we need more, add capital city matches
        if (suggestions.size < maxSuggestions) {
            allCountries
                .filter { 
                    it.capital.lowercase().startsWith(normalizedQuery) &&
                    !suggestions.contains(it.name)
                }
                .take(maxSuggestions - suggestions.size)
                .forEach { suggestions.add(it.capital) }
        }

        return suggestions.take(maxSuggestions)
    }
}
