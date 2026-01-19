package com.vamsi.worldcountriesinformation.domainmodel

/**
 * Recently viewed country entry.
 *
 * Stores a country code with timestamp for recency tracking.
 *
 * @property countryCode Three-letter country code (ISO-3166 alpha-3)
 * @property timestamp When the country was viewed (epoch milliseconds)
 */
data class RecentlyViewedEntry(
    val countryCode: String,
    val timestamp: Long = System.currentTimeMillis()
)
