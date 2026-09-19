package com.vamsi.worldcountriesinformation.data.countries.mapper

import com.vamsi.worldcountriesinformation.domainmodel.Country
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * Reads a World Bank indicator response (`[metadata, rows]`) into population by ISO-3 code.
 * Rows without a value, and aggregates too large for an Int (world, regions), are skipped.
 */
fun JsonArray.toPopulationByIso3(): Map<String, Int> {
    val rows = getOrNull(1) as? JsonArray ?: return emptyMap()
    return buildMap {
        rows.forEach { row ->
            val obj = row.jsonObject
            val iso3 = obj["countryiso3code"]?.jsonPrimitive?.content?.takeIf { it.length == ISO3_LENGTH } ?: return@forEach
            val value = obj["value"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.longOrNull ?: return@forEach
            if (value in 1..Int.MAX_VALUE) put(iso3, value.toInt())
        }
    }
}

/** Applies World Bank population where known; countries the World Bank does not track keep 0. */
fun List<Country>.withPopulation(populationByIso3: Map<String, Int>): List<Country> {
    if (populationByIso3.isEmpty()) return this
    return map { it.copy(population = populationByIso3[it.threeLetterCode] ?: it.population) }
}

private const val ISO3_LENGTH = 3
