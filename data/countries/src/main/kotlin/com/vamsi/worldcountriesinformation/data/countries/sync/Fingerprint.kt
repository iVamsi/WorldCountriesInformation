package com.vamsi.worldcountriesinformation.data.countries.sync

import com.vamsi.worldcountriesinformation.core.database.entity.CountryEntity
import java.security.MessageDigest

/**
 * SHA-256 over every stored field except `lastUpdated`, in a fixed order, so two fetches of the
 * same data hash the same regardless of list order or when they ran.
 */
fun List<CountryEntity>.fingerprint(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    sortedBy { it.threeLetterCode }.forEach { country ->
        val line = buildString {
            append(country.threeLetterCode).append('|')
            append(country.twoLetterCode).append('|')
            append(country.name).append('|')
            append(country.capital).append('|')
            append(country.region).append('|')
            append(country.population).append('|')
            append(country.callingCode).append('|')
            append(country.latitude).append('|')
            append(country.longitude).append('|')
            country.languages.forEach { append(it.name).append(',').append(it.nativeName).append(';') }
            append('|')
            country.currencies.forEach { append(it.code).append(',').append(it.name).append(',').append(it.symbol).append(';') }
            append('\n')
        }
        digest.update(line.toByteArray())
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}
