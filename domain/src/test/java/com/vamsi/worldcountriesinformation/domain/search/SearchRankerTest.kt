package com.vamsi.worldcountriesinformation.domain.search

import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchRankerTest {

    private lateinit var ranker: SearchRanker

    private val unitedStates = country("United States", "Washington D.C.", "US", "USA")
    private val unitedKingdom = country("United Kingdom", "London", "GB", "GBR")
    private val unitedArab = country("United Arab Emirates", "Abu Dhabi", "AE", "ARE")
    private val canada = country("Canada", "Ottawa", "CA", "CAN")
    private val ukraine = country("Ukraine", "Kyiv", "UA", "UKR")
    private val australia = country("Australia", "Canberra", "AU", "AUS")

    private val all = listOf(unitedStates, unitedKingdom, unitedArab, canada, ukraine, australia)

    @Before
    fun setup() {
        ranker = SearchRanker()
    }

    @Test
    fun `blank query returns alphabetical order`() {
        val result = ranker.rank("", all)
        assertEquals(
            listOf("Australia", "Canada", "Ukraine", "United Arab Emirates", "United Kingdom", "United States"),
            result.map { it.name },
        )
    }

    @Test
    fun `blank query promotes favorites then recents`() {
        val result = ranker.rank(
            query = "",
            countries = all,
            recentCodes = setOf("CAN"),
            favoriteCodes = setOf("UKR"),
        )
        assertEquals("Ukraine", result.first().name)
        assertEquals("Canada", result[1].name)
    }

    @Test
    fun `prefix on name ranks ahead of capital substring`() {
        // Australia's capital "Canberra" contains "can"; Canada starts with "can".
        val result = ranker.rank("can", all)
        assertEquals(canada, result.first())
        assertTrue(result.indexOf(canada) < result.indexOf(australia))
    }

    @Test
    fun `exact name match wins`() {
        val result = ranker.rank("canada", all)
        assertEquals(canada, result.first())
    }

    @Test
    fun `recent boost reorders ties`() {
        val withoutBoost = ranker.rank("united", all)
        val withBoost = ranker.rank(
            query = "united",
            countries = all,
            recentCodes = setOf("USA"),
        )
        // Without boost: alphabetical tiebreak among the three "United*" prefix matches.
        assertEquals(unitedArab, withoutBoost.first())
        // With recent boost: USA jumps ahead of the alphabetical winner.
        assertEquals(unitedStates, withBoost.first())
    }

    @Test
    fun `favorite boost outranks recent`() {
        val result = ranker.rank(
            query = "united",
            countries = all,
            recentCodes = setOf("USA"),
            favoriteCodes = setOf("GBR"),
        )
        assertEquals(unitedKingdom, result.first())
    }

    @Test
    fun `fuzzy match catches typos`() {
        val result = ranker.rank("canda", all)
        assertEquals(canada, result.first())
    }

    @Test
    fun `non-matching candidates are dropped`() {
        val result = ranker.rank("zzz", all)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `three letter code match returns country`() {
        val result = ranker.rank("usa", all)
        assertEquals(unitedStates, result.first())
    }

    private fun country(
        name: String,
        capital: String,
        twoLetter: String,
        threeLetter: String,
    ) = CountrySummary(
        name = name,
        capital = capital,
        twoLetterCode = twoLetter,
        threeLetterCode = threeLetter,
        population = 1,
        region = "Test",
        latitude = 0.0,
        longitude = 0.0,
    )
}
