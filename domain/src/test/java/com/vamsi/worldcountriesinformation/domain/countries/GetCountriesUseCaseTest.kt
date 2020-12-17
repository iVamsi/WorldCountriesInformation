package com.vamsi.worldcountriesinformation.domain.countries

import com.nhaarman.mockito_kotlin.whenever
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.tests_shared.MainCoroutineRule
import com.vamsi.worldcountriesinformation.tests_shared.TestData
import com.vamsi.worldcountriesinformation.tests_shared.runBlockingTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.IOException

class GetCountriesUseCaseTest {

    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private lateinit var countriesRepository: CountriesRepository

    private lateinit var useCase: GetCountriesUseCase

    @Before
    fun setUp() {
        countriesRepository = TestCountriesRepository
        useCase = GetCountriesUseCase(countriesRepository, coroutineRule.testDispatcher)
    }

    @Test
    fun  `countries list is returned by repository successfully`(): Unit = coroutineRule.runBlockingTest {
        val expectedResult =  flowOf(ApiResponse.Success(TestData.getCountries())).first()

        val result = useCase.invoke(false).first()

        Assert.assertEquals(expectedResult, result)
    }
}