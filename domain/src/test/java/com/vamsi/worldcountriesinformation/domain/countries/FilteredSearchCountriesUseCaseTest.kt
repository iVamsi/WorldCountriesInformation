package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.SearchFilters
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FilteredSearchCountriesUseCaseTest {

    private lateinit var useCase: FilteredSearchCountriesUseCase

    private val countries = listOf(
        createCountry(
            name = "India",
            region = "Asia",
            subregion = "Southern Asia",
            area = 3_287_263.0,
            population = 1_428_627_663,
        ),
        createCountry(
            name = "Japan",
            region = "Asia",
            subregion = "Eastern Asia",
            area = 377_975.0,
            population = 123_294_513,
        ),
        createCountry(
            name = "France",
            region = "Europe",
            subregion = "Western Europe",
            area = 551_695.0,
            population = 68_400_000,
        ),
    )

    @Before
    fun setup() {
        useCase = FilteredSearchCountriesUseCase(searchCountriesUseCase = mockk())
    }

    @Test
    fun `applyFiltersAndSort - given subregion filters - returns matching countries only`() {
        val filters = SearchFilters(selectedSubregions = setOf("Southern Asia"))

        val result = useCase.applyFiltersAndSort(countries, filters)

        assertEquals(listOf("India"), result.map { it.name })
    }

    @Test
    fun `applyFiltersAndSort - given area descending sort - orders by largest area first`() {
        val filters = SearchFilters(sortOrder = SortOrder.AREA_DESC)

        val result = useCase.applyFiltersAndSort(countries, filters)

        assertEquals(listOf("India", "France", "Japan"), result.map { it.name })
    }

    private fun createCountry(
        name: String,
        region: String,
        subregion: String,
        area: Double,
        population: Int,
    ) = Country(
        name = name,
        capital = "$name City",
        languages = emptyList(),
        twoLetterCode = name.take(2).uppercase(),
        threeLetterCode = name.take(3).uppercase(),
        population = population,
        region = region,
        subregion = subregion,
        area = area,
        currencies = emptyList(),
        callingCode = "+1",
        latitude = 0.0,
        longitude = 0.0,
    )
}
