package com.vamsi.worldcountriesinformation.domainmodel

/**
 * Light, list-friendly projection of a country. Excludes detail-only fields
 * (languages, currencies, calling code) so list, search, suggestions, recents,
 * favorites, and widget paths don't pull heavy data into memory.
 *
 * Detail screens should resolve the full [Country] via the repository.
 */
data class CountrySummary(
    val name: String,
    val capital: String,
    val region: String,
    val population: Int,
    val twoLetterCode: String,
    val threeLetterCode: String,
    val latitude: Double,
    val longitude: Double,
)
