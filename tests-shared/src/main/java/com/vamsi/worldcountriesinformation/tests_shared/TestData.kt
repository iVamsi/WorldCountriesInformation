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
            Country("India", "New Delhi", languages, "IN", "IND", 1295210000, "Asia", currencies, "91", 20.0, 77.0),
            Country("United States of America", "Washington, D.C.", languages, "US", "USA", 323947000, "Americas", currencies, "1", 38.0, -97.0)
        )
    }
}