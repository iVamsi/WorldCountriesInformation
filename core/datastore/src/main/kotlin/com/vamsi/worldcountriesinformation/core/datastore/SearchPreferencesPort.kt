package com.vamsi.worldcountriesinformation.core.datastore

import com.vamsi.worldcountriesinformation.domainmodel.SortOrder
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for persisted search UX state (filters, history, recents).
 *
 * Backed by [SearchPreferencesDataSource] in production; fakes can be used in tests.
 */
interface SearchPreferencesPort {
    val searchPreferences: Flow<SearchPreferences>

    suspend fun updateSelectedRegions(regions: Set<String>)

    suspend fun updateSortOrder(sortOrder: SortOrder)

    suspend fun clearFilters()

    suspend fun addToSearchHistory(query: String)

    suspend fun removeFromSearchHistory(query: String)

    suspend fun clearSearchHistory()

    suspend fun addToRecentlyViewedCountry(countryCode: String)
}
