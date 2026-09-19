package com.vamsi.worldcountriesinformation.data.countries.sync

import com.vamsi.worldcountriesinformation.core.database.entity.CountryEntity
import com.vamsi.worldcountriesinformation.core.database.entity.LanguageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FingerprintTest {

    private fun entity(code: String, population: Int = 1, lastUpdated: Long = 0L) = CountryEntity(
        threeLetterCode = code,
        twoLetterCode = code.take(2),
        name = "Name $code",
        capital = "Capital",
        region = "Region",
        population = population,
        callingCode = "+1",
        latitude = 1.0,
        longitude = 2.0,
        languages = listOf(LanguageEntity(name = "English")),
        currencies = emptyList(),
        lastUpdated = lastUpdated,
    )

    @Test
    fun `same data hashes the same regardless of order and write time`() {
        val a = listOf(entity("AAA", lastUpdated = 1L), entity("BBB", lastUpdated = 1L)).fingerprint()
        val b = listOf(entity("BBB", lastUpdated = 99L), entity("AAA", lastUpdated = 99L)).fingerprint()

        assertEquals(a, b)
    }

    @Test
    fun `a changed field changes the hash`() {
        val before = listOf(entity("AAA", population = 100)).fingerprint()
        val after = listOf(entity("AAA", population = 101)).fingerprint()

        assertNotEquals(before, after)
    }
}
