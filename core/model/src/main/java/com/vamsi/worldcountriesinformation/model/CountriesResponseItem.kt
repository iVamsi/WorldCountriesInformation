package com.vamsi.worldcountriesinformation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountriesResponseItem(
    @SerialName("alpha2Code")
    val alpha2Code: String? = null,
    @SerialName("alpha3Code")
    val alpha3Code: String? = null,
    @SerialName("altSpellings")
    val altSpellings: List<String>? = null,
    @SerialName("area")
    val area: Double? = null,
    @SerialName("borders")
    val borders: List<String>? = null,
    @SerialName("callingCodes")
    val callingCodes: List<String>? = null,
    @SerialName("capital")
    val capital: String? = null,
    @SerialName("cioc")
    val cioc: String? = null,
    @SerialName("currencies")
    val currencies: List<CurrencyResponseItem>? = null,
    @SerialName("demonym")
    val demonym: String? = null,
    @SerialName("flag")
    val flag: String? = null,
    @SerialName("gini")
    val gini: Double? = null,
    @SerialName("languages")
    val languages: List<LanguageResponseItem>? = null,
    @SerialName("latlng")
    val latlng: List<Double>? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("nativeName")
    val nativeName: String? = null,
    @SerialName("numericCode")
    val numericCode: String? = null,
    @SerialName("population")
    val population: Int? = null,
    @SerialName("region")
    val region: String? = null,
    @SerialName("regionalBlocs")
    val regionalBlocs: List<RegionalBlocResponseItem>? = null,
    @SerialName("subregion")
    val subregion: String? = null,
    @SerialName("timezones")
    val timezones: List<String>? = null,
    @SerialName("topLevelDomain")
    val topLevelDomain: List<String>? = null,
    @SerialName("translations")
    val translations: TranslationsResponseItem? = null,
)
