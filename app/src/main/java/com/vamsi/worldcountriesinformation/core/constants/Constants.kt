package com.vamsi.worldcountriesinformation.core.constants

object Constants {
    // https://restcountries.eu/rest/v2/all
    const val BASE_URL = "https://restcountries.eu/rest/v2/"

    // API end points
    const val ALL = "all"
    const val NAME = "name/{name}"
    const val FULL_NAME = "fullText"
    const val CODE = "alpha/{code}"
    const val CURRENCY = "currency/{currency}"
    const val LANGUAGE = "lang/{et}"
    const val CAPITAL_CITY = "capital/{capital}"
    const val CALLING_CODE = "callingcode/{callingcode}"
    const val REGION = "region/{region}"
    const val REGIONAL_BLOC = "regionalbloc/{regionalbloc}"

    // url constants
    const val FIELDS = "fields"
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