package com.vamsi.worldcountriesinformation.data.countries.mapper

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import org.junit.Assert.assertEquals
import org.junit.Test

class PopulationMapperTest {

    @Test
    fun `parses latest value per ISO-3 code and skips nulls and aggregates`() {
        val body = """
            [
              {"page":1,"pages":1,"per_page":400,"total":4},
              [
                {"countryiso3code":"AUS","date":"2025","value":27614411},
                {"countryiso3code":"ATA","date":"2025","value":null},
                {"countryiso3code":"WLD","date":"2025","value":8200000000},
                {"countryiso3code":"","date":"2025","value":100}
              ]
            ]
        """.trimIndent()

        val result = Json.parseToJsonElement(body).jsonArray.toPopulationByIso3()

        assertEquals(mapOf("AUS" to 27_614_411), result)
    }

    @Test
    fun `returns empty map when rows are missing`() {
        assertEquals(emptyMap<String, Int>(), Json.parseToJsonElement("""[{"page":1}]""").jsonArray.toPopulationByIso3())
    }
}
