package com.vamsi.worldcountriesinformation.data.countries.repository

import app.cash.turbine.test
import com.vamsi.worldcountriesinformation.core.database.dao.CountryDao
import com.vamsi.worldcountriesinformation.core.database.entity.CountryEntity
import com.vamsi.worldcountriesinformation.core.network.WorldCountriesApi
import com.vamsi.worldcountriesinformation.data.countries.mapper.toCountries
import com.vamsi.worldcountriesinformation.data.countries.mapper.toEntityList
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.SyncOutcome
import com.vamsi.worldcountriesinformation.domain.countries.SyncState
import com.vamsi.worldcountriesinformation.domain.preferences.RefreshInterval
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferences
import com.vamsi.worldcountriesinformation.model.CountriesV3ResponseItem
import com.vamsi.worldcountriesinformation.model.NameV3
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
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
    private val prefs = FakeUserPreferencesPort()

    @Test
    fun `FORCE_REFRESH emits error when network throws IOException`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        coEvery { dao.getCountryCount() } returns 0
        coEvery { api.fetchWorldCountriesInformation() } throws IOException("offline")

        val repo = CountriesRepositoryImpl(api, dao, clock, prefs, FakeSyncStatePort())

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

        val repo = CountriesRepositoryImpl(api, dao, clock, prefs, FakeSyncStatePort())
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

        val repo = CountriesRepositoryImpl(api, dao, clock, prefs, FakeSyncStatePort())
        repo.clearCountryCache()

        coVerify(exactly = 1) { dao.deleteAllCountries() }
    }

    @Test
    fun `CACHE_ONLY emits error when database empty`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        coEvery { dao.getAllCountriesOnce() } returns emptyList()

        val repo = CountriesRepositoryImpl(api, dao, clock, prefs, FakeSyncStatePort())
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

        val repo = CountriesRepositoryImpl(api, dao, clock, prefs, FakeSyncStatePort())
        repo.getCountries(CachePolicy.CACHE_ONLY).test {
            assertEquals(ApiResponse.Loading, awaitItem())
            val first = awaitItem() as ApiResponse.Success
            assertEquals(1, first.data.size)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { api.fetchWorldCountriesInformation() }
    }

    @Test
    fun `CACHE_FIRST respects the refresh interval from settings`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        val threeDaysOld = clock.millis() - 3 * 24 * 3_600_000L
        val entity = testEntity(threeDaysOld)
        coEvery { dao.getCountryCount() } returns 1
        coEvery { dao.getOldestTimestamp() } returns threeDaysOld
        coEvery { dao.getAllCountriesOnce() } returns listOf(entity)
        coEvery { dao.getAllCountries() } returns flowOf(listOf(entity))
        coEvery { api.fetchWorldCountriesInformation() } returns emptyList()
        coEvery { api.fetchPopulation() } returns JsonArray(emptyList())
        coEvery { dao.refreshCountries(any()) } returns Unit

        val weekly = CountriesRepositoryImpl(api, dao, clock, FakeUserPreferencesPort(), FakeSyncStatePort())
        weekly.getCountries(CachePolicy.CACHE_FIRST).test { cancelAndIgnoreRemainingEvents() }
        coVerify(exactly = 0) { api.fetchWorldCountriesInformation() }

        val daily = CountriesRepositoryImpl(
            api,
            dao,
            clock,
            FakeUserPreferencesPort(UserPreferences(refreshInterval = RefreshInterval.DAILY)),
            FakeSyncStatePort(),
        )
        daily.getCountries(CachePolicy.CACHE_FIRST).test { cancelAndIgnoreRemainingEvents() }
        coVerify(exactly = 1) { api.fetchWorldCountriesInformation() }
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

        val repo = CountriesRepositoryImpl(api, dao, clock, prefs, FakeSyncStatePort())
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
        coEvery { api.fetchPopulation() } returns JsonArray(emptyList())
        coEvery { dao.refreshCountries(any()) } returns Unit
        coEvery { dao.getAllCountries() } returns flowOf(entities)

        val repo = CountriesRepositoryImpl(api, dao, clock, prefs, FakeSyncStatePort())
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
    fun `network fetch merges World Bank population by ISO-3 code`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        val apiItem = minimalApiItem()
        val saved = slot<List<CountryEntity>>()

        coEvery { dao.getCountryCount() } returns 0
        coEvery { api.fetchWorldCountriesInformation() } returns listOf(apiItem)
        coEvery { api.fetchPopulation() } returns Json.parseToJsonElement(
            """[{"page":1},[{"countryiso3code":"${apiItem.cca3}","value":27614411},{"countryiso3code":"WLD","value":8200000000}]]""",
        ).jsonArray
        coEvery { dao.refreshCountries(capture(saved)) } returns Unit
        coEvery { dao.getAllCountries() } returns flowOf(emptyList())

        CountriesRepositoryImpl(api, dao, clock, prefs, FakeSyncStatePort()).forceRefresh()

        assertEquals(27_614_411, saved.captured.single().population)
    }

    @Test
    fun `sync skips the database write when the data is unchanged`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        val syncStatePort = FakeSyncStatePort()
        coEvery { api.fetchWorldCountriesInformation() } returns listOf(minimalApiItem())
        coEvery { api.fetchPopulation() } returns JsonArray(emptyList())
        coEvery { dao.refreshCountries(any()) } returns Unit
        val repo = CountriesRepositoryImpl(api, dao, clock, prefs, syncStatePort)

        val first = repo.sync()
        val second = repo.sync()

        assertEquals(SyncOutcome.UPDATED, first.getOrThrow())
        assertEquals(SyncOutcome.UNCHANGED, second.getOrThrow())
        coVerify(exactly = 1) { dao.refreshCountries(any()) }
        assertEquals(clock.millis(), syncStatePort.state.lastCheckedAtMs)
        assertEquals(clock.millis(), syncStatePort.state.lastChangedAtMs)
        assertTrue(syncStatePort.state.fingerprint.isNotEmpty())
    }

    @Test
    fun `sync failure leaves the sync state untouched`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        val syncStatePort = FakeSyncStatePort(SyncState(fingerprint = "old", lastCheckedAtMs = 5L))
        coEvery { api.fetchWorldCountriesInformation() } throws IOException("offline")

        val result = CountriesRepositoryImpl(api, dao, clock, prefs, syncStatePort).sync()

        assertTrue(result.isFailure)
        assertEquals(SyncState(fingerprint = "old", lastCheckedAtMs = 5L), syncStatePort.state)
        coVerify(exactly = 0) { dao.refreshCountries(any()) }
    }

    @Test
    fun `CACHE_FIRST uses the last check time, not the row age`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        val tenDaysOld = clock.millis() - 10 * 24 * 3_600_000L
        val entity = testEntity(tenDaysOld)
        coEvery { dao.getCountryCount() } returns 1
        coEvery { dao.getOldestTimestamp() } returns tenDaysOld
        coEvery { dao.getAllCountriesOnce() } returns listOf(entity)
        coEvery { dao.getAllCountries() } returns flowOf(listOf(entity))
        val checkedAnHourAgo = FakeSyncStatePort(SyncState(fingerprint = "x", lastCheckedAtMs = clock.millis() - 3_600_000L))

        val repo = CountriesRepositoryImpl(api, dao, clock, prefs, checkedAnHourAgo)
        repo.getCountries(CachePolicy.CACHE_FIRST).test { cancelAndIgnoreRemainingEvents() }

        coVerify(exactly = 0) { api.fetchWorldCountriesInformation() }
    }

    @Test
    fun `network fetch keeps countries when population call fails`() = runTest {
        val api = mockk<WorldCountriesApi>()
        val dao = mockk<CountryDao>()
        val saved = slot<List<CountryEntity>>()

        coEvery { api.fetchWorldCountriesInformation() } returns listOf(minimalApiItem())
        coEvery { api.fetchPopulation() } throws IOException("world bank down")
        coEvery { dao.refreshCountries(capture(saved)) } returns Unit

        val result = CountriesRepositoryImpl(api, dao, clock, prefs, FakeSyncStatePort()).forceRefresh()

        assertTrue(result.isSuccess)
        // Falls back to whatever the feed carried (the fixture's 1; the real feed has none).
        assertEquals(1, saved.captured.single().population)
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

        val repo = CountriesRepositoryImpl(api, dao, clock, prefs, FakeSyncStatePort())
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
