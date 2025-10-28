package com.vamsi.worldcountriesinformation.core.datastore

import kotlinx.serialization.Serializable

/**
 * Serializable wrapper for SearchHistoryEntry.
 *
 * This is needed because the domain model doesn't have kotlinx.serialization dependency.
 * This wrapper is used only for DataStore persistence.
 */
@Serializable
internal data class SearchHistoryEntryDto(
    val query: String,
    val timestamp: Long
)

/**
 * Converts domain SearchHistoryEntry to DTO.
 */
internal fun com.vamsi.worldcountriesinformation.domainmodel.SearchHistoryEntry.toDto() =
    SearchHistoryEntryDto(query = query, timestamp = timestamp)

/**
 * Converts DTO to domain SearchHistoryEntry.
 */
internal fun SearchHistoryEntryDto.toDomain() =
    com.vamsi.worldcountriesinformation.domainmodel.SearchHistoryEntry(query = query, timestamp = timestamp)
