package com.vamsi.worldcountriesinformation.data.countries.mapper

import com.vamsi.worldcountriesinformation.core.common.Constants.EMPTY
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import com.vamsi.worldcountriesinformation.model.CountriesV3ResponseItem

/**
 * Mapper functions to convert REST Countries API v3.1 response models to domain models
 *
 * Key changes from v2 to v3.1:
 * - currencies: List -> Map<String, CurrencyV3>
 * - languages: List -> Map<String, String>
 * - name: String -> NameV3 object (with common, official, nativeName)
 * - capital: String -> List<String>
 * - callingCodes: List<String> -> IddV3 object
 */

/**
 * Convert list of v3.1 API responses to domain Country list
 */
fun List<CountriesV3ResponseItem>.toCountries(): List<Country> {
    return this.mapNotNull { it.toCountry() }
}

/**
 * Convert single v3.1 API response to domain Country
 */
fun CountriesV3ResponseItem.toCountry(): Country? {
    // Extract languages from Map to List
    val languages = this.languages?.map { (code, name) ->
        Language(
            name = name,
            nativeName = this.name?.nativeName?.get(code)?.common
        )
    } ?: emptyList()

    // Extract currencies from Map to List
    val currencies = this.currencies?.map { (code, currency) ->
        Currency(
            code = code,
            name = currency.name,
            symbol = currency.symbol
        )
    } ?: emptyList()

    // Build calling code from IDD (International Direct Dialing)
    val callingCode = buildCallingCode(this.idd?.root, this.idd?.suffixes)

    // Get capital (take first from list, or empty)
    val capital = this.capital?.firstOrNull() ?: EMPTY

    // Get latitude and longitude
    val (latitude, longitude) = this.latlng?.let {
        if (it.size >= 2) Pair(it[0], it[1]) else Pair(0.0, 0.0)
    } ?: Pair(0.0, 0.0)

    return Country(
        name = this.name?.common ?: EMPTY,
        capital = capital,
        languages = languages,
        twoLetterCode = this.cca2 ?: EMPTY,
        threeLetterCode = this.cca3 ?: EMPTY,
        population = this.population?.toInt() ?: 0,
        region = this.region ?: EMPTY,
        currencies = currencies,
        callingCode = callingCode,
        latitude = latitude,
        longitude = longitude
    )
}

/**
 * Build calling code from IDD root and suffixes
 * Example: root = "+9", suffixes = ["1", "2"] -> "+91" (first suffix is used)
 */
private fun buildCallingCode(root: String?, suffixes: List<String>?): String {
    if (root == null) return EMPTY
    val suffix = suffixes?.firstOrNull() ?: return root
    return "$root$suffix"
}
