package com.vamsi.worldcountriesinformation.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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
@JsonClass(generateAdapter = true)
data class CountriesV3ResponseItem(
    // Country codes
    @Json(name = "cca2")
    val cca2: String? = null, // Two-letter country code (ISO 3166-1 alpha-2)

    @Json(name = "cca3")
    val cca3: String? = null, // Three-letter country code (ISO 3166-1 alpha-3)

    @Json(name = "ccn3")
    val ccn3: String? = null, // Numeric country code

    // Names
    @Json(name = "name")
    val name: NameV3? = null,

    // Capital cities (can be multiple)
    @Json(name = "capital")
    val capital: List<String>? = null,

    // Region and location
    @Json(name = "region")
    val region: String? = null,

    @Json(name = "subregion")
    val subregion: String? = null,

    @Json(name = "continents")
    val continents: List<String>? = null,

    @Json(name = "latlng")
    val latlng: List<Double>? = null,

    // Population and demographics
    @Json(name = "population")
    val population: Long? = null,

    @Json(name = "area")
    val area: Double? = null,

    // Currencies (Map with currency code as key)
    @Json(name = "currencies")
    val currencies: Map<String, CurrencyV3>? = null,

    // Languages (Map with language code as key, language name as value)
    @Json(name = "languages")
    val languages: Map<String, String>? = null,

    // International Direct Dialing
    @Json(name = "idd")
    val idd: IddV3? = null,

    // Flags
    @Json(name = "flags")
    val flags: FlagsV3? = null,

    // Coat of Arms
    @Json(name = "coatOfArms")
    val coatOfArms: CoatOfArmsV3? = null,

    // Borders (country codes)
    @Json(name = "borders")
    val borders: List<String>? = null,

    // Timezones
    @Json(name = "timezones")
    val timezones: List<String>? = null,

    // Maps
    @Json(name = "maps")
    val maps: MapsV3? = null,

    // Translations
    @Json(name = "translations")
    val translations: Map<String, TranslationV3>? = null,

    // Additional fields
    @Json(name = "tld")
    val tld: List<String>? = null, // Top level domain

    @Json(name = "independent")
    val independent: Boolean? = null,

    @Json(name = "status")
    val status: String? = null,

    @Json(name = "unMember")
    val unMember: Boolean? = null,

    @Json(name = "landlocked")
    val landlocked: Boolean? = null,

    @Json(name = "fifa")
    val fifa: String? = null,

    @Json(name = "startOfWeek")
    val startOfWeek: String? = null,

    @Json(name = "capitalInfo")
    val capitalInfo: CapitalInfoV3? = null
)

@JsonClass(generateAdapter = true)
data class NameV3(
    @Json(name = "common")
    val common: String? = null,

    @Json(name = "official")
    val official: String? = null,

    @Json(name = "nativeName")
    val nativeName: Map<String, NativeNameV3>? = null
)

@JsonClass(generateAdapter = true)
data class NativeNameV3(
    @Json(name = "official")
    val official: String? = null,

    @Json(name = "common")
    val common: String? = null
)

@JsonClass(generateAdapter = true)
data class CurrencyV3(
    @Json(name = "name")
    val name: String? = null,

    @Json(name = "symbol")
    val symbol: String? = null
)

@JsonClass(generateAdapter = true)
data class IddV3(
    @Json(name = "root")
    val root: String? = null,

    @Json(name = "suffixes")
    val suffixes: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class FlagsV3(
    @Json(name = "png")
    val png: String? = null,

    @Json(name = "svg")
    val svg: String? = null,

    @Json(name = "alt")
    val alt: String? = null
)

@JsonClass(generateAdapter = true)
data class CoatOfArmsV3(
    @Json(name = "png")
    val png: String? = null,

    @Json(name = "svg")
    val svg: String? = null
)

@JsonClass(generateAdapter = true)
data class MapsV3(
    @Json(name = "googleMaps")
    val googleMaps: String? = null,

    @Json(name = "openStreetMaps")
    val openStreetMaps: String? = null
)

@JsonClass(generateAdapter = true)
data class TranslationV3(
    @Json(name = "official")
    val official: String? = null,

    @Json(name = "common")
    val common: String? = null
)

@JsonClass(generateAdapter = true)
data class CapitalInfoV3(
    @Json(name = "latlng")
    val latlng: List<Double>? = null
)
