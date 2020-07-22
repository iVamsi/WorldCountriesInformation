package com.vamsi.worldcountriesinformation.domain.countries

data class Country(
    val name: String,
    val capital: String,
    val languages: List<String>,
    val twoLetterCode: String,
    val threeLetterCode: String,
    val population: Int,
    val region: String,
    val currencies: List<Currency>,
    val callingCode: String
)