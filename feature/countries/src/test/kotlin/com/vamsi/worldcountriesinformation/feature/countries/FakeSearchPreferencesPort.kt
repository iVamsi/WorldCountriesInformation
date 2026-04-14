package com.vamsi.worldcountriesinformation.feature.countries

import com.vamsi.worldcountriesinformation.core.datastore.SearchPreferences
import com.vamsi.worldcountriesinformation.core.datastore.SearchPreferencesPort
import com.vamsi.worldcountriesinformation.domainmodel.RecentlyViewedEntry
import com.vamsi.worldcountriesinformation.domainmodel.SearchHistoryEntry
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory [SearchPreferencesPort] for unit tests (no Android DataStore).
 */
class FakeSearchPreferencesPort(
    initial: SearchPreferences = SearchPreferences(),
) : SearchPreferencesPort {

    private val _prefs = MutableStateFlow(initial)
    val prefsState: StateFlow<SearchPreferences> = _prefs.asStateFlow()

    override val searchPreferences = _prefs.asStateFlow()

    override suspend fun updateSelectedRegions(regions: Set<String>) {
        _prefs.update { current ->
            current.copy(filters = current.filters.copy(selectedRegions = regions))
        }
    }

    override suspend fun updateSortOrder(sortOrder: SortOrder) {
        _prefs.update { current ->
            current.copy(filters = current.filters.copy(sortOrder = sortOrder))
        }
    }

    override suspend fun clearFilters() {
        _prefs.update { current ->
            current.copy(
                filters = current.filters.copy(
                    selectedRegions = emptySet(),
                    selectedSubregions = emptySet(),
                    sortOrder = SortOrder.NAME_ASC,
                )
            )
        }
    }

    override suspend fun addToSearchHistory(query: String) {
        if (query.isBlank()) return
        _prefs.update { current ->
            val entry = SearchHistoryEntry(query.trim())
            current.copy(searchHistory = listOf(entry) + current.searchHistory.filterNot {
                it.query.equals(query.trim(), ignoreCase = true)
            })
        }
    }

    override suspend fun removeFromSearchHistory(query: String) {
        if (query.isBlank()) return
        _prefs.update { current ->
            current.copy(
                searchHistory = current.searchHistory.filterNot {
                    it.query.equals(query.trim(), ignoreCase = true)
                }
            )
        }
    }

    override suspend fun clearSearchHistory() {
        _prefs.update { it.copy(searchHistory = emptyList()) }
    }

    override suspend fun addToRecentlyViewedCountry(countryCode: String) {
        val code = countryCode.trim().uppercase()
        if (code.isBlank()) return
        _prefs.update { current ->
            val entry = RecentlyViewedEntry(code)
            current.copy(
                recentlyViewed = listOf(entry) + current.recentlyViewed.filterNot {
                    it.countryCode.equals(code, ignoreCase = true)
                }
            )
        }
    }
}
