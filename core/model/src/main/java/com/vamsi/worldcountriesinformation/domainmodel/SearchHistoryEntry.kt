package com.vamsi.worldcountriesinformation.domainmodel

/**
 * Search history entry.
 *
 * Stores a single search query with timestamp for history tracking.
 *
 * @property query The search query text
 * @property timestamp When the search was performed (epoch milliseconds)
 *
 */
data class SearchHistoryEntry(
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
