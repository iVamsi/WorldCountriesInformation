package com.vamsi.worldcountriesinformation.domain.countries

import com.nhaarman.mockito_kotlin.whenever
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.tests_shared.TestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class GetCountriesUseCaseTest {

    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher)

    @Mock
    private lateinit var countriesRepository: CountriesRepository

    private lateinit var testCase: GetCountriesUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        testCase = GetCountriesUseCase(countriesRepository, testCoroutineDispatcher)
        Dispatchers.setMain(testCoroutineDispatcher)
    }

    @Test
    fun  `countries list is returned by repository successfully`()  = testCoroutineScope.runBlockingTest{
        val expectedResult =  flowOf(ApiResponse.Success(TestData.getCountries()))
        whenever(countriesRepository.getCountries()).thenReturn(expectedResult)

        val result = testCase.invoke(false)

        Assert.assertEquals(expectedResult, result)
    }

    @Test
    fun  `repository returns error when exception is thrown while fetching currencies`()  = testCoroutineScope.runBlockingTest{
        val expectedError =  flowOf(ApiResponse.Error(IOException()))
        whenever(countriesRepository.getCountries()).thenReturn(expectedError)

        val error = testCase.invoke(false)

        Assert.assertEquals(expectedError, error)
    }
}