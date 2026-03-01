package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GenerateSearchSuggestionsUseCaseTest {

    private lateinit var useCase: GenerateSearchSuggestionsUseCase

    private val testCountries = listOf(
        createCountry("United States", "Washington D.C."),
        createCountry("United Kingdom", "London"),
        createCountry("Canada", "Ottawa"),
        createCountry("United Arab Emirates", "Abu Dhabi"),
        createCountry("Australia", "Canberra"),
        createCountry("Austria", "Vienna"),
        createCountry("Japan", "Tokyo"),
        createCountry("Uruguay", "Montevideo")
    )

    private fun createCountry(name: String, capital: String): Country {
        return Country(
            name = name,
            capital = capital,
            languages = listOf(Language("English", "en")),
            twoLetterCode = name.take(2).uppercase(),
            threeLetterCode = name.take(3).uppercase(),
            population = 1000000,
            region = "TestRegion",
            currencies = listOf(Currency("TestCurrency", "TST", "$")),
            callingCode = "+1",
            latitude = 0.0,
            longitude = 0.0
        )
    }

    @Before
    fun setup() {
        useCase = GenerateSearchSuggestionsUseCase()
    }

    @Test
    fun `should return empty list when query is blank`() {
        val result = useCase("", testCountries)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `should match prefix of country names case insensitive`() {
        val result = useCase("uNi", testCountries)

        assertEquals(
            listOf("United States", "United Kingdom", "United Arab Emirates"),
            result
        )
    }

    @Test
    fun `should include capital cities if needed to reach maxSuggestions`() {
        // Query "can" matches "Canada" (country) and "Canberra" (Australia's capital)
        val result = useCase("can", testCountries)

        assertEquals(
            listOf("Canada", "Canberra"),
            result
        )
    }

    @Test
    fun `should not duplicate country name if capital also matches`() {
        // Query "aus" matches "Australia" and "Austria".
        // They shouldn't appear twice if both country and capital match (though here capital of Austria is Vienna)
        val result = useCase("aus", testCountries)

        assertEquals(
            listOf("Australia", "Austria"),
            result
        )
    }

    @Test
    fun `should respect maxSuggestions limit`() {
        // Query "u" matches U-countries: United States, United Kingdom, United Arab Emirates, Uruguay
        val result = useCase("u", testCountries, maxSuggestions = 2)

        assertEquals(
            listOf("United States", "United Kingdom"),
            result
        )
    }
}
