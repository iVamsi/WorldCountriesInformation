package com.vamsi.worldcountriesinformation.data.countries.repository

import com.vamsi.worldcountriesinformation.domain.countries.SyncState
import com.vamsi.worldcountriesinformation.domain.countries.SyncStatePort

/** In-memory sync state for repository tests. */
class FakeSyncStatePort(var state: SyncState = SyncState()) : SyncStatePort {
    override suspend fun read(): SyncState = state

    override suspend fun recordCheck(atMs: Long) {
        state = state.copy(lastCheckedAtMs = atMs)
    }

    override suspend fun recordChange(fingerprint: String, atMs: Long) {
        state = state.copy(fingerprint = fingerprint, lastChangedAtMs = atMs)
    }

    override suspend fun clear() {
        state = SyncState()
    }
}
