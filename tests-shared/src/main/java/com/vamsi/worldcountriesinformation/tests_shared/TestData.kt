package com.vamsi.worldcountriesinformation.tests_shared

import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language

object TestData {

    private val languages = listOf(
        Language(name = "Hindi"),
        Language(name = "English"),
        Language(name = "French"),
        Language(name = "Spanish")
    )
    private val currencies = listOf(
        Currency(code = "INR"),
        Currency(code = "USD"),
        Currency(code = "EUR"),
        Currency(code = "GBP")
    )

    fun getCountries(): List<Country> {
        return listOf(
            Country(
                name = "India",
                capital = "New Delhi",
                languages = languages,
                twoLetterCode = "IN",
                threeLetterCode = "IND",
                population = 1295210000,
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
                population = 323947000,
                region = "Americas",
                currencies = currencies,
                callingCode = "1",
                latitude = 38.0,
                longitude = -97.0,
            )
        )
    }
}