package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeaturedCountrySelectorTest {

    private val selector = FeaturedCountrySelector()

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
    fun select_returnsNullForEmptyList() {
        assertNull(selector.select(emptyList()))
    }

    @Test
    fun select_returnsElementFromList() {
        val a = testCountry.copy(name = "A", threeLetterCode = "AAA")
        val b = testCountry.copy(name = "B", threeLetterCode = "BBB")
        val picked = selector.select(listOf(a, b))
        assertTrue(picked === a || picked === b)
    }
}
