package com.vamsi.worldcountriesinformation.domain.search

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.CountriesRepository
import com.vamsi.worldcountriesinformation.domain.countries.FilteredSearchCountriesUseCase
import com.vamsi.worldcountriesinformation.domain.countries.SearchCountriesUseCase
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.SearchFilters
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ExecuteStructuredCountryQueryUseCaseTest {

    private lateinit var useCase: ExecuteStructuredCountryQueryUseCase

    private val countries = listOf(
        createCountry(
            name = "India",
            capital = "New Delhi",
            region = "Asia",
            subregion = "Southern Asia",
            population = 1_428_627_663,
            area = 3_287_263.0,
            latitude = 20.0,
            longitude = 77.0,
        ),
        createCountry(
            name = "Japan",
            capital = "Tokyo",
            region = "Asia",
            subregion = "Eastern Asia",
            population = 123_294_513,
            area = 377_975.0,
            latitude = 36.0,
            longitude = 138.0,
        ),
        createCountry(
            name = "France",
            capital = "Paris",
            region = "Europe",
            subregion = "Western Europe",
            population = 68_400_000,
            area = 551_695.0,
            latitude = 46.0,
            longitude = 2.0,
        ),
        createCountry(
            name = "Argentina",
            capital = "Buenos Aires",
            region = "Americas",
            subregion = "South America",
            population = 45_696_159,
            area = 2_780_400.0,
            latitude = -38.4,
            longitude = -63.6,
        ),
        createCountry(
            name = "Chile",
            capital = "Santiago",
            region = "Americas",
            subregion = "South America",
            population = 19_629_590,
            area = 756_102.0,
            latitude = -35.7,
            longitude = -71.5,
        ),
        createCountry(
            name = "Brazil",
            capital = "Brasilia",
            region = "Americas",
            subregion = "South America",
            population = 203_062_512,
            area = 8_515_767.0,
            latitude = -14.2,
            longitude = -51.9,
        ),
    )

    @Before
    fun setup() {
        val repository = FakeCountriesRepository(countries = countries)
        val filteredSearchCountriesUseCase = FilteredSearchCountriesUseCase(
            searchCountriesUseCase = SearchCountriesUseCase(repository)
        )
        useCase = ExecuteStructuredCountryQueryUseCase(
            countriesRepository = repository,
            filteredSearchCountriesUseCase = filteredSearchCountriesUseCase,
        )
    }

    @Test
    fun `invoke - given highest population query - returns top ranked country`() = runTest {
        val query = StructuredCountryQuery(
            filters = SearchFilters(sortOrder = SortOrder.POPULATION_DESC),
            limit = 1,
        )

        val result = useCase(query).toList().single()

        assertEquals(listOf("India"), result.map { it.name })
    }

    @Test
    fun `invoke - given capital text query - matches country by capital`() = runTest {
        val query = StructuredCountryQuery(textQuery = "tokyo")

        val result = useCase(query).toList().single()

        assertEquals(listOf("Japan"), result.map { it.name })
    }

    @Test
    fun `invoke - given southernmost americas query - returns country with lowest latitude`() = runTest {
        val query = StructuredCountryQuery(
            filters = SearchFilters(selectedRegions = setOf("Americas")),
            directionalRanking = DirectionalRanking.SOUTHERNMOST,
            limit = 1,
        )

        val result = useCase(query).toList().single()

        assertEquals(listOf("Argentina"), result.map { it.name })
    }

    @Test
    fun `invoke - given easternmost query - returns country with highest longitude`() = runTest {
        val query = StructuredCountryQuery(
            directionalRanking = DirectionalRanking.EASTERNMOST,
            limit = 1,
        )

        val result = useCase(query).toList().single()

        assertEquals(listOf("Japan"), result.map { it.name })
    }

    private fun createCountry(
        name: String,
        capital: String,
        region: String,
        subregion: String,
        population: Int,
        area: Double,
        latitude: Double,
        longitude: Double,
    ) = Country(
        name = name,
        capital = capital,
        languages = emptyList(),
        twoLetterCode = name.take(2).uppercase(),
        threeLetterCode = name.take(3).uppercase(),
        population = population,
        region = region,
        subregion = subregion,
        area = area,
        currencies = emptyList(),
        callingCode = "+1",
        latitude = latitude,
        longitude = longitude,
    )

    private class FakeCountriesRepository(
        private val countries: List<Country>,
    ) : CountriesRepository {
        override fun getCountries(policy: CachePolicy): Flow<ApiResponse<List<Country>>> = flowOf(
            ApiResponse.Success(countries)
        )

        override fun getCountryByCode(
            code: String,
            policy: CachePolicy,
        ): Flow<ApiResponse<Country>> = throw UnsupportedOperationException("Unused in test")

        override fun getCountriesFlow(): Flow<List<Country>> = flowOf(countries)

        override fun searchCountries(query: String): Flow<List<Country>> =
            throw UnsupportedOperationException("Unused in test")

        override fun getCountriesByRegion(region: String): Flow<List<Country>> =
            throw UnsupportedOperationException("Unused in test")

        override suspend fun forceRefresh(): Result<Unit> =
            throw UnsupportedOperationException("Unused in test")
    }
}
