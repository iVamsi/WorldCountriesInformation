package com.vamsi.worldcountriesinformation.domain.core

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [CachePolicy] enum and its utility methods.
 *
 * Tests cover:
 * - Policy behavior methods (shouldFetchFromNetwork, allowsCacheFallback, requiresStalenessCheck)
 * - Cache freshness detection
 * - Cache age calculation
 * - Human-readable age descriptions
 * - Edge cases (zero timestamp, negative values, very old data)
 */
class CachePolicyTest {

    // ===========================================
    // Policy Behavior Tests
    // ===========================================

    @Test
    fun `CACHE_FIRST should not always fetch from network`() {
        // CACHE_FIRST only fetches if cache is stale
        assertFalse(CachePolicy.CACHE_FIRST.shouldFetchFromNetwork())
    }

    @Test
    fun `NETWORK_FIRST should always fetch from network`() {
        assertTrue(CachePolicy.NETWORK_FIRST.shouldFetchFromNetwork())
    }

    @Test
    fun `FORCE_REFRESH should always fetch from network`() {
        assertTrue(CachePolicy.FORCE_REFRESH.shouldFetchFromNetwork())
    }

    @Test
    fun `CACHE_ONLY should never fetch from network`() {
        assertFalse(CachePolicy.CACHE_ONLY.shouldFetchFromNetwork())
    }

    @Test
    fun `CACHE_FIRST should allow cache fallback`() {
        assertTrue(CachePolicy.CACHE_FIRST.allowsCacheFallback())
    }

    @Test
    fun `NETWORK_FIRST should allow cache fallback`() {
        assertTrue(CachePolicy.NETWORK_FIRST.allowsCacheFallback())
    }

    @Test
    fun `FORCE_REFRESH should not allow cache fallback`() {
        // FORCE_REFRESH fails completely on network error
        assertFalse(CachePolicy.FORCE_REFRESH.allowsCacheFallback())
    }

    @Test
    fun `CACHE_ONLY should allow cache fallback`() {
        assertTrue(CachePolicy.CACHE_ONLY.allowsCacheFallback())
    }

    @Test
    fun `CACHE_FIRST should require staleness check`() {
        assertTrue(CachePolicy.CACHE_FIRST.requiresStalenessCheck())
    }

    @Test
    fun `NETWORK_FIRST should not require staleness check`() {
        // Always fetches network anyway
        assertFalse(CachePolicy.NETWORK_FIRST.requiresStalenessCheck())
    }

    @Test
    fun `FORCE_REFRESH should not require staleness check`() {
        // Always fetches network
        assertFalse(CachePolicy.FORCE_REFRESH.requiresStalenessCheck())
    }

    @Test
    fun `CACHE_ONLY should not require staleness check`() {
        // Returns any cached data regardless of age
        assertFalse(CachePolicy.CACHE_ONLY.requiresStalenessCheck())
    }

    // ===========================================
    // Cache Freshness Tests
    // ===========================================

    @Test
    fun `isCacheFresh returns true for recent data`() {
        // Data updated 1 hour ago
        val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
        assertTrue(CachePolicy.isCacheFresh(oneHourAgo))
    }

    @Test
    fun `isCacheFresh returns true for data just under 24 hours old`() {
        // Data updated 23 hours 59 minutes ago
        val almostOneDayAgo = System.currentTimeMillis() - (23 * 60 * 60 * 1000 + 59 * 60 * 1000)
        assertTrue(CachePolicy.isCacheFresh(almostOneDayAgo))
    }

    @Test
    fun `isCacheFresh returns false for data over 24 hours old`() {
        // Data updated 25 hours ago
        val oneDayAgo = System.currentTimeMillis() - (25 * 60 * 60 * 1000)
        assertFalse(CachePolicy.isCacheFresh(oneDayAgo))
    }

    @Test
    fun `isCacheFresh returns false for very old data`() {
        // Data updated 30 days ago
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        assertFalse(CachePolicy.isCacheFresh(thirtyDaysAgo))
    }

    @Test
    fun `isCacheFresh returns true for just created data`() {
        // Data updated right now
        val now = System.currentTimeMillis()
        assertTrue(CachePolicy.isCacheFresh(now))
    }

    @Test
    fun `isCacheFresh returns true for data updated in the future`() {
        // Edge case: Clock skew or test scenario
        val futureTimestamp = System.currentTimeMillis() + 1000
        assertTrue(CachePolicy.isCacheFresh(futureTimestamp))
    }

    @Test
    fun `isCacheFresh respects custom validity period`() {
        // Custom validity: 1 hour
        val oneHourMs = 60 * 60 * 1000L
        
        // Data updated 30 minutes ago (fresh)
        val thirtyMinutesAgo = System.currentTimeMillis() - (30 * 60 * 1000)
        assertTrue(CachePolicy.isCacheFresh(thirtyMinutesAgo, oneHourMs))
        
        // Data updated 2 hours ago (stale)
        val twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
        assertFalse(CachePolicy.isCacheFresh(twoHoursAgo, oneHourMs))
    }

    @Test
    fun `isCacheFresh with zero validity period makes all data stale`() {
        val now = System.currentTimeMillis()
        assertFalse(CachePolicy.isCacheFresh(now, 0L))
    }

    // ===========================================
    // Cache Age Calculation Tests
    // ===========================================

    @Test
    fun `getCacheAge returns positive age for past timestamp`() {
        val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
        val age = CachePolicy.getCacheAge(oneHourAgo)
        
        // Age should be approximately 1 hour (allowing for test execution time)
        assertTrue("Age should be around 1 hour (3600000ms), was $age", 
            age in 3600000L..3605000L)
    }

    @Test
    fun `getCacheAge returns zero for current timestamp`() {
        val now = System.currentTimeMillis()
        val age = CachePolicy.getCacheAge(now)
        
        // Age should be very close to zero (< 100ms for test execution)
        assertTrue("Age should be near zero, was $age", age < 100L)
    }

    @Test
    fun `getCacheAge returns negative age for future timestamp`() {
        // Edge case: Clock skew
        val futureTimestamp = System.currentTimeMillis() + 10000
        val age = CachePolicy.getCacheAge(futureTimestamp)
        
        assertTrue("Age should be negative for future timestamp", age < 0)
    }

    @Test
    fun `getCacheAge for very old data`() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val age = CachePolicy.getCacheAge(thirtyDaysAgo)
        
        // 30 days in milliseconds
        val expectedAge = 30L * 24 * 60 * 60 * 1000
        assertTrue("Age should be around 30 days", 
            age in expectedAge..(expectedAge + 10000))
    }

    // ===========================================
    // Human-Readable Age Description Tests
    // ===========================================

    @Test
    fun `getCacheAgeDescription returns 'Just now' for recent timestamp`() {
        val fiveSecondsAgo = System.currentTimeMillis() - 5000
        assertEquals("Just now", CachePolicy.getCacheAgeDescription(fiveSecondsAgo))
    }

    @Test
    fun `getCacheAgeDescription returns minutes for data under 1 hour old`() {
        val thirtyMinutesAgo = System.currentTimeMillis() - (30 * 60 * 1000)
        assertEquals("30 minutes ago", CachePolicy.getCacheAgeDescription(thirtyMinutesAgo))
    }

    @Test
    fun `getCacheAgeDescription returns singular 'minute' for 1 minute`() {
        val oneMinuteAgo = System.currentTimeMillis() - (60 * 1000)
        assertEquals("1 minute ago", CachePolicy.getCacheAgeDescription(oneMinuteAgo))
    }

    @Test
    fun `getCacheAgeDescription returns hours for data over 1 hour old`() {
        val twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
        assertEquals("2 hours ago", CachePolicy.getCacheAgeDescription(twoHoursAgo))
    }

    @Test
    fun `getCacheAgeDescription returns singular 'hour' for 1 hour`() {
        val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
        assertEquals("1 hour ago", CachePolicy.getCacheAgeDescription(oneHourAgo))
    }

    @Test
    fun `getCacheAgeDescription returns days for data over 24 hours old`() {
        val threeDaysAgo = System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000)
        assertEquals("3 days ago", CachePolicy.getCacheAgeDescription(threeDaysAgo))
    }

    @Test
    fun `getCacheAgeDescription returns singular 'day' for 1 day`() {
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        assertEquals("1 day ago", CachePolicy.getCacheAgeDescription(oneDayAgo))
    }

    @Test
    fun `getCacheAgeDescription returns 'Just now' for future timestamp`() {
        // Edge case: Clock skew
        val futureTimestamp = System.currentTimeMillis() + 5000
        assertEquals("Just now", CachePolicy.getCacheAgeDescription(futureTimestamp))
    }

    @Test
    fun `getCacheAgeDescription for exactly 23 hours 59 minutes`() {
        // Just under 24 hours (should show hours, not days)
        val almostOneDay = System.currentTimeMillis() - (23 * 60 * 60 * 1000 + 59 * 60 * 1000)
        val description = CachePolicy.getCacheAgeDescription(almostOneDay)
        assertTrue("Should show hours, got: $description", 
            description.contains("hour"))
    }

    @Test
    fun `getCacheAgeDescription for exactly 59 seconds`() {
        // Just under 1 minute (should show "Just now")
        val fiftyNineSeconds = System.currentTimeMillis() - 59000
        assertEquals("Just now", CachePolicy.getCacheAgeDescription(fiftyNineSeconds))
    }

    @Test
    fun `getCacheAgeDescription for very old data`() {
        // 365 days ago
        val oneYearAgo = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
        assertEquals("365 days ago", CachePolicy.getCacheAgeDescription(oneYearAgo))
    }

    // ===========================================
    // Edge Cases and Error Conditions
    // ===========================================

    @Test
    fun `isCacheFresh handles zero timestamp`() {
        // Epoch time (very old data)
        assertFalse(CachePolicy.isCacheFresh(0L))
    }

    @Test
    fun `getCacheAge handles zero timestamp`() {
        val age = CachePolicy.getCacheAge(0L)
        // Age should be approximately current time
        val now = System.currentTimeMillis()
        assertTrue("Age should be close to current time", 
            age in (now - 1000)..(now + 1000))
    }

    @Test
    fun `getCacheAgeDescription handles zero timestamp`() {
        // Very old data from epoch
        val description = CachePolicy.getCacheAgeDescription(0L)
        assertTrue("Should show days for epoch time", 
            description.contains("days ago"))
    }

    @Test
    fun `default cache validity is 24 hours`() {
        assertEquals(24 * 60 * 60 * 1000L, CachePolicy.DEFAULT_CACHE_VALIDITY_MS)
    }

    // ===========================================
    // Integration Tests (Multiple Policies)
    // ===========================================

    @Test
    fun `all policies have consistent behavior patterns`() {
        val policies = CachePolicy.values()
        assertEquals("Should have 4 cache policies", 4, policies.size)
        
        // At least one policy should fetch from network
        assertTrue("At least one policy should fetch from network",
            policies.any { it.shouldFetchFromNetwork() })
        
        // At least one policy should not fetch from network
        assertTrue("At least one policy should not fetch from network",
            policies.any { !it.shouldFetchFromNetwork() })
        
        // At least one policy should require staleness check
        assertTrue("At least one policy should require staleness check",
            policies.any { it.requiresStalenessCheck() })
    }

    @Test
    fun `FORCE_REFRESH is the strictest policy`() {
        // FORCE_REFRESH should:
        // - Always fetch from network
        assertTrue(CachePolicy.FORCE_REFRESH.shouldFetchFromNetwork())
        
        // - Not allow cache fallback
        assertFalse(CachePolicy.FORCE_REFRESH.allowsCacheFallback())
        
        // - Not require staleness check (always fresh from network)
        assertFalse(CachePolicy.FORCE_REFRESH.requiresStalenessCheck())
    }

    @Test
    fun `CACHE_ONLY is the most lenient policy`() {
        // CACHE_ONLY should:
        // - Never fetch from network
        assertFalse(CachePolicy.CACHE_ONLY.shouldFetchFromNetwork())
        
        // - Allow using cache (any age)
        assertTrue(CachePolicy.CACHE_ONLY.allowsCacheFallback())
        
        // - Not require staleness check (uses any cached data)
        assertFalse(CachePolicy.CACHE_ONLY.requiresStalenessCheck())
    }

    @Test
    fun `CACHE_FIRST balances performance and freshness`() {
        // CACHE_FIRST should:
        // - Only fetch when needed (not always)
        assertFalse(CachePolicy.CACHE_FIRST.shouldFetchFromNetwork())
        
        // - Allow cache fallback
        assertTrue(CachePolicy.CACHE_FIRST.allowsCacheFallback())
        
        // - Require staleness check to determine if fetch is needed
        assertTrue(CachePolicy.CACHE_FIRST.requiresStalenessCheck())
    }

    @Test
    fun `NETWORK_FIRST prioritizes freshness over performance`() {
        // NETWORK_FIRST should:
        // - Always try network first
        assertTrue(CachePolicy.NETWORK_FIRST.shouldFetchFromNetwork())
        
        // - Allow cache fallback on network failure
        assertTrue(CachePolicy.NETWORK_FIRST.allowsCacheFallback())
        
        // - Not require staleness check (always tries network anyway)
        assertFalse(CachePolicy.NETWORK_FIRST.requiresStalenessCheck())
    }

    // ===========================================
    // Boundary Value Tests
    // ===========================================

    @Test
    fun `isCacheFresh at exact 24 hour boundary`() {
        // Exactly 24 hours (86400000 milliseconds)
        val exactlyOneDayAgo = System.currentTimeMillis() - CachePolicy.DEFAULT_CACHE_VALIDITY_MS
        
        // Should be stale (boundary is exclusive: age < validityPeriod)
        assertFalse("Data exactly 24 hours old should be stale", 
            CachePolicy.isCacheFresh(exactlyOneDayAgo))
    }

    @Test
    fun `isCacheFresh one millisecond before 24 hour boundary`() {
        // One millisecond before 24 hours
        val justUnderOneDay = System.currentTimeMillis() - CachePolicy.DEFAULT_CACHE_VALIDITY_MS + 1
        
        // Should be fresh
        assertTrue("Data 1ms before 24 hours should be fresh", 
            CachePolicy.isCacheFresh(justUnderOneDay))
    }

    @Test
    fun `isCacheFresh one millisecond after 24 hour boundary`() {
        // One millisecond after 24 hours
        val justOverOneDay = System.currentTimeMillis() - CachePolicy.DEFAULT_CACHE_VALIDITY_MS - 1
        
        // Should be stale
        assertFalse("Data 1ms after 24 hours should be stale", 
            CachePolicy.isCacheFresh(justOverOneDay))
    }
}
