package com.vamsi.worldcountriesinformation.core.common

object Constants {
    // v3.1-compatible country data (mledoze/countries).
    // restcountries.com v3.1 now returns a deprecation envelope; this feed keeps the app working.
    const val BASE_URL = "https://raw.githubusercontent.com/mledoze/countries/master/"

    // API end points
    const val ALL = "countries.json"

    // World Bank "Population, total" (SP.POP.TOTL), most recent value per economy, CC BY 4.0.
    // The mledoze dataset has no population, so this is merged in by ISO-3 code.
    const val WORLD_BANK_POPULATION_URL =
        "https://api.worldbank.org/v2/country/all/indicator/SP.POP.TOTL?format=json&mrv=1&per_page=400"
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
    SAARC, // South Asian Association for Regional Cooperation
}
