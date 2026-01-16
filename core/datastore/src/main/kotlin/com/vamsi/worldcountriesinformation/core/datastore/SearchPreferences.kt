package com.vamsi.worldcountriesinformation.core.datastore

import com.vamsi.worldcountriesinformation.domainmodel.RecentlyViewedEntry
import com.vamsi.worldcountriesinformation.domainmodel.SearchFilters
import com.vamsi.worldcountriesinformation.domainmodel.SearchHistoryEntry

/**
 * Search preferences including filters, history, and suggestions.
 *
 * Pure data class for storing search-related preferences.
 * Business logic should be in use cases, not in this model.
 *
 * @property filters Active search filters
 * @property searchHistory List of recent searches (max 10)
 * @property enableSuggestions Whether to show search suggestions
 * @property recentlyViewed List of recently viewed countries (max 20)
 *
 * @see [ManageSearchHistoryUseCase] for history operations
 * @see [SearchFiltersUseCase] for filter operations
 */
data class SearchPreferences(
    val filters: SearchFilters = SearchFilters(),
    val searchHistory: List<SearchHistoryEntry> = emptyList(),
    val enableSuggestions: Boolean = true,
    val recentlyViewed: List<RecentlyViewedEntry> = emptyList()
)
