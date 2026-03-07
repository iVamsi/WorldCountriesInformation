package com.vamsi.worldcountriesinformation.data.countries.search

import com.vamsi.worldcountriesinformation.domain.search.DirectionalRanking
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleBasedCountryQueryInterpreterTest {

    private val interpreter = RuleBasedCountryQueryInterpreter()

    @Test
    fun `interpret - highest population query - maps to top population request`() = runTest {
        val result = interpreter.interpret("country with highest population")

        assertEquals(SortOrder.POPULATION_DESC, result.filters.sortOrder)
        assertEquals(1, result.limit)
    }

    @Test
    fun `interpret - smallest country in europe - maps area sort and region filter`() = runTest {
        val result = interpreter.interpret("smallest country in Europe")

        assertEquals(SortOrder.AREA_ASC, result.filters.sortOrder)
        assertEquals(setOf("Europe"), result.filters.selectedRegions)
        assertEquals(1, result.limit)
    }

    @Test
    fun `interpret - south most country in south america - maps directional ranking and americas filter`() = runTest {
        val result = interpreter.interpret("south most country in south america")

        assertEquals(DirectionalRanking.SOUTHERNMOST, result.directionalRanking)
        assertEquals(setOf("Americas"), result.filters.selectedRegions)
        assertEquals(1, result.limit)
    }
}
