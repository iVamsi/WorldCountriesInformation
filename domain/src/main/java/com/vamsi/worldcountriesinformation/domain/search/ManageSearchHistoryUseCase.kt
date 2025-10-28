package com.vamsi.worldcountriesinformation.domain.search

import com.vamsi.worldcountriesinformation.domainmodel.SearchHistoryEntry
import javax.inject.Inject

/**
 * Use case for managing search history operations.
 *
 * Handles adding, removing, and clearing search history entries
 * with proper business logic validation.
 *
 * ## Features
 *
 * 1. **History Management**
 *    - Add queries to history
 *    - Maintain max size limit
 *    - Deduplicate entries
 *    - Clear all history
 *
 * 2. **Validation**
 *    - Rejects blank queries
 *    - Trims whitespace
 *    - Case-insensitive deduplication
 *
 * @since Phase 3.10
 */
class ManageSearchHistoryUseCase @Inject constructor() {

    companion object {
        /**
         * Maximum number of search history entries to keep.
         */
        const val MAX_HISTORY_SIZE = 10
    }

    /**
     * Adds a new search query to history, maintaining max size.
     *
     * - Rejects blank queries
     * - Removes duplicate (case-insensitive)
     * - Adds new entry at the beginning
     * - Keeps only max size
     *
     * @param query The search query to add
     * @param currentHistory Current list of history entries
     * @return New list with query added
     */
    fun addToHistory(
        query: String,
        currentHistory: List<SearchHistoryEntry>
    ): List<SearchHistoryEntry> {
        if (query.isBlank()) return currentHistory

        // Remove existing entry with same query (case-insensitive)
        val filteredHistory = currentHistory.filterNot { 
            it.query.equals(query.trim(), ignoreCase = true) 
        }

        // Add new entry at the beginning
        val newHistory = listOf(SearchHistoryEntry(query.trim())) + filteredHistory

        // Keep only max size
        return newHistory.take(MAX_HISTORY_SIZE)
    }

    /**
     * Clears all search history.
     *
     * @return Empty list
     */
    fun clearHistory(): List<SearchHistoryEntry> {
        return emptyList()
    }
}
