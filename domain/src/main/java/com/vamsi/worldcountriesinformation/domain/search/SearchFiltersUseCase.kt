package com.vamsi.worldcountriesinformation.domain.search

import com.vamsi.worldcountriesinformation.domainmodel.SearchFilters
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder
import javax.inject.Inject

/**
 * Use case for search filter operations.
 *
 * Provides business logic for managing search filters,
 * checking filter states, and counting active filters.
 *
 * ## Features
 *
 * 1. **Filter State Checking**
 *    - Detect if any filters are active
 *    - Count active filter categories
 *
 * 2. **Filter Validation**
 *    - Validate filter combinations
 *    - Check filter applicability
 *
 * @since Phase 3.10
 */
class SearchFiltersUseCase @Inject constructor() {

    /**
     * Checks if any filters are active.
     *
     * A filter is considered active if:
     * - At least one region is selected
     * - At least one subregion is selected
     * - Sort order is not the default (NAME_ASC)
     *
     * @param filters The filters to check
     * @return True if any filters are applied
     */
    fun hasActiveFilters(filters: SearchFilters): Boolean {
        return filters.selectedRegions.isNotEmpty() ||
               filters.selectedSubregions.isNotEmpty() ||
               filters.sortOrder != SortOrder.NAME_ASC
    }

    /**
     * Gets the count of active filter categories.
     *
     * Counts how many filter categories are active:
     * - Regions (0 or 1)
     * - Subregions (0 or 1)
     * - Sort order (0 or 1)
     *
     * @param filters The filters to count
     * @return Number of active filter categories (0-3)
     */
    fun getActiveFilterCount(filters: SearchFilters): Int {
        var count = 0
        if (filters.selectedRegions.isNotEmpty()) count++
        if (filters.selectedSubregions.isNotEmpty()) count++
        if (filters.sortOrder != SortOrder.NAME_ASC) count++
        return count
    }

    /**
     * Creates a copy of filters with the region toggled.
     *
     * If the region is already selected, it's removed.
     * If not selected, it's added.
     *
     * @param filters Current filters
     * @param region Region to toggle
     * @return New filters with region toggled
     */
    fun toggleRegion(filters: SearchFilters, region: String): SearchFilters {
        val newRegions = if (filters.selectedRegions.contains(region)) {
            filters.selectedRegions - region
        } else {
            filters.selectedRegions + region
        }
        return filters.copy(selectedRegions = newRegions)
    }

    /**
     * Creates filters with all settings cleared to defaults.
     *
     * @return Default SearchFilters
     */
    fun clearFilters(): SearchFilters {
        return SearchFilters()
    }
}
