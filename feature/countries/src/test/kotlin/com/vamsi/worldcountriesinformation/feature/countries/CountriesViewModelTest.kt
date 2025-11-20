package com.vamsi.worldcountriesinformation.feature.countries

import app.cash.turbine.test
import com.vamsi.worldcountriesinformation.core.datastore.SearchPreferencesDataSource
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.FilteredSearchCountriesUseCase
import com.vamsi.worldcountriesinformation.domain.countries.GenerateSearchSuggestionsUseCase
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.domain.countries.SearchCountriesUseCase
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import com.vamsi.worldcountriesinformation.domainmodel.SearchHistoryEntry
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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

/**
 * Unit tests for CountriesViewModel.
 *
 * Tests MVI pattern implementation including:
 * - Intent handling
 * - State management
 * - Effect emission
 * - Search functionality
 * - Filter operations
 * - Cache management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CountriesViewModelTest {

    private lateinit var viewModel: CountriesViewModel
    private lateinit var getCountriesUseCase: GetCountriesUseCase
    private lateinit var searchCountriesUseCase: SearchCountriesUseCase
    private lateinit var filteredSearchUseCase: FilteredSearchCountriesUseCase
    private lateinit var suggestionsUseCase: GenerateSearchSuggestionsUseCase
    private lateinit var searchPreferencesDataSource: SearchPreferencesDataSource
    private lateinit var searchFiltersUseCase: com.vamsi.worldcountriesinformation.domain.search.SearchFiltersUseCase

    private val testDispatcher = StandardTestDispatcher()

    private val testCountries = listOf(
        Country(
            name = "United States",
            capital = "Washington D.C.",
            languages = listOf(Language("English", "en")),
            twoLetterCode = "US",
            threeLetterCode = "USA",
            population = 331002651,
            region = "Americas",
            currencies = listOf(Currency("US Dollar", "USD", "$")),
            callingCode = "+1",
            latitude = 38.0,
            longitude = -97.0
        ),
        Country(
            name = "Canada",
            capital = "Ottawa",
            languages = listOf(Language("English", "en"), Language("French", "fr")),
            twoLetterCode = "CA",
            threeLetterCode = "CAN",
            population = 37742154,
            region = "Americas",
            currencies = listOf(Currency("Canadian Dollar", "CAD", "$")),
            callingCode = "+1",
            latitude = 56.0,
            longitude = -106.0
        ),
        Country(
            name = "United Kingdom",
            capital = "London",
            languages = listOf(Language("English", "en")),
            twoLetterCode = "GB",
            threeLetterCode = "GBR",
            population = 67886011,
            region = "Europe",
            currencies = listOf(Currency("British Pound", "GBP", "£")),
            callingCode = "+44",
            latitude = 54.0,
            longitude = -2.0
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getCountriesUseCase = mockk()
        searchCountriesUseCase = mockk()
        filteredSearchUseCase = mockk()
        suggestionsUseCase = mockk()
        searchPreferencesDataSource = mockk(relaxed = true)
        searchFiltersUseCase = mockk()

        // Setup default mocks
        coEvery { searchPreferencesDataSource.searchPreferences } returns flowOf(
            com.vamsi.worldcountriesinformation.core.datastore.SearchPreferences()
        )
        every { suggestionsUseCase(any(), any(), any()) } returns emptyList()
        every { filteredSearchUseCase.applyFiltersAndSort(any(), any()) } answers { firstArg() }
        every { searchFiltersUseCase.hasActiveFilters(any()) } returns false
        every { searchFiltersUseCase.getActiveFilterCount(any()) } returns 0
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CountriesViewModel {
        return CountriesViewModel(
            getCountriesUseCase = getCountriesUseCase,
            searchCountriesUseCase = searchCountriesUseCase,
            filteredSearchUseCase = filteredSearchUseCase,
            suggestionsUseCase = suggestionsUseCase,
            searchPreferencesDataSource = searchPreferencesDataSource,
            searchFiltersUseCase = searchFiltersUseCase
        )
    }

    // ============================================================================
    // Initial State Tests
    // ============================================================================

    @Test
    fun `initial state should be correct`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Loading)

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(emptyList<Country>(), state.countries)
        assertEquals(emptyList<Country>(), state.filteredCountries)
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearchActive)
        assertFalse(state.isSearchFocused)
        assertEquals(emptyList<SearchHistoryEntry>(), state.searchHistory)
        assertEquals(emptySet<String>(), state.selectedRegions)
        assertEquals(SortOrder.NAME_ASC, state.sortOrder)
        assertEquals(null, state.errorMessage)
    }

    // ============================================================================
    // Load Countries Tests
    // ============================================================================

    @Test
    fun `loading countries successfully should update state`() = runTest {
        // Given
        coEvery { getCountriesUseCase(CachePolicy.CACHE_FIRST) } returns flowOf(
            ApiResponse.Loading,
            ApiResponse.Success(testCountries)
        )

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(testCountries, state.countries)
            assertEquals(testCountries, state.filteredCountries)
            assertFalse(state.isLoading)
            assertEquals(null, state.errorMessage)
        }
    }

    @Test
    fun `loading countries with error should update error state`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { getCountriesUseCase(CachePolicy.CACHE_FIRST) } returns flowOf(
            ApiResponse.Loading,
            ApiResponse.Error(Exception(errorMessage))
        )

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.errorMessage?.contains("Network") == true)
        }
    }

    // ============================================================================
    // Search Tests
    // ============================================================================

    @Test
    fun `search query change should update state`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        coEvery { searchCountriesUseCase(any()) } returns flowOf(testCountries.take(1))
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.SearchQueryChanged("United"))
        advanceTimeBy(400) // Advance past debounce delay
        advanceUntilIdle()

        // Then
        assertEquals("United", viewModel.state.value.searchQuery)
        assertTrue(viewModel.state.value.isSearchActive)
    }

    @Test
    fun `clear search should reset search state`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        coEvery { searchCountriesUseCase(any()) } returns flowOf(testCountries.take(1))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processIntent(CountriesContract.Intent.SearchQueryChanged("United"))
        advanceTimeBy(400)
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.ClearSearch)
        advanceUntilIdle()

        // Then
        assertEquals("", viewModel.state.value.searchQuery)
        assertFalse(viewModel.state.value.isSearchActive)
    }

    // ============================================================================
    // Filter Tests
    // ============================================================================

    @Test
    fun `toggle region should update preferences`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        coEvery { searchPreferencesDataSource.updateSelectedRegions(any()) } just Runs
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.ToggleRegion("Americas"))
        advanceUntilIdle()

        // Then
        coVerify { searchPreferencesDataSource.updateSelectedRegions(setOf("Americas")) }
    }

    @Test
    fun `change sort order should update preferences`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        coEvery { searchPreferencesDataSource.updateSortOrder(any()) } just Runs
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.ChangeSortOrder(SortOrder.POPULATION_DESC))
        advanceUntilIdle()

        // Then
        coVerify { searchPreferencesDataSource.updateSortOrder(SortOrder.POPULATION_DESC) }
    }

    @Test
    fun `clear filters should reset all filters`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        coEvery { searchPreferencesDataSource.clearFilters() } just Runs
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.ClearFilters)
        advanceUntilIdle()

        // Then
        coVerify { searchPreferencesDataSource.clearFilters() }
    }

    // ============================================================================
    // Refresh Tests
    // ============================================================================

    @Test
    fun `refresh should use force refresh policy`() = runTest {
        // Given
        coEvery { getCountriesUseCase(CachePolicy.CACHE_FIRST) } returns flowOf(
            ApiResponse.Success(testCountries)
        )
        coEvery { getCountriesUseCase(CachePolicy.FORCE_REFRESH) } returns flowOf(
            ApiResponse.Success(testCountries)
        )
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.RefreshCountries)
        advanceUntilIdle()

        // Then
        coVerify { getCountriesUseCase(CachePolicy.FORCE_REFRESH) }
    }

    @Test
    fun `refresh should set refreshing state`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.RefreshCountries)

        // Then (should be refreshing initially)
        assertTrue(viewModel.state.value.isRefreshing)
    }

    // ============================================================================
    // Navigation Tests
    // ============================================================================

    @Test
    fun `country clicked should emit navigation effect`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        viewModel = createViewModel()
        advanceUntilIdle()

        // When/Then
        viewModel.effect.test {
            viewModel.processIntent(CountriesContract.Intent.CountryClicked("USA"))
            val effect = awaitItem()
            assertTrue(effect is CountriesContract.Effect.NavigateToDetails)
            assertEquals("USA", (effect as CountriesContract.Effect.NavigateToDetails).countryCode)
        }
    }

    // ============================================================================
    // Favorite Tests
    // ============================================================================

    @Test
    fun `toggle favorite should update favorites set`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.ToggleFavorite("USA"))
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value.favoriteCountryCodes.contains("USA"))
    }

    @Test
    fun `toggle favorite twice should remove from favorites`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.ToggleFavorite("USA"))
        advanceUntilIdle()
        viewModel.processIntent(CountriesContract.Intent.ToggleFavorite("USA"))
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.state.value.favoriteCountryCodes.contains("USA"))
    }

    // ============================================================================
    // Cache Management Tests
    // ============================================================================

    @Test
    fun `getCacheAge should return never for zero timestamp`() {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Loading)
        viewModel = createViewModel()

        // When
        val age = viewModel.getCacheAge()

        // Then
        assertEquals("Never", age)
    }

    @Test
    fun `isCacheFresh should return false for zero timestamp`() {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Loading)
        viewModel = createViewModel()

        // When
        val isFresh = viewModel.isCacheFresh()

        // Then
        assertFalse(isFresh)
    }

    // ============================================================================
    // Search History Tests
    // ============================================================================

    @Test
    fun `saveSearchToHistory should save query`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        coEvery { searchCountriesUseCase(any()) } returns flowOf(testCountries.take(1))
        coEvery { searchPreferencesDataSource.addToSearchHistory(any()) } just Runs
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processIntent(CountriesContract.Intent.SearchQueryChanged("United"))
        advanceTimeBy(400)
        advanceUntilIdle()

        // When
        viewModel.saveSearchToHistory()
        advanceUntilIdle()

        // Then
        coVerify { searchPreferencesDataSource.addToSearchHistory("United") }
    }

    @Test
    fun `clearSearchHistory should clear history`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        coEvery { searchPreferencesDataSource.clearSearchHistory() } just Runs
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.ClearSearchHistory)
        advanceUntilIdle()

        // Then
        coVerify { searchPreferencesDataSource.clearSearchHistory() }
    }

    @Test
    fun `search history from preferences should update state`() = runTest {
        // Given
        val history = listOf(SearchHistoryEntry(query = "USA"))
        coEvery { searchPreferencesDataSource.searchPreferences } returns flowOf(
            com.vamsi.worldcountriesinformation.core.datastore.SearchPreferences(
                searchHistory = history
            )
        )
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(history, viewModel.state.value.searchHistory)
    }

    @Test
    fun `search history should show when focused with entries`() = runTest {
        // Given
        val history = listOf(SearchHistoryEntry(query = "USA"))
        coEvery { searchPreferencesDataSource.searchPreferences } returns flowOf(
            com.vamsi.worldcountriesinformation.core.datastore.SearchPreferences(
                searchHistory = history
            )
        )
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))

        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.processIntent(CountriesContract.Intent.SearchFocusChanged(true))
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value.shouldShowSearchHistory)
    }

    @Test
    fun `delete history intent should remove entry`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        coEvery { searchPreferencesDataSource.removeFromSearchHistory(any()) } just Runs
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.DeleteSearchHistoryItem("USA"))
        advanceUntilIdle()

        // Then
        coVerify { searchPreferencesDataSource.removeFromSearchHistory("USA") }
    }

    @Test
    fun `history item selection should restore query`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(ApiResponse.Success(testCountries))
        coEvery { searchCountriesUseCase(any()) } returns flowOf(testCountries.take(1))
        coEvery { searchPreferencesDataSource.addToSearchHistory(any()) } just Runs
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.SearchHistoryItemSelected("Canada"))
        advanceUntilIdle()

        // Then
        assertEquals("Canada", viewModel.state.value.searchQuery)
        coVerify { searchPreferencesDataSource.addToSearchHistory("Canada") }
    }

    // ============================================================================
    // Error Handling Tests
    // ============================================================================

    @Test
    fun `error shown intent should clear error message`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(
            ApiResponse.Error(Exception("Error"))
        )
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.processIntent(CountriesContract.Intent.ErrorShown)
        advanceUntilIdle()

        // Then
        assertEquals(null, viewModel.state.value.errorMessage)
    }

    @Test
    fun `retry should reload countries with cache first policy`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(
            ApiResponse.Error(Exception("Error"))
        )
        viewModel = createViewModel()
        advanceUntilIdle()

        clearMocks(getCountriesUseCase)
        coEvery { getCountriesUseCase(CachePolicy.CACHE_FIRST) } returns flowOf(
            ApiResponse.Success(testCountries)
        )

        // When
        viewModel.processIntent(CountriesContract.Intent.RetryLoading)
        advanceUntilIdle()

        // Then
        coVerify { getCountriesUseCase(CachePolicy.CACHE_FIRST) }
    }
}
