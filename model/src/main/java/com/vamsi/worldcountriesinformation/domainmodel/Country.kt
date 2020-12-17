package com.vamsi.worldcountriesinformation.domainmodel

import java.io.Serializable

data class Country(
    val name: String,
    val capital: String,
    val languages: List<Language>,
    val twoLetterCode: String,
    val threeLetterCode: String,
    val population: Int,
    val region: String,
    val currencies: List<Currency>,
    val callingCode: String,
    val latitude: Double,
    val longitude: Double
): Serializable