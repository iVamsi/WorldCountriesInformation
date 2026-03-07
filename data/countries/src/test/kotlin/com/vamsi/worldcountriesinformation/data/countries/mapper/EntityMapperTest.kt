package com.vamsi.worldcountriesinformation.data.countries.mapper

import com.vamsi.worldcountriesinformation.core.database.entity.CountryEntity
import com.vamsi.worldcountriesinformation.domainmodel.Country
import org.junit.Assert.assertEquals
import org.junit.Test

class EntityMapperTest {

    @Test
    fun `toEntity and toDomain - given structured search fields - preserve area and subregion`() {
        val country = Country(
            name = "India",
            capital = "New Delhi",
            languages = emptyList(),
            twoLetterCode = "IN",
            threeLetterCode = "IND",
            population = 1_428_627_663,
            region = "Asia",
            subregion = "Southern Asia",
            area = 3_287_263.0,
            currencies = emptyList(),
            callingCode = "+91",
            latitude = 20.0,
            longitude = 77.0,
        )

        val entity = country.toEntity()
        val mappedCountry = entity.toDomain()

        assertEquals("Southern Asia", entity.subregion)
        assertEquals(3_287_263.0, entity.area, 0.0)
        assertEquals("Southern Asia", mappedCountry.subregion)
        assertEquals(3_287_263.0, mappedCountry.area, 0.0)
    }
}
