package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domainmodel.Country
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for retrieving nearby countries in the same geographic region,
 * excluding the current country.
 *
 * This use case is designed for the Country Details screen to show related countries
 * that share the same region. It delegates to [CountriesRepository.getCountriesByRegion]
 * and filters out the current country from the results.
 *
 * ## Features
 *
 * 1. **Region-based filtering** – Returns countries in the same geographic region
 * 2. **Self-exclusion** – Filters out the country being viewed
 * 3. **Reactive updates** – Returns Flow for continuous database observation
 * 4. **Input validation** – Validates region is not blank
 *
 * ## Usage
 *
 * ```kotlin
 * class CountryDetailsViewModel @Inject constructor(
 *     private val getNearbyCountriesUseCase: GetNearbyCountriesUseCase
 * ) : ViewModel() {
 *
 *     fun loadNearby(region: String, currentCode: String) {
 *         viewModelScope.launch {
 *             getNearbyCountriesUseCase(region, currentCode)
 *                 .collect { nearbyCountries ->
 *                     // Update UI with nearby countries
 *                 }
 *         }
 *     }
 * }
 * ```
 *
 * ## Architecture
 *
 * ```
 * UI Layer (Composable)
 *        ↓
 * ViewModel
 *        ↓
 * GetNearbyCountriesUseCase ← (You are here)
 *        ↓
 * CountriesRepository
 *        ↓
 * Database (Room)
 * ```
 *
 * @param countriesRepository Repository providing country data access
 *
 * @see CountriesRepository.getCountriesByRegion
 * @see GetCountriesByRegionUseCase
 *
 * @since 2.5.0
 */
class GetNearbyCountriesUseCase @Inject constructor(
    private val countriesRepository: CountriesRepository,
) {

    /**
     * Gets countries in the same region, excluding the specified country.
     *
     * @param region Region name (case-sensitive, e.g. "Europe", "Asia")
     * @param excludeCountryCode Three-letter ISO code of the country to exclude
     * @return Flow emitting filtered list of nearby countries, sorted alphabetically
     * @throws IllegalArgumentException if region is blank
     */
    operator fun invoke(region: String, excludeCountryCode: String): Flow<List<Country>> {
        require(region.isNotBlank()) {
            "Region cannot be empty. Provide a valid region to find nearby countries."
        }

        val normalizedRegion = region.trim()
        val normalizedCode = excludeCountryCode.uppercase().trim()

        return countriesRepository.getCountriesByRegion(normalizedRegion)
            .map { countries ->
                countries.filter { it.threeLetterCode != normalizedCode }
            }
    }
}
