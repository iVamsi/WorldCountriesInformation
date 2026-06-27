package com.vamsi.worldcountriesinformation.domain.preferences

import app.cash.turbine.test
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetUserDataPolicyUseCaseTest {
    private lateinit var userPreferencesPort: UserPreferencesPort
    private lateinit var useCase: GetUserDataPolicyUseCase
    private val preferencesFlow = MutableStateFlow(UserPreferences())

    @Before
    fun setup() {
        userPreferencesPort = mockk()
        every { userPreferencesPort.userPreferences } returns preferencesFlow
        useCase = GetUserDataPolicyUseCase(userPreferencesPort)
    }

    @Test
    fun `emits stored policy when online`() = runTest {
        preferencesFlow.value = UserPreferences(cachePolicy = CachePolicy.NETWORK_FIRST)

        useCase().test {
            assertEquals(CachePolicy.NETWORK_FIRST, awaitItem())
        }
    }

    @Test
    fun `emits CACHE_ONLY when offline mode enabled`() = runTest {
        preferencesFlow.value =
            UserPreferences(
                cachePolicy = CachePolicy.FORCE_REFRESH,
                offlineMode = true,
            )

        useCase().test {
            assertEquals(CachePolicy.CACHE_ONLY, awaitItem())
        }
    }

    @Test
    fun `updates when preferences change`() = runTest {
        useCase().test {
            assertEquals(CachePolicy.CACHE_FIRST, awaitItem())

            preferencesFlow.value = UserPreferences(cachePolicy = CachePolicy.CACHE_ONLY)
            assertEquals(CachePolicy.CACHE_ONLY, awaitItem())
        }
    }
}
