package com.vamsi.worldcountriesinformation.core.datastore

/**
 * Represents user preferences for the application.
 *
 * This data class holds all user-configurable settings that are persisted
 * across app sessions using DataStore Preferences.
 *
 * @property cachePolicy The cache policy preference for data fetching.
 *                       Determines whether to prioritize cache or network.
 * @property offlineMode Whether offline mode is enabled. When true, the app
 *                       will only use cached data and not make network requests.
 * @property lastCacheClearTimestamp The timestamp (in milliseconds) when the
 *                                   cache was last manually cleared by the user.
 * @property themeMode The theme preference (SYSTEM, LIGHT, DARK).
 *                     Reserved for future implementation.
 */
data class UserPreferences(
    val cachePolicy: CachePolicy = CachePolicy.CACHE_FIRST,
    val offlineMode: Boolean = false,
    val lastCacheClearTimestamp: Long = 0L,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

/**
 * Cache policy options for data fetching.
 *
 * These policies determine how the repository should handle data requests
 * when both cache and network are available.
 */
enum class CachePolicy {
    /**
     * Always try cache first. Only fetch from network if cache is empty or stale.
     * Best for: Reducing network usage, faster response times.
     */
    CACHE_FIRST,

    /**
     * Always fetch from network first. Fall back to cache on network error.
     * Best for: Always having the latest data, real-time updates.
     */
    NETWORK_FIRST,

    /**
     * Only use cache. Never make network requests.
     * Best for: Offline mode, testing, reducing data usage.
     */
    CACHE_ONLY,

    /**
     * Only use network. Never use cache.
     * Best for: Always fresh data, ignoring potentially stale cache.
     */
    NETWORK_ONLY
}

/**
 * Theme mode options for the application.
 *
 * Reserved for future theme switching implementation.
 */
enum class ThemeMode {
    /**
     * Follow system theme (light/dark).
     */
    SYSTEM,

    /**
     * Always use light theme.
     */
    LIGHT,

    /**
     * Always use dark theme.
     */
    DARK
}
