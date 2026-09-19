package com.vamsi.worldcountriesinformation.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vamsi.worldcountriesinformation.domain.countries.SyncState
import com.vamsi.worldcountriesinformation.domain.countries.SyncStatePort
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncStateStore: DataStore<Preferences> by preferencesDataStore(name = "sync_state")

@Singleton
class SyncStateDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SyncStatePort {

    private object Keys {
        val FINGERPRINT = stringPreferencesKey("fingerprint")
        val LAST_CHECKED_AT = longPreferencesKey("last_checked_at")
        val LAST_CHANGED_AT = longPreferencesKey("last_changed_at")
    }

    override suspend fun read(): SyncState {
        val prefs = context.syncStateStore.data.first()
        return SyncState(
            fingerprint = prefs[Keys.FINGERPRINT].orEmpty(),
            lastCheckedAtMs = prefs[Keys.LAST_CHECKED_AT] ?: 0L,
            lastChangedAtMs = prefs[Keys.LAST_CHANGED_AT] ?: 0L,
        )
    }

    override suspend fun recordCheck(atMs: Long) {
        context.syncStateStore.edit { it[Keys.LAST_CHECKED_AT] = atMs }
    }

    override suspend fun recordChange(fingerprint: String, atMs: Long) {
        context.syncStateStore.edit {
            it[Keys.FINGERPRINT] = fingerprint
            it[Keys.LAST_CHANGED_AT] = atMs
        }
    }

    override suspend fun clear() {
        context.syncStateStore.edit { it.clear() }
    }
}
