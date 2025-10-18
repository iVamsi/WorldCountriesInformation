package com.vamsi.worldcountriesinformation.core.common

object Constants {
    // REST Countries API v3.1 (latest version)
    // https://restcountries.com/v3.1/all
    const val BASE_URL = "https://restcountries.com/v3.1/"

    // API end points (v3.1)
    const val ALL = "all"
    const val NAME = "name/{name}"
    const val FULL_NAME = "fullText"
    const val CODE = "alpha/{code}"
    const val CURRENCY = "currency/{currency}"
    const val LANGUAGE = "lang/{language}"
    const val CAPITAL_CITY = "capital/{capital}"
    const val REGION = "region/{region}"

    // url constants
    const val FIELDS = "fields"

    // Self hosted API (fallback)
    const val TEST_BASE_URL = "https://ivamsi.github.io/WorldCountriesAPI/api/"

    // Self hosted API end points
    const val TEST_ALL = "all/"

    // other constants
    const val EMPTY = ""
}

enum class RegionalBLoc {
    EU, // European Union
    EFTA, // European Free Trade Association
    CARICOM, // Caribbean Community
    PA, // Pacific Alliance
    AU, // African Union
    USAN, // Union of South American Nations
    EEU, // Eurasian Economic Union
    AL, // Arab League
    ASEAN, // Association of Southeast Asian Nations
    CAIS, // Central American Integration System
    CEFTA, // Central European Free Trade Agreement
    NAFTA, // North American Free Trade Agreement
    SAARC // South Asian Association for Regional Cooperation
}