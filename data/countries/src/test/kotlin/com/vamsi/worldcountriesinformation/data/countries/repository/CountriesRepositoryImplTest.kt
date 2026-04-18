package com.vamsi.worldcountriesinformation.data.countries.repository

import app.cash.turbine.test
import com.vamsi.worldcountriesinformation.core.database.dao.CountryDao
import com.vamsi.worldcountriesinformation.core.database.entity.CountryEntity
import com.vamsi.worldcountriesinformation.core.network.WorldCountriesApi
import com.vamsi.worldcountriesinformation.data.countries.mapper.toCountries
import com.vamsi.worldcountriesinformation.data.countries.mapper.toEntityList
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.model.CountriesV3ResponseItem
import com.vamsi.worldcountriesinformation.model.NameV3
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CountriesRepositoryImplTest {

    private val clock: Clock =
        Clock.fixed(Instant.ofEpochMilli(1_735_689_600_000L), ZoneOffset.UTC)

    @Test
    fun `FORCE_REFRESH emits error when network throws IOException`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        coEvery { dao.getCountryCount() } returns 0
        coEvery { api.fetchWorldCountriesInformation() } throws IOException("offline")

        val repo = CountriesRepositoryImpl(api, dao, clock)

        repo.getCountries(CachePolicy.FORCE_REFRESH).test {
            assertEquals(ApiResponse.Loading, awaitItem())
            val err = awaitItem() as ApiResponse.Error
            assertTrue(err.exception is IOException)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { dao.getAllCountriesOnce() }
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

    @Test
    fun `CACHE_ONLY emits error when database empty`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        coEvery { dao.getAllCountriesOnce() } returns emptyList()

        val repo = CountriesRepositoryImpl(api, dao, clock)
        repo.getCountries(CachePolicy.CACHE_ONLY).test {
            assertEquals(ApiResponse.Loading, awaitItem())
            val err = awaitItem() as ApiResponse.Error
            assertTrue(err.exception.message!!.contains("No cached", ignoreCase = true))
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { api.fetchWorldCountriesInformation() }
    }

    @Test
    fun `CACHE_ONLY emits cached rows and never calls network`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        val entity = testEntity(clock.millis())
        coEvery { dao.getAllCountriesOnce() } returns listOf(entity)
        coEvery { dao.getAllCountries() } returns flowOf(listOf(entity))

        val repo = CountriesRepositoryImpl(api, dao, clock)
        repo.getCountries(CachePolicy.CACHE_ONLY).test {
            assertEquals(ApiResponse.Loading, awaitItem())
            val first = awaitItem() as ApiResponse.Success
            assertEquals(1, first.data.size)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { api.fetchWorldCountriesInformation() }
    }

    @Test
    fun `CACHE_FIRST does not call network when cache is fresh`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        val lastUpdated = clock.millis() - 3_600_000L // 1 hour — within 24h window
        val entity = testEntity(lastUpdated)
        coEvery { dao.getCountryCount() } returns 1
        coEvery { dao.getOldestTimestamp() } returns lastUpdated
        coEvery { dao.getAllCountriesOnce() } returns listOf(entity)
        coEvery { dao.getAllCountries() } returns flowOf(listOf(entity))

        val repo = CountriesRepositoryImpl(api, dao, clock)
        repo.getCountries(CachePolicy.CACHE_FIRST).test {
            assertEquals(ApiResponse.Loading, awaitItem())
            val first = awaitItem() as ApiResponse.Success
            assertEquals(1, first.data.size)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { api.fetchWorldCountriesInformation() }
        coVerify(exactly = 1) { dao.getCountryCount() }
        coVerify(exactly = 1) { dao.getOldestTimestamp() }
        coVerify(exactly = 1) { dao.getAllCountriesOnce() }
    }

    @Test
    fun `NETWORK_FIRST fetches from network when cache empty`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        val apiItem = minimalApiItem()
        val entities = listOf(apiItem).toCountries().toEntityList()

        coEvery { dao.getCountryCount() } returns 0
        coEvery { api.fetchWorldCountriesInformation() } returns listOf(apiItem)
        coEvery { dao.refreshCountries(any()) } returns Unit
        coEvery { dao.getAllCountries() } returns flowOf(entities)

        val repo = CountriesRepositoryImpl(api, dao, clock)
        repo.getCountries(CachePolicy.NETWORK_FIRST).test {
            assertEquals(ApiResponse.Loading, awaitItem())
            val success = awaitItem() as ApiResponse.Success
            assertTrue(success.data.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 1) { api.fetchWorldCountriesInformation() }
        coVerify(exactly = 1) { dao.refreshCountries(any()) }
        coVerify(exactly = 0) { dao.getAllCountriesOnce() }
    }

    @Test
    fun `NETWORK_FIRST emits cache when network fails and cache exists`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        val entity = testEntity(clock.millis())
        coEvery { dao.getCountryCount() } returns 1
        coEvery { api.fetchWorldCountriesInformation() } throws IOException("offline")
        coEvery { dao.getAllCountriesOnce() } returns listOf(entity)
        coEvery { dao.getAllCountries() } returns flowOf(listOf(entity))

        val repo = CountriesRepositoryImpl(api, dao, clock)
        repo.getCountries(CachePolicy.NETWORK_FIRST).test {
            assertEquals(ApiResponse.Loading, awaitItem())
            val success = awaitItem() as ApiResponse.Success
            assertEquals(1, success.data.size)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 1) { dao.getAllCountriesOnce() }
        coVerify(exactly = 1) { api.fetchWorldCountriesInformation() }
    }

    private fun testEntity(lastUpdated: Long) = CountryEntity(
        threeLetterCode = "USA",
        twoLetterCode = "US",
        name = "United States",
        capital = "Washington",
        region = "Americas",
        population = 1,
        callingCode = "+1",
        latitude = 0.0,
        longitude = 0.0,
        languages = emptyList(),
        currencies = emptyList(),
        lastUpdated = lastUpdated,
    )

    private fun minimalApiItem() = CountriesV3ResponseItem(
        cca2 = "US",
        cca3 = "USA",
        name = NameV3(common = "United States"),
        capital = listOf("Washington"),
        region = "Americas",
        population = 1L,
        latlng = listOf(0.0, 0.0),
    )
}
