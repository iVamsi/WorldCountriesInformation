package com.vamsi.worldcountriesinformation.feature.widget.data

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.FeaturedCountrySelector
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.GetUserDataPolicyUseCase
import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetDataSourceTest {

    private val testCountry = CountrySummary(
        name = "Testland",
        capital = "Test City",
        twoLetterCode = "TL",
        threeLetterCode = "TLS",
        population = 1,
        region = "Test",
        latitude = 0.0,
        longitude = 0.0,
    )

    @Test
    fun getWidgetData_invokesUseCaseExactlyOnce() = runTest {
        val ioDispatcher = UnconfinedTestDispatcher()
        val useCase = mockk<GetCountriesUseCase>()
        val policyUseCase = mockk<GetUserDataPolicyUseCase>()
        every { policyUseCase() } returns flowOf(CachePolicy.CACHE_FIRST)
        every { useCase.invoke(CachePolicy.CACHE_FIRST) } returns flowOf(
            ApiResponse.Success(listOf(testCountry)),
        )
        val dataSource = WidgetDataSource(
            getCountriesUseCase = useCase,
            getUserDataPolicyUseCase = policyUseCase,
            featuredCountrySelector = FeaturedCountrySelector(),
            ioDispatcher = ioDispatcher,
        )

        val result = dataSource.getWidgetData()

        assertEquals(1, result.totalCountries)
        assertEquals(testCountry, result.featuredCountry)
        verify(exactly = 1) { useCase.invoke(CachePolicy.CACHE_FIRST) }
    }

    @Test
    fun getWidgetData_propagatesCancellationException() = runTest {
        val ioDispatcher = UnconfinedTestDispatcher()
        val useCase = mockk<GetCountriesUseCase>()
        val policyUseCase = mockk<GetUserDataPolicyUseCase>()
        every { policyUseCase() } returns flowOf(CachePolicy.CACHE_FIRST)
        every { useCase.invoke(CachePolicy.CACHE_FIRST) } returns flow {
            throw CancellationException()
        }
        val dataSource = WidgetDataSource(
            getCountriesUseCase = useCase,
            getUserDataPolicyUseCase = policyUseCase,
            featuredCountrySelector = FeaturedCountrySelector(),
            ioDispatcher = ioDispatcher,
        )

        try {
            dataSource.getWidgetData()
            throw AssertionError("Expected CancellationException")
        } catch (e: CancellationException) {
            // expected
        }

        verify(exactly = 1) { useCase.invoke(CachePolicy.CACHE_FIRST) }
    }
}
