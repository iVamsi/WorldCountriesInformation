package com.vamsi.worldcountriesinformation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * REST Countries API v3.1 Response Model
 *
 * This model represents the response structure from REST Countries API v3.1
 * https://restcountries.com/v3.1/all
 *
 * Key differences from v2:
 * - currencies is now a Map<String, CurrencyV3> instead of List
 * - languages is now a Map<String, String> instead of List
 * - name is now a complex object with common, official, and nativeName
 * - capital is now a List<String> instead of String
 * - idd contains calling code information
 * - flags includes png, svg, and alt text
 */
@Serializable
data class CountriesV3ResponseItem(
    // Country codes
    @SerialName("cca2")
    val cca2: String? = null, // Two-letter country code (ISO 3166-1 alpha-2)

    @SerialName("cca3")
    val cca3: String? = null, // Three-letter country code (ISO 3166-1 alpha-3)

    @SerialName("ccn3")
    val ccn3: String? = null, // Numeric country code

    // Names
    @SerialName("name")
    val name: NameV3? = null,

    // Capital cities (can be multiple)
    @SerialName("capital")
    val capital: List<String>? = null,

    // Region and location
    @SerialName("region")
    val region: String? = null,

    @SerialName("subregion")
    val subregion: String? = null,

    @SerialName("continents")
    val continents: List<String>? = null,

    @SerialName("latlng")
    val latlng: List<Double>? = null,

    // Population and demographics
    @SerialName("population")
    val population: Long? = null,

    @SerialName("area")
    val area: Double? = null,

    // Currencies (Map with currency code as key)
    @SerialName("currencies")
    val currencies: Map<String, CurrencyV3>? = null,

    // Languages (Map with language code as key, language name as value)
    @SerialName("languages")
    val languages: Map<String, String>? = null,

    // International Direct Dialing
    @SerialName("idd")
    val idd: IddV3? = null,

    // Flags
    @SerialName("flags")
    val flags: FlagsV3? = null,

    // Coat of Arms
    @SerialName("coatOfArms")
    val coatOfArms: CoatOfArmsV3? = null,

    // Borders (country codes)
    @SerialName("borders")
    val borders: List<String>? = null,

    // Timezones
    @SerialName("timezones")
    val timezones: List<String>? = null,

    // Maps
    @SerialName("maps")
    val maps: MapsV3? = null,

    // Translations
    @SerialName("translations")
    val translations: Map<String, TranslationV3>? = null,

    // Additional fields
    @SerialName("tld")
    val tld: List<String>? = null, // Top level domain

    @SerialName("independent")
    val independent: Boolean? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("unMember")
    val unMember: Boolean? = null,

    @SerialName("landlocked")
    val landlocked: Boolean? = null,

    @SerialName("fifa")
    val fifa: String? = null,

    @SerialName("startOfWeek")
    val startOfWeek: String? = null,

    @SerialName("capitalInfo")
    val capitalInfo: CapitalInfoV3? = null,
)

@Serializable
data class NameV3(
    @SerialName("common")
    val common: String? = null,

    @SerialName("official")
    val official: String? = null,

    @SerialName("nativeName")
    val nativeName: Map<String, NativeNameV3>? = null,
)

@Serializable
data class NativeNameV3(
    @SerialName("official")
    val official: String? = null,

    @SerialName("common")
    val common: String? = null,
)

@Serializable
data class CurrencyV3(
    @SerialName("name")
    val name: String? = null,

    @SerialName("symbol")
    val symbol: String? = null,
)

@Serializable
data class IddV3(
    @SerialName("root")
    val root: String? = null,

    @SerialName("suffixes")
    val suffixes: List<String>? = null,
)

@Serializable
data class FlagsV3(
    @SerialName("png")
    val png: String? = null,

    @SerialName("svg")
    val svg: String? = null,

    @SerialName("alt")
    val alt: String? = null,
)

@Serializable
data class CoatOfArmsV3(
    @SerialName("png")
    val png: String? = null,

    @SerialName("svg")
    val svg: String? = null,
)

@Serializable
data class MapsV3(
    @SerialName("googleMaps")
    val googleMaps: String? = null,

    @SerialName("openStreetMaps")
    val openStreetMaps: String? = null,
)

@Serializable
data class TranslationV3(
    @SerialName("official")
    val official: String? = null,

    @SerialName("common")
    val common: String? = null,
)

@Serializable
data class CapitalInfoV3(
    @SerialName("latlng")
    val latlng: List<Double>? = null,
)
