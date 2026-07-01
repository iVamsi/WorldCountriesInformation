package com.vamsi.worldcountriesinformation.feature.compare

import app.cash.turbine.test
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.CountryByCodeParams
import com.vamsi.worldcountriesinformation.domain.countries.GetCountryByCodeUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.GetUserDataPolicyUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferences
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferencesPort
import com.vamsi.worldcountriesinformation.tests_shared.TestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompareViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getCountryByCodeUseCase: GetCountryByCodeUseCase
    private lateinit var getUserDataPolicyUseCase: GetUserDataPolicyUseCase
    private lateinit var userPreferencesPort: UserPreferencesPort
    private lateinit var viewModel: CompareViewModel

    private val usa = TestData.getCountries()[1]
    private val india = TestData.getCountries()[0]

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCountryByCodeUseCase = mockk()
        getUserDataPolicyUseCase = mockk()
        userPreferencesPort = mockk()
        every { getUserDataPolicyUseCase() } returns flowOf(CachePolicy.CACHE_FIRST)
        every { userPreferencesPort.userPreferences } returns flowOf(UserPreferences(aiSummaryEnabled = false))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CompareViewModel = CompareViewModel(
        getCountryByCodeUseCase = getCountryByCodeUseCase,
        getUserDataPolicyUseCase = getUserDataPolicyUseCase,
        userPreferencesPort = userPreferencesPort,
    )

    @Test
    fun `load with fewer than two codes sets error`() = runTest {
        viewModel = createViewModel()

        viewModel.processIntent(CompareContract.Intent.LoadCountries(listOf("USA")))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.countries.isEmpty())
    }

    @Test
    fun `load countries success updates state`() = runTest {
        coEvery { getCountryByCodeUseCase(CountryByCodeParams("USA", CachePolicy.CACHE_FIRST)) } returns
            flowOf(ApiResponse.Success(usa))
        coEvery { getCountryByCodeUseCase(CountryByCodeParams("IND", CachePolicy.CACHE_FIRST)) } returns
            flowOf(ApiResponse.Success(india))

        viewModel = createViewModel()
        viewModel.processIntent(CompareContract.Intent.LoadCountries(listOf("USA", "IND")))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(null, viewModel.state.value.error)
        assertEquals(2, viewModel.state.value.countries.size)
        assertTrue(viewModel.state.value.hasData)
    }

    @Test
    fun `navigate back emits effect`() = runTest {
        viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.processIntent(CompareContract.Intent.NavigateBack)
            assertEquals(CompareContract.Effect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `failed fetch for one country sets error`() = runTest {
        coEvery { getCountryByCodeUseCase(CountryByCodeParams("USA", CachePolicy.CACHE_FIRST)) } returns
            flowOf(ApiResponse.Success(usa))
        coEvery { getCountryByCodeUseCase(CountryByCodeParams("ZZZ", CachePolicy.CACHE_FIRST)) } returns
            flowOf(ApiResponse.Error(Exception("not found")))

        viewModel = createViewModel()
        viewModel.processIntent(CompareContract.Intent.LoadCountries(listOf("USA", "ZZZ")))
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.countries.size < 2)
    }
}
