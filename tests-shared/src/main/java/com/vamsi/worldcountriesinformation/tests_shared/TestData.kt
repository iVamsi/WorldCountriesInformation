package com.vamsi.worldcountriesinformation.tests_shared

import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language

object TestData {

    private val languages = listOf(
        Language(name = "Hindi"),
        Language(name = "English"),
        Language(name = "French"),
        Language(name = "Spanish"),
    )
    private val currencies = listOf(
        Currency(code = "INR"),
        Currency(code = "USD"),
        Currency(code = "EUR"),
        Currency(code = "GBP"),
    )

    fun getCountries(): List<Country> = listOf(
        Country(
            name = "India",
            capital = "New Delhi",
            languages = languages,
            twoLetterCode = "IN",
            threeLetterCode = "IND",
            population = 1_295_210_000,
            region = "Asia",
            currencies = currencies,
            callingCode = "91",
            latitude = 20.0,
            longitude = 77.0,
        ),
        Country(
            name = "United States of America",
            capital = "Washington, D.C.",
            languages = languages,
            twoLetterCode = "US",
            threeLetterCode = "USA",
            population = 323_947_000,
            region = "Americas",
            currencies = currencies,
            callingCode = "1",
            latitude = 38.0,
            longitude = -97.0,
        ),
    )

    fun getCountrySummaries(): List<CountrySummary> = listOf(
        CountrySummary(
            name = "India",
            capital = "New Delhi",
            region = "Asia",
            population = 1_295_210_000,
            twoLetterCode = "IN",
            threeLetterCode = "IND",
            latitude = 20.0,
            longitude = 77.0,
        ),
        CountrySummary(
            name = "United States of America",
            capital = "Washington, D.C.",
            region = "Americas",
            population = 323_947_000,
            twoLetterCode = "US",
            threeLetterCode = "USA",
            latitude = 38.0,
            longitude = -97.0,
        ),
    )
}
