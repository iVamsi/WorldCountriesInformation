package com.vamsi.worldcountriesinformation.data.countries.mapper

import com.vamsi.worldcountriesinformation.model.CountriesV3ResponseItem
import com.vamsi.worldcountriesinformation.model.NameV3
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiV3ResponseMapperTest {

    @Test
    fun `toCountry - given area and subregion - maps structured search fields`() {
        val country = CountriesV3ResponseItem(
            cca2 = "IN",
            cca3 = "IND",
            name = NameV3(common = "India"),
            capital = listOf("New Delhi"),
            region = "Asia",
            subregion = "Southern Asia",
            population = 1_428_627_663,
            area = 3_287_263.0,
        ).toCountry()

        assertEquals("Southern Asia", country.subregion)
        assertEquals(3_287_263.0, country.area, 0.0)
    }
}
