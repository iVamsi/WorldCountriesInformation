package com.vamsi.worldcountriesinformation.feature.settings

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.ClearCacheUseCase
import com.vamsi.worldcountriesinformation.domain.countries.CountryCacheSnapshot
import com.vamsi.worldcountriesinformation.domain.countries.GetCacheStatsUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.ThemeMode
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferences
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferencesPort
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userPreferencesPort: UserPreferencesPort
    private lateinit var getCacheStatsUseCase: GetCacheStatsUseCase
    private lateinit var clearCacheUseCase: ClearCacheUseCase
    private lateinit var viewModel: SettingsViewModel

    private val preferencesFlow = MutableStateFlow(UserPreferences())
    private val testClock: Clock =
        Clock.fixed(Instant.ofEpochMilli(1_000_000L), ZoneOffset.UTC)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userPreferencesPort = mockk(relaxed = true)
        getCacheStatsUseCase = mockk()
        clearCacheUseCase = mockk()
        every { userPreferencesPort.userPreferences } returns preferencesFlow
        coEvery { getCacheStatsUseCase(Unit) } returns ApiResponse.Success(
            CountryCacheSnapshot(
                entryCount = 10,
                oldestEntryLastUpdatedMs = 500_000L,
            ),
        )
        coEvery { userPreferencesPort.updateCachePolicy(any()) } just Runs
        coEvery { userPreferencesPort.updateOfflineMode(any()) } just Runs
        coEvery { userPreferencesPort.updateThemeMode(any()) } just Runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel = SettingsViewModel(
        userPreferencesPort = userPreferencesPort,
        getCacheStatsUseCase = getCacheStatsUseCase,
        clearCacheUseCase = clearCacheUseCase,
        clock = testClock,
    )

    @Test
    fun `init loads cache stats`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(10, viewModel.state.value.cacheStats.entryCount)
        assertEquals(500_000L, viewModel.state.value.cacheStats.oldestEntryAgeMs)
    }

    @Test
    fun `preferences flow updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        preferencesFlow.value = UserPreferences(
            cachePolicy = CachePolicy.NETWORK_FIRST,
            offlineMode = true,
            themeMode = ThemeMode.DARK,
        )
        advanceUntilIdle()

        assertEquals(CachePolicy.NETWORK_FIRST, viewModel.state.value.userPreferences.cachePolicy)
        assertTrue(viewModel.state.value.userPreferences.offlineMode)
        assertEquals(ThemeMode.DARK, viewModel.state.value.userPreferences.themeMode)
    }

    @Test
    fun `update cache policy delegates to port`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processIntent(SettingsContract.Intent.UpdateCachePolicy(CachePolicy.FORCE_REFRESH))
        advanceUntilIdle()

        coVerify { userPreferencesPort.updateCachePolicy(CachePolicy.FORCE_REFRESH) }
    }

    @Test
    fun `clear cache invokes use case and refreshes stats`() = runTest {
        coEvery { clearCacheUseCase(Unit) } returns ApiResponse.Success(Unit)
        coEvery { userPreferencesPort.updateLastCacheClear(any()) } just Runs

        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.processIntent(SettingsContract.Intent.ClearCache)
        advanceUntilIdle()

        coVerify { clearCacheUseCase(Unit) }
        coVerify { userPreferencesPort.updateLastCacheClear(testClock.millis()) }
        coVerify(atLeast = 2) { getCacheStatsUseCase(Unit) }
    }

    @Test
    fun `clear error intent clears error state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.processIntent(SettingsContract.Intent.ClearError)
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
    }
}
