package com.vamsi.worldcountriesinformation.domain.countries

/** What the last sync saw: used to skip Room writes when nothing changed. */
data class SyncState(
    val fingerprint: String = "",
    val lastCheckedAtMs: Long = 0L,
    val lastChangedAtMs: Long = 0L,
)

interface SyncStatePort {
    suspend fun read(): SyncState

    /** Both sources answered; the data may or may not have changed. */
    suspend fun recordCheck(atMs: Long)

    /** Room was rewritten with data that hashes to [fingerprint]. */
    suspend fun recordChange(fingerprint: String, atMs: Long)

    suspend fun clear()
}

enum class SyncOutcome { UPDATED, UNCHANGED }
