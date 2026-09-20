package com.vamsi.worldcountriesinformation.data.countries.sync

import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CountriesSyncWorkerTest {

    @Test
    fun `syncs with default preferences`() {
        assertTrue(CountriesSyncWorker.shouldSync(UserPreferences()))
    }

    @Test
    fun `stays offline when offline mode is on`() {
        assertFalse(CountriesSyncWorker.shouldSync(UserPreferences(offlineMode = true)))
    }

    @Test
    fun `stays offline under the cache-only policy`() {
        assertFalse(CountriesSyncWorker.shouldSync(UserPreferences(cachePolicy = CachePolicy.CACHE_ONLY)))
    }
}
