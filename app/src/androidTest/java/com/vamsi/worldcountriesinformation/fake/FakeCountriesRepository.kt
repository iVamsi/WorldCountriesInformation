package com.vamsi.worldcountriesinformation.fake

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.CountriesRepository
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of [CountriesRepository] for UI testing.
 *
 * Provides controllable behavior for testing different scenarios:
 * - Success with test data
 * - Error states
 * - Loading states
 * - Empty data
 */
class FakeCountriesRepository : CountriesRepository {

    private val countriesFlow = MutableStateFlow(TestData.countries)

    // Control flags for testing different scenarios
    var shouldReturnError = false
    var errorMessage = "Test error"
    var simulateDelay = false
    var delayMillis = 500L

    fun setCountries(countries: List<Country>) {
        countriesFlow.value = countries
    }

    fun reset() {
        countriesFlow.value = TestData.countries
        shouldReturnError = false
        errorMessage = "Test error"
        simulateDelay = false
        delayMillis = 500L
    }

    override fun getCountries(policy: CachePolicy): Flow<ApiResponse<List<Country>>> = flow {
        emit(ApiResponse.Loading)

        if (simulateDelay) {
            delay(delayMillis)
        }

        if (shouldReturnError) {
            emit(ApiResponse.Error(Exception(errorMessage)))
        } else {
            emit(ApiResponse.Success(countriesFlow.value))
        }
    }

    override fun getCountryByCode(code: String, policy: CachePolicy): Flow<ApiResponse<Country>> =
        flow {
            emit(ApiResponse.Loading)

            if (simulateDelay) {
                delay(delayMillis)
            }

            if (shouldReturnError) {
                emit(ApiResponse.Error(Exception(errorMessage)))
            } else {
                val country = countriesFlow.value.find {
                    it.threeLetterCode.equals(code, ignoreCase = true)
                }
                if (country != null) {
                    emit(ApiResponse.Success(country))
                } else {
                    emit(ApiResponse.Error(Exception("Country not found: $code")))
                }
            }
        }

    override fun getCountriesFlow(): Flow<List<Country>> = countriesFlow

    override fun searchCountries(query: String): Flow<List<Country>> =
        countriesFlow.map { countries ->
            if (query.isBlank()) {
                countries
            } else {
                countries.filter { country ->
                    country.name.contains(query, ignoreCase = true) ||
                            country.capital.contains(query, ignoreCase = true)
                }
            }
        }

    override fun getCountriesByRegion(region: String): Flow<List<Country>> =
        countriesFlow.map { countries ->
            countries.filter { it.region.equals(region, ignoreCase = true) }
        }

    override suspend fun forceRefresh(): Result<Unit> {
        if (simulateDelay) {
            delay(delayMillis)
        }

        return if (shouldReturnError) {
            Result.failure(Exception(errorMessage))
        } else {
            Result.success(Unit)
        }
    }
}

/**
 * Test data for UI testing.
 */
object TestData {
    val countries = listOf(
        Country(
            name = "United States",
            capital = "Washington, D.C.",
            languages = listOf(Language(name = "English")),
            twoLetterCode = "US",
            threeLetterCode = "USA",
            population = 331002651,
            region = "Americas",
            currencies = listOf(
                Currency(
                    code = "USD",
                    name = "United States dollar",
                    symbol = "$"
                )
            ),
            callingCode = "+1",
            latitude = 38.8951,
            longitude = -77.0364
        ),
        Country(
            name = "Canada",
            capital = "Ottawa",
            languages = listOf(Language(name = "English"), Language(name = "French")),
            twoLetterCode = "CA",
            threeLetterCode = "CAN",
            population = 38005238,
            region = "Americas",
            currencies = listOf(Currency(code = "CAD", name = "Canadian dollar", symbol = "$")),
            callingCode = "+1",
            latitude = 56.1304,
            longitude = -106.3468
        ),
        Country(
            name = "Japan",
            capital = "Tokyo",
            languages = listOf(Language(name = "Japanese")),
            twoLetterCode = "JP",
            threeLetterCode = "JPN",
            population = 125836021,
            region = "Asia",
            currencies = listOf(Currency(code = "JPY", name = "Japanese yen", symbol = "¥")),
            callingCode = "+81",
            latitude = 36.2048,
            longitude = 138.2529
        ),
        Country(
            name = "Germany",
            capital = "Berlin",
            languages = listOf(Language(name = "German")),
            twoLetterCode = "DE",
            threeLetterCode = "DEU",
            population = 83240525,
            region = "Europe",
            currencies = listOf(Currency(code = "EUR", name = "Euro", symbol = "€")),
            callingCode = "+49",
            latitude = 51.1657,
            longitude = 10.4515
        ),
        Country(
            name = "Australia",
            capital = "Canberra",
            languages = listOf(Language(name = "English")),
            twoLetterCode = "AU",
            threeLetterCode = "AUS",
            population = 25687041,
            region = "Oceania",
            currencies = listOf(Currency(code = "AUD", name = "Australian dollar", symbol = "$")),
            callingCode = "+61",
            latitude = -25.2744,
            longitude = 133.7751
        ),
        Country(
            name = "Brazil",
            capital = "Brasilia",
            languages = listOf(Language(name = "Portuguese")),
            twoLetterCode = "BR",
            threeLetterCode = "BRA",
            population = 212559409,
            region = "Americas",
            currencies = listOf(Currency(code = "BRL", name = "Brazilian real", symbol = "R$")),
            callingCode = "+55",
            latitude = -14.235,
            longitude = -51.9253
        ),
        Country(
            name = "India",
            capital = "New Delhi",
            languages = listOf(Language(name = "Hindi"), Language(name = "English")),
            twoLetterCode = "IN",
            threeLetterCode = "IND",
            population = 1380004385,
            region = "Asia",
            currencies = listOf(Currency(code = "INR", name = "Indian rupee", symbol = "₹")),
            callingCode = "+91",
            latitude = 20.5937,
            longitude = 78.9629
        ),
        Country(
            name = "South Africa",
            capital = "Pretoria",
            languages = listOf(Language(name = "Afrikaans"), Language(name = "English")),
            twoLetterCode = "ZA",
            threeLetterCode = "ZAF",
            population = 59308690,
            region = "Africa",
            currencies = listOf(Currency(code = "ZAR", name = "South African rand", symbol = "R")),
            callingCode = "+27",
            latitude = -30.5595,
            longitude = 22.9375
        )
    )

    val singleCountry = countries.first()

    val emptyCountries = emptyList<Country>()
}
