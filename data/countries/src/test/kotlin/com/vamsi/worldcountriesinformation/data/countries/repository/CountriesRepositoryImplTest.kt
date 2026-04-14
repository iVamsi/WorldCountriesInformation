package com.vamsi.worldcountriesinformation.data.countries.repository

import app.cash.turbine.test
import com.vamsi.worldcountriesinformation.core.database.dao.CountryDao
import com.vamsi.worldcountriesinformation.core.network.WorldCountriesApi
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CountriesRepositoryImplTest {

    private val clock: Clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `FORCE_REFRESH emits error when network throws IOException`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        coEvery { dao.getAllCountriesOnce() } returns emptyList()
        coEvery { api.fetchWorldCountriesInformation() } throws IOException("offline")

        val repo = CountriesRepositoryImpl(api, dao, clock)

        repo.getCountries(CachePolicy.FORCE_REFRESH).test {
            assertEquals(ApiResponse.Loading, awaitItem())
            val err = awaitItem() as ApiResponse.Error
            assertTrue(err.exception is IOException)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCountryCacheSnapshot reads dao counts`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        coEvery { dao.getCountryCount() } returns 42
        coEvery { dao.getOldestTimestamp() } returns 1_000L

        val repo = CountriesRepositoryImpl(api, dao, clock)
        val snapshot = repo.getCountryCacheSnapshot()

        assertEquals(42, snapshot.entryCount)
        assertEquals(1_000L, snapshot.oldestEntryLastUpdatedMs)
        coVerify(exactly = 1) { dao.getCountryCount() }
        coVerify(exactly = 1) { dao.getOldestTimestamp() }
    }

    @Test
    fun `clearCountryCache deletes all countries`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        coEvery { dao.deleteAllCountries() } returns Unit

        val repo = CountriesRepositoryImpl(api, dao, clock)
        repo.clearCountryCache()

        coVerify(exactly = 1) { dao.deleteAllCountries() }
    }
}
