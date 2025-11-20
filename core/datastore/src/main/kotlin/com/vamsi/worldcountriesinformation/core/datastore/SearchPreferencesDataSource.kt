package com.vamsi.worldcountriesinformation.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vamsi.worldcountriesinformation.domainmodel.SearchFilters
import com.vamsi.worldcountriesinformation.domainmodel.SearchHistoryEntry
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore instance for search preferences.
 */
private val Context.searchDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "search_preferences"
)

/**
 * Data source for managing search-related preferences using DataStore.
 *
 * Handles:
 * - Search filters (regions, subregions, sort order)
 * - Search history (recent searches)
 * - Search suggestions (enabled/disabled)
 *
 * All operations are reactive and emit updates via Flow.
 *
 * @property context Application context for DataStore access
 * @property json JSON serializer for complex data types
 */
@Singleton
class SearchPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }

    /**
     * Preference keys for search-related data.
     */
    private object Keys {
        val SELECTED_REGIONS = stringPreferencesKey("selected_regions")
        val SELECTED_SUBREGIONS = stringPreferencesKey("selected_subregions")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val SEARCH_HISTORY = stringPreferencesKey("search_history")
        val ENABLE_SUGGESTIONS = booleanPreferencesKey("enable_suggestions")
    }

    /**
     * Flow of search preferences.
     *
     * Emits current preferences and all updates reactively.
     */
    val searchPreferences: Flow<SearchPreferences> = context.searchDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapSearchPreferences(preferences)
        }

    /**
     * Updates the selected regions filter.
     *
     * @param regions Set of region names to filter by
     */
    suspend fun updateSelectedRegions(regions: Set<String>) {
        context.searchDataStore.edit { preferences ->
            preferences[Keys.SELECTED_REGIONS] = regions.joinToString(",")
        }
    }

    /**
     * Updates the selected subregions filter.
     *
     * @param subregions Set of subregion names to filter by
     */
    suspend fun updateSelectedSubregions(subregions: Set<String>) {
        context.searchDataStore.edit { preferences ->
            preferences[Keys.SELECTED_SUBREGIONS] = subregions.joinToString(",")
        }
    }

    /**
     * Updates the sort order.
     *
     * @param sortOrder The new sort order
     */
    suspend fun updateSortOrder(sortOrder: SortOrder) {
        context.searchDataStore.edit { preferences ->
            preferences[Keys.SORT_ORDER] = sortOrder.name
        }
    }

    /**
     * Updates the complete search filters.
     *
     * @param filters The new search filters
     */
    suspend fun updateFilters(filters: SearchFilters) {
        context.searchDataStore.edit { preferences ->
            preferences[Keys.SELECTED_REGIONS] = filters.selectedRegions.joinToString(",")
            preferences[Keys.SELECTED_SUBREGIONS] = filters.selectedSubregions.joinToString(",")
            preferences[Keys.SORT_ORDER] = filters.sortOrder.name
        }
    }

    /**
     * Adds a search query to history.
     *
     * Maintains a maximum of 10 recent searches.
     *
     * @param query The search query to add
     */
    suspend fun addToSearchHistory(query: String) {
        if (query.isBlank()) return

        context.searchDataStore.edit { preferences ->
            val currentHistory = getSearchHistoryFromPreferences(preferences)
            val updatedHistory = addToHistoryInternal(query.trim(), currentHistory)

            // Convert to DTOs for serialization
            val dtos = updatedHistory.map { it.toDto() }
            val historyJson = json.encodeToString(dtos)
            preferences[Keys.SEARCH_HISTORY] = historyJson
        }
    }

    /**
     * Removes a single search history entry that matches the provided query.
     */
    suspend fun removeFromSearchHistory(query: String) {
        if (query.isBlank()) return

        context.searchDataStore.edit { preferences ->
            val currentHistory = getSearchHistoryFromPreferences(preferences)
            val updatedHistory = currentHistory.filterNot {
                it.query.equals(query.trim(), ignoreCase = true)
            }

            val dtos = updatedHistory.map { it.toDto() }
            preferences[Keys.SEARCH_HISTORY] = json.encodeToString(dtos)
        }
    }

    /**
     * Internal method to add query to history with business logic.
     */
    private fun addToHistoryInternal(
        query: String,
        currentHistory: List<SearchHistoryEntry>,
    ): List<SearchHistoryEntry> {
        // Remove existing entry with same query (case-insensitive)
        val filteredHistory = currentHistory.filterNot {
            it.query.equals(query, ignoreCase = true)
        }

        // Add new entry at the beginning
        val newHistory = listOf(SearchHistoryEntry(query)) + filteredHistory

        // Keep only max size
        return newHistory.take(MAX_HISTORY_SIZE)
    }

    /**
     * Clears all search history.
     */
    suspend fun clearSearchHistory() {
        context.searchDataStore.edit { preferences ->
            preferences[Keys.SEARCH_HISTORY] = json.encodeToString(emptyList<SearchHistoryEntryDto>())
        }
    }

    /**
     * Toggles search suggestions on/off.
     *
     * @param enabled Whether suggestions should be enabled
     */
    suspend fun updateSuggestionsEnabled(enabled: Boolean) {
        context.searchDataStore.edit { preferences ->
            preferences[Keys.ENABLE_SUGGESTIONS] = enabled
        }
    }

    /**
     * Clears all search filters.
     */
    suspend fun clearFilters() {
        context.searchDataStore.edit { preferences ->
            preferences[Keys.SELECTED_REGIONS] = ""
            preferences[Keys.SELECTED_SUBREGIONS] = ""
            preferences[Keys.SORT_ORDER] = SortOrder.NAME_ASC.name
        }
    }

    /**
     * Maps DataStore preferences to SearchPreferences object.
     */
    private fun mapSearchPreferences(preferences: Preferences): SearchPreferences {
        // Parse regions
        val regionsString = preferences[Keys.SELECTED_REGIONS] ?: ""
        val selectedRegions = if (regionsString.isNotBlank()) {
            regionsString.split(",").toSet()
        } else {
            emptySet()
        }

        // Parse subregions
        val subregionsString = preferences[Keys.SELECTED_SUBREGIONS] ?: ""
        val selectedSubregions = if (subregionsString.isNotBlank()) {
            subregionsString.split(",").toSet()
        } else {
            emptySet()
        }

        // Parse sort order
        val sortOrderString = preferences[Keys.SORT_ORDER]
        val sortOrder = try {
            sortOrderString?.let { SortOrder.valueOf(it) } ?: SortOrder.NAME_ASC
        } catch (e: IllegalArgumentException) {
            SortOrder.NAME_ASC
        }

        // Parse search history
        val searchHistory = getSearchHistoryFromPreferences(preferences)

        // Parse suggestions enabled
        val enableSuggestions = preferences[Keys.ENABLE_SUGGESTIONS] ?: true

        return SearchPreferences(
            filters = SearchFilters(
                selectedRegions = selectedRegions,
                selectedSubregions = selectedSubregions,
                sortOrder = sortOrder
            ),
            searchHistory = searchHistory,
            enableSuggestions = enableSuggestions
        )
    }

    /**
     * Extracts search history from preferences.
     */
    private fun getSearchHistoryFromPreferences(preferences: Preferences): List<SearchHistoryEntry> {
        val historyJson = preferences[Keys.SEARCH_HISTORY] ?: ""
        return if (historyJson.isNotBlank()) {
            try {
                // Decode DTOs and convert to domain models
                val dtos = json.decodeFromString<List<SearchHistoryEntryDto>>(historyJson)
                dtos.map { it.toDomain() }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}
