package com.vamsi.worldcountriesinformation.data.mappers

import com.vamsi.worldcountriesinformation.data.local.entity.CountryEntity
import com.vamsi.worldcountriesinformation.data.local.entity.CurrencyEntity
import com.vamsi.worldcountriesinformation.data.local.entity.LanguageEntity
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language

/**
 * Extension functions to map between Room entities and domain models
 */

// Entity to Domain
fun CountryEntity.toDomain(): Country {
    return Country(
        name = name,
        capital = capital,
        languages = languages.map { it.toDomain() },
        twoLetterCode = twoLetterCode,
        threeLetterCode = threeLetterCode,
        population = population,
        region = region,
        currencies = currencies.map { it.toDomain() },
        callingCode = callingCode,
        latitude = latitude,
        longitude = longitude
    )
}

fun LanguageEntity.toDomain(): Language {
    return Language(
        name = name,
        nativeName = nativeName
    )
}

fun CurrencyEntity.toDomain(): Currency {
    return Currency(
        code = code,
        name = name,
        symbol = symbol
    )
}

fun List<CountryEntity>.toDomainList(): List<Country> {
    return this.map { it.toDomain() }
}

// Domain to Entity
fun Country.toEntity(): CountryEntity {
    return CountryEntity(
        name = name,
        capital = capital,
        languages = languages.map { it.toEntity() },
        twoLetterCode = twoLetterCode,
        threeLetterCode = threeLetterCode,
        population = population,
        region = region,
        currencies = currencies.map { it.toEntity() },
        callingCode = callingCode,
        latitude = latitude,
        longitude = longitude,
        lastUpdated = System.currentTimeMillis()
    )
}

fun Language.toEntity(): LanguageEntity {
    return LanguageEntity(
        name = name,
        nativeName = nativeName
    )
}

fun Currency.toEntity(): CurrencyEntity {
    return CurrencyEntity(
        code = code,
        name = name,
        symbol = symbol
    )
}

fun List<Country>.toEntityList(): List<CountryEntity> {
    return this.map { it.toEntity() }
}
