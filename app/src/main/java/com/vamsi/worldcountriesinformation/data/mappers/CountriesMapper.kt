package com.vamsi.worldcountriesinformation.data.mappers

import com.vamsi.worldcountriesinformation.core.constants.Constants.EMPTY
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import com.vamsi.worldcountriesinformation.model.CountriesResponseItem

fun List<CountriesResponseItem>.toCountries(): List<Country> {
    return this.map { countriesResponseItem ->
        val languages = countriesResponseItem.languages?.map { Language(it.name, it.nativeName) }?.toList()
        val currencies = countriesResponseItem.currencies?.map { Currency(it.code, it.name, it.symbol) }?.toList()
        Country(
            name = countriesResponseItem.name ?: EMPTY,
            capital = countriesResponseItem.capital ?: EMPTY,
            languages = languages ?: emptyList(),
            twoLetterCode = countriesResponseItem.alpha2Code ?: EMPTY,
            threeLetterCode = countriesResponseItem.alpha3Code ?: EMPTY,
            population = countriesResponseItem.population ?: 0,
            region = countriesResponseItem.subregion ?: EMPTY,
            currencies = currencies ?: emptyList(),
            callingCode = countriesResponseItem.callingCodes?.first() ?: EMPTY,
            latitude = countriesResponseItem.latlng?.let {
                if (it.isNotEmpty() && it.size == 2) {
                    it[0]
                } else {
                    0.0
                }
            } ?: 0.0,
            longitude = countriesResponseItem.latlng?.let {
                if (it.isNotEmpty() && it.size == 2) {
                    it[1]
                } else {
                    0.0
                }
            } ?: 0.0
        )
    }.toList()
}