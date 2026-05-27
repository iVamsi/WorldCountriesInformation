package com.vamsi.worldcountriesinformation.domain.quiz

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateQuizQuestionUseCaseTest {

    private lateinit var getCountriesUseCase: GetCountriesUseCase
    private lateinit var useCase: GenerateQuizQuestionUseCase

    private val countries = listOf(
        summary("United States", "Washington", "Americas", "USA"),
        summary("Canada", "Ottawa", "Americas", "CAN"),
        summary("Japan", "Tokyo", "Asia", "JPN"),
        summary("France", "Paris", "Europe", "FRA"),
        summary("Germany", "Berlin", "Europe", "DEU"),
        summary("Egypt", "Cairo", "Africa", "EGY"),
        summary("Australia", "Canberra", "Oceania", "AUS"),
    )

    @Before
    fun setup() {
        getCountriesUseCase = mockk()
        every { getCountriesUseCase.invoke(CachePolicy.CACHE_ONLY) } returns
            flowOf(ApiResponse.Success(countries))
        useCase = GenerateQuizQuestionUseCase(getCountriesUseCase)
    }

    @Test
    fun `returns null when cache has fewer than four countries`() = runTest {
        every { getCountriesUseCase.invoke(CachePolicy.CACHE_ONLY) } returns
            flowOf(ApiResponse.Success(countries.take(3)))

        val result = useCase(GuessMode.FLAG)

        assertNull(result)
    }

    @Test
    fun `capital mode question has four capital options with one correct`() = runTest {
        val question = useCase(GuessMode.CAPITAL)

        assertNotNull(question)
        question!!
        assertEquals(GuessMode.CAPITAL, question.mode)
        assertEquals(4, question.options.size)
        assertEquals(question.country.capital, question.options[question.correctOptionIndex])
        assertTrue(question.options.distinct().size == 4)
    }

    @Test
    fun `flag mode uses country names as options`() = runTest {
        val question = useCase(GuessMode.FLAG)

        assertNotNull(question)
        question!!
        assertEquals(question.country.name, question.options[question.correctOptionIndex])
    }

    @Test
    fun `region mode uses region strings as options`() = runTest {
        val question = useCase(GuessMode.REGION)

        assertNotNull(question)
        question!!
        assertEquals(question.country.region, question.options[question.correctOptionIndex])
    }

    private fun summary(
        name: String,
        capital: String,
        region: String,
        code: String,
    ) = CountrySummary(
        name = name,
        capital = capital,
        region = region,
        population = 1_000_000,
        twoLetterCode = code.take(2),
        threeLetterCode = code,
        latitude = 0.0,
        longitude = 0.0,
    )
}
