package com.vamsi.worldcountriesinformation.data.mappers

import com.vamsi.worldcountriesinformation.domain.countries.Country
import com.vamsi.worldcountriesinformation.domain.countries.Currency
import com.vamsi.worldcountriesinformation.domain.countries.Language
import com.vamsi.worldcountriesinformation.model.CountriesResponseItem

fun List<CountriesResponseItem>.toCountries(): List<Country> {
    return this.map { countriesResponseItem ->
        val languages = countriesResponseItem.languages.map { Language(it.name, it.nativeName) }.toList()
        val currencies = countriesResponseItem.currencies.map { Currency(it.code, it.name, it.symbol) }.toList()
        Country(
            name = countriesResponseItem.name,
            capital = countriesResponseItem.capital,
            languages = languages,
            twoLetterCode = countriesResponseItem.alpha2Code,
            threeLetterCode = countriesResponseItem.alpha3Code,
            population = countriesResponseItem.population,
            region = countriesResponseItem.subregion,
            currencies = currencies,
            callingCode = countriesResponseItem.callingCodes.first()
        )
    }.toList()
}