package com.vamsi.worldcountriesinformation.feature.countrydetails

import app.cash.turbine.test
import com.vamsi.worldcountriesinformation.core.datastore.SearchPreferencesPort
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.CountryByCodeParams
import com.vamsi.worldcountriesinformation.domain.countries.GenerateCountrySummaryUseCase
import com.vamsi.worldcountriesinformation.domain.countries.GetCountryByCodeUseCase
import com.vamsi.worldcountriesinformation.domain.countries.GetNearbyCountriesUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.GetUserDataPolicyUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.ObserveFavoritesUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.ToggleFavoriteUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferencesPort
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import com.vamsi.worldcountriesinformation.tests_shared.TestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class CountryDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getCountryByCodeUseCase: GetCountryByCodeUseCase
    private lateinit var getNearbyCountriesUseCase: GetNearbyCountriesUseCase
    private lateinit var searchPreferencesDataSource: SearchPreferencesPort
    private lateinit var userPreferencesPort: UserPreferencesPort
    private lateinit var getUserDataPolicyUseCase: GetUserDataPolicyUseCase
    private lateinit var observeFavoritesUseCase: ObserveFavoritesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var generateCountrySummaryUseCase: GenerateCountrySummaryUseCase
    private lateinit var viewModel: CountryDetailsViewModel

    private val favoritesFlow = MutableStateFlow<Set<String>>(emptySet())
    private val testCountry: Country = TestData.getCountries()[0]
    private val nearby = listOf(
        CountrySummary(
            name = "Pakistan",
            capital = "Islamabad",
            region = "Asia",
            population = 1,
            twoLetterCode = "PK",
            threeLetterCode = "PAK",
            latitude = 0.0,
            longitude = 0.0,
        ),
    )
    private val testClock: Clock =
        Clock.fixed(Instant.ofEpochMilli(2_000_000L), ZoneOffset.UTC)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCountryByCodeUseCase = mockk()
        getNearbyCountriesUseCase = mockk()
        searchPreferencesDataSource = mockk(relaxed = true)
        userPreferencesPort = mockk()
        getUserDataPolicyUseCase = mockk()
        observeFavoritesUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        generateCountrySummaryUseCase = mockk(relaxed = true)

        every { getUserDataPolicyUseCase() } returns flowOf(CachePolicy.CACHE_FIRST)
        every { observeFavoritesUseCase() } returns favoritesFlow
        every { userPreferencesPort.userPreferences } returns flowOf(
            com.vamsi.worldcountriesinformation.domain.preferences.UserPreferences(
                aiSummaryEnabled = false,
            ),
        )
        coEvery { toggleFavoriteUseCase(any()) } returns ApiResponse.Success(Unit)
        coEvery { searchPreferencesDataSource.addToRecentlyViewedCountry(any()) } just Runs
        every { getNearbyCountriesUseCase(any(), any()) } returns flowOf(nearby)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CountryDetailsViewModel = CountryDetailsViewModel(
        getCountryByCodeUseCase = getCountryByCodeUseCase,
        getNearbyCountriesUseCase = getNearbyCountriesUseCase,
        searchPreferencesDataSource = searchPreferencesDataSource,
        userPreferencesPort = userPreferencesPort,
        getUserDataPolicyUseCase = getUserDataPolicyUseCase,
        observeFavoritesUseCase = observeFavoritesUseCase,
        toggleFavoriteUseCase = toggleFavoriteUseCase,
        generateCountrySummaryUseCase = generateCountrySummaryUseCase,
        clock = testClock,
    )

    @Test
    fun `load country details success updates state`() = runTest {
        coEvery { getCountryByCodeUseCase(CountryByCodeParams("IND", CachePolicy.CACHE_FIRST)) } returns
            flowOf(ApiResponse.Loading, ApiResponse.Success(testCountry))

        viewModel = createViewModel()
        viewModel.processIntent(CountryDetailsContract.Intent.LoadCountryDetails("IND"))
        advanceUntilIdle()

        assertEquals(testCountry, viewModel.state.value.country)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(nearby, viewModel.state.value.nearbyCountries)
    }

    @Test
    fun `navigate back emits effect`() = runTest {
        viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.processIntent(CountryDetailsContract.Intent.NavigateBack)
            assertEquals(CountryDetailsContract.Effect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `toggle favorite when country loaded invokes use case`() = runTest {
        coEvery { getCountryByCodeUseCase(CountryByCodeParams("IND", CachePolicy.CACHE_FIRST)) } returns
            flowOf(ApiResponse.Success(testCountry))

        viewModel = createViewModel()
        viewModel.processIntent(CountryDetailsContract.Intent.LoadCountryDetails("IND"))
        advanceUntilIdle()
        viewModel.processIntent(CountryDetailsContract.Intent.ToggleFavorite)
        advanceUntilIdle()

        coVerify { toggleFavoriteUseCase("IND") }
    }

    @Test
    fun `countryCodeToFlagEmoji converts two letter code`() {
        assertEquals("🇺🇸", CountryDetailsViewModel.countryCodeToFlagEmoji("US"))
        assertEquals("🇮🇳", CountryDetailsViewModel.countryCodeToFlagEmoji("IN"))
    }

    @Test
    fun `formatShareText includes country name and capital`() {
        val text = CountryDetailsViewModel.formatShareText(testCountry)

        assertTrue(text.contains(testCountry.name))
        assertTrue(text.contains(testCountry.capital))
    }

    @Test
    fun `getCacheAge returns Never when not loaded`() = runTest {
        viewModel = createViewModel()
        assertEquals("Never", viewModel.getCacheAge())
        assertFalse(viewModel.isCacheFresh())
    }
}
