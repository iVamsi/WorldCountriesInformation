package com.vamsi.worldcountriesinformation.domain.search

/**
 * Converts a raw user query into a deterministic structured query.
 */
fun interface CountryQueryInterpreter {
    suspend fun interpret(query: String): StructuredCountryQuery
}
