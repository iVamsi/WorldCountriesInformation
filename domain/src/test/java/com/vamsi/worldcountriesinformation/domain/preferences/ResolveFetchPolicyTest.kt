package com.vamsi.worldcountriesinformation.domain.preferences

import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveFetchPolicyTest {
    @Test
    fun `offline mode forces CACHE_ONLY regardless of stored policy`() {
        val prefs =
            UserPreferences(
                cachePolicy = CachePolicy.NETWORK_FIRST,
                offlineMode = true,
            )

        assertEquals(CachePolicy.CACHE_ONLY, resolveFetchPolicy(prefs))
    }

    @Test
    fun `online mode returns stored cache policy`() {
        val prefs =
            UserPreferences(
                cachePolicy = CachePolicy.FORCE_REFRESH,
                offlineMode = false,
            )

        assertEquals(CachePolicy.FORCE_REFRESH, resolveFetchPolicy(prefs))
    }

    @Test
    fun `default preferences resolve to CACHE_FIRST when online`() {
        assertEquals(CachePolicy.CACHE_FIRST, resolveFetchPolicy(UserPreferences()))
    }
}
