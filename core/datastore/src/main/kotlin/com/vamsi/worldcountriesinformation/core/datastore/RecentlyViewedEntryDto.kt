package com.vamsi.worldcountriesinformation.core.datastore

import kotlinx.serialization.Serializable

/**
 * Serializable wrapper for RecentlyViewedEntry.
 */
@Serializable
internal data class RecentlyViewedEntryDto(
    val countryCode: String,
    val timestamp: Long
)

/**
 * Converts domain RecentlyViewedEntry to DTO.
 */
internal fun com.vamsi.worldcountriesinformation.domainmodel.RecentlyViewedEntry.toDto() =
    RecentlyViewedEntryDto(countryCode = countryCode, timestamp = timestamp)

/**
 * Converts DTO to domain RecentlyViewedEntry.
 */
internal fun RecentlyViewedEntryDto.toDomain() =
    com.vamsi.worldcountriesinformation.domainmodel.RecentlyViewedEntry(
        countryCode = countryCode,
        timestamp = timestamp
    )
