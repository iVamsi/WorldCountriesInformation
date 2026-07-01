package com.vamsi.worldcountriesinformation.domain.preferences

import com.vamsi.worldcountriesinformation.domain.core.CachePolicy

/**
 * Resolves the effective [CachePolicy] for data fetches from stored user preferences.
 * Offline mode always forces [CachePolicy.CACHE_ONLY].
 */
fun resolveFetchPolicy(preferences: UserPreferences): CachePolicy {
    if (preferences.offlineMode) {
        return CachePolicy.CACHE_ONLY
    }
    return preferences.cachePolicy
}
