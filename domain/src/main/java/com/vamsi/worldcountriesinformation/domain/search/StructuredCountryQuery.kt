package com.vamsi.worldcountriesinformation.domain.search

import com.vamsi.worldcountriesinformation.domainmodel.SearchFilters

enum class DirectionalRanking {
    NORTHERNMOST,
    SOUTHERNMOST,
    EASTERNMOST,
    WESTERNMOST,
}

/**
 * Structured representation of a free-form country search request.
 *
 * Natural-language interpreters should map user input into this object so the
 * final search can be executed deterministically against local data.
 */
data class StructuredCountryQuery(
    val textQuery: String = "",
    val filters: SearchFilters = SearchFilters(),
    val limit: Int? = null,
    val directionalRanking: DirectionalRanking? = null,
)
