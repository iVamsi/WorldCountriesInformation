package com.vamsi.worldcountriesinformation.domainmodel

/**
 * Search filters for country search functionality.
 *
 * Platform-agnostic model for filtering country searches.
 *
 * @property selectedRegions Set of regions to filter by (empty = all regions)
 * @property selectedSubregions Set of subregions to filter by (empty = all subregions)
 * @property sortOrder Sort order for search results
 */
data class SearchFilters(
    val selectedRegions: Set<String> = emptySet(),
    val selectedSubregions: Set<String> = emptySet(),
    val sortOrder: SortOrder = SortOrder.NAME_ASC
)

/**
 * Sort order options for country list.
 */
enum class SortOrder {
    /**
     * Sort by name (A-Z)
     */
    NAME_ASC,

    /**
     * Sort by name (Z-A)
     */
    NAME_DESC,

    /**
     * Sort by population (highest first)
     */
    POPULATION_DESC,

    /**
     * Sort by population (lowest first)
     */
    POPULATION_ASC,

    /**
     * Sort by area (largest first)
     */
    AREA_DESC,

    /**
     * Sort by area (smallest first)
     */
    AREA_ASC
}

/**
 * Common regions for filtering.
 */
object Regions {
    const val AFRICA = "Africa"
    const val AMERICAS = "Americas"
    const val ASIA = "Asia"
    const val EUROPE = "Europe"
    const val OCEANIA = "Oceania"
    const val ANTARCTIC = "Antarctic"
    
    val ALL = setOf(AFRICA, AMERICAS, ASIA, EUROPE, OCEANIA, ANTARCTIC)
}
