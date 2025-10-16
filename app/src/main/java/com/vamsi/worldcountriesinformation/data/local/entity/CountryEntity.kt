package com.vamsi.worldcountriesinformation.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.vamsi.worldcountriesinformation.data.local.converter.Converters

/**
 * Room database entity for storing country information locally
 * Matches the domain model structure for simplicity
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

data class LanguageEntity(
    val name: String?,
    val nativeName: String?
)

data class CurrencyEntity(
    val code: String?,
    val name: String?,
    val symbol: String?
)
