package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.tests_shared.MainCoroutineRule
import com.vamsi.worldcountriesinformation.tests_shared.TestData
import com.vamsi.worldcountriesinformation.tests_shared.runBlockingTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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