package com.vamsi.worldcountriesinformation.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.vamsi.worldcountriesinformation.core.database.converter.Converters
import kotlinx.serialization.Serializable

/**
 * Room database entity for storing country information locally
 */
@Entity(tableName = "countries")
@TypeConverters(Converters::class)
data class CountryEntity(
    @PrimaryKey
    val threeLetterCode: String,
    val twoLetterCode: String,
    val name: String,
    val capital: String,
    val region: String,
    val population: Int,
    val callingCode: String,
    val latitude: Double,
    val longitude: Double,
    val languages: List<LanguageEntity>,
    val currencies: List<CurrencyEntity>,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Embedded entity for language information.
 * 
 * Marked as @Serializable for use with kotlinx.serialization in Room type converters.
 * This provides compile-time safety and better performance compared to reflection-based JSON parsing.
 */
@Serializable
data class LanguageEntity(
    val name: String? = null,
    val nativeName: String? = null
)

/**
 * Embedded entity for currency information.
 * 
 * Marked as @Serializable for use with kotlinx.serialization in Room type converters.
 * This provides compile-time safety and better performance compared to reflection-based JSON parsing.
 */
@Serializable
data class CurrencyEntity(
    val code: String? = null,
    val name: String? = null,
    val symbol: String? = null
)
