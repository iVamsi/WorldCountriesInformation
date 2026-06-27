package com.vamsi.worldcountriesinformation.domain.preferences

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FavoritesPreferencesTest {
    private lateinit var userPreferencesPort: UserPreferencesPort
    private lateinit var observeFavoritesUseCase: ObserveFavoritesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private val preferencesFlow = MutableStateFlow(UserPreferences())

    @Before
    fun setup() {
        userPreferencesPort = mockk(relaxed = true)
        every { userPreferencesPort.userPreferences } returns preferencesFlow
        observeFavoritesUseCase = ObserveFavoritesUseCase(userPreferencesPort)
        toggleFavoriteUseCase =
            ToggleFavoriteUseCase(
                userPreferencesPort = userPreferencesPort,
                ioDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(),
            )
    }

    @Test
    fun `observe favorites maps favorite codes from preferences`() = runTest {
        preferencesFlow.value = UserPreferences(favoriteCountryCodes = setOf("USA", "IND"))

        observeFavoritesUseCase().test {
            assertEquals(setOf("USA", "IND"), awaitItem())
        }
    }

    @Test
    fun `toggle favorite delegates to port`() = runTest {
        coEvery { userPreferencesPort.toggleFavorite("usa") } returns Unit

        toggleFavoriteUseCase("usa")

        coVerify { userPreferencesPort.toggleFavorite("usa") }
    }
}
