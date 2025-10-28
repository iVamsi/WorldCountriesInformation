package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.tests_shared.TestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class GetCountriesUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var countriesRepository: CountriesRepository
    private lateinit var testCase: GetCountriesUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        countriesRepository = mockk()
        testCase = GetCountriesUseCase(countriesRepository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `countries list is returned by repository successfully`() = runTest(testDispatcher) {
        // Given
        val expectedCountries = TestData.getCountries()
        val expectedResult = flowOf(ApiResponse.Success(expectedCountries))
        coEvery { countriesRepository.getCountries() } returns expectedResult

        // When
        val result = testCase.invoke(CachePolicy.CACHE_FIRST).toList()

        // Then
        Assert.assertEquals(1, result.size)
        Assert.assertTrue(result[0] is ApiResponse.Success)
        Assert.assertEquals(expectedCountries, (result[0] as ApiResponse.Success).data)
    }

    @Test
    fun `repository returns error when exception is thrown while fetching countries`() = runTest(testDispatcher) {
        // Given
        val expectedException = IOException("Network error")
        val expectedError = flowOf(ApiResponse.Error(expectedException))
        coEvery { countriesRepository.getCountries() } returns expectedError

        // When
        val result = testCase.invoke(CachePolicy.CACHE_FIRST).toList()

        // Then
        Assert.assertEquals(1, result.size)
        Assert.assertTrue(result[0] is ApiResponse.Error)
        Assert.assertEquals(expectedException, (result[0] as ApiResponse.Error).exception)
    }
}
