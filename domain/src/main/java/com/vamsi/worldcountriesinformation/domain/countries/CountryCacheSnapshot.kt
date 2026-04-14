package com.vamsi.worldcountriesinformation.domain.countries

/**
 * Point-in-time snapshot of the local country cache for settings and diagnostics.
 *
 * @property entryCount Number of country rows stored locally.
 * @property oldestEntryLastUpdatedMs Minimum [lastUpdated] among rows, or 0 if empty.
 */
data class CountryCacheSnapshot(
    val entryCount: Int,
    val oldestEntryLastUpdatedMs: Long,
)
