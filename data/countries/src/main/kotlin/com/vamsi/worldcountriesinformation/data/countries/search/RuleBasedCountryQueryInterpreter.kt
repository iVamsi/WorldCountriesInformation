package com.vamsi.worldcountriesinformation.data.countries.search

import com.vamsi.worldcountriesinformation.domain.search.CountryQueryInterpreter
import com.vamsi.worldcountriesinformation.domain.search.DirectionalRanking
import com.vamsi.worldcountriesinformation.domain.search.StructuredCountryQuery
import com.vamsi.worldcountriesinformation.domainmodel.Regions
import com.vamsi.worldcountriesinformation.domainmodel.SearchFilters
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder
import javax.inject.Inject

/**
 * Deterministic fallback interpreter for common country-search phrases.
 */
class RuleBasedCountryQueryInterpreter @Inject constructor() : CountryQueryInterpreter {

    override suspend fun interpret(query: String): StructuredCountryQuery {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return StructuredCountryQuery()

        val lowerCaseQuery = normalizedQuery.lowercase()
        val selectedRegions = extractRegions(lowerCaseQuery)
        val sortOrder = determineSortOrder(lowerCaseQuery)
        val directionalRanking = extractDirectionalRanking(lowerCaseQuery)
        val limit = determineLimit(
            normalizedQuery = lowerCaseQuery,
            sortOrder = sortOrder,
            directionalRanking = directionalRanking,
        )
        val textQuery = extractTextQuery(
            originalQuery = normalizedQuery,
            normalizedQuery = lowerCaseQuery,
            sortOrder = sortOrder,
            directionalRanking = directionalRanking,
        )

        return StructuredCountryQuery(
            textQuery = textQuery,
            filters = SearchFilters(
                selectedRegions = selectedRegions,
                sortOrder = sortOrder,
            ),
            limit = limit,
            directionalRanking = directionalRanking,
        )
    }

    private fun determineLimit(
        normalizedQuery: String,
        sortOrder: SortOrder,
        directionalRanking: DirectionalRanking?,
    ): Int? {
        if (directionalRanking != null) return 1

        val isAggregateQuery = sortOrder != SortOrder.NAME_ASC && (
            normalizedQuery.contains("highest") ||
                normalizedQuery.contains("largest") ||
                normalizedQuery.contains("biggest") ||
                normalizedQuery.contains("lowest") ||
                normalizedQuery.contains("smallest") ||
                normalizedQuery.contains("least") ||
                normalizedQuery.contains("most")
            )

        return if (isAggregateQuery) 1 else null
    }

    private fun determineSortOrder(normalizedQuery: String): SortOrder {
        val mentionsPopulation = normalizedQuery.contains("population") ||
            normalizedQuery.contains("populated")
        val mentionsArea = normalizedQuery.contains("area") ||
            normalizedQuery.contains("largest country") ||
            normalizedQuery.contains("smallest country")

        return when {
            mentionsPopulation && (
                normalizedQuery.contains("highest") ||
                    normalizedQuery.contains("largest") ||
                    normalizedQuery.contains("most")
                ) -> SortOrder.POPULATION_DESC
            mentionsPopulation && (
                normalizedQuery.contains("lowest") ||
                    normalizedQuery.contains("least") ||
                    normalizedQuery.contains("smallest")
                ) -> SortOrder.POPULATION_ASC
            mentionsArea && (
                normalizedQuery.contains("highest") ||
                    normalizedQuery.contains("largest") ||
                    normalizedQuery.contains("biggest")
                ) -> SortOrder.AREA_DESC
            mentionsArea && normalizedQuery.contains("smallest") -> SortOrder.AREA_ASC
            else -> SortOrder.NAME_ASC
        }
    }

    private fun extractDirectionalRanking(normalizedQuery: String): DirectionalRanking? {
        return when {
            normalizedQuery.contains("southernmost") ||
                normalizedQuery.contains("south most") -> DirectionalRanking.SOUTHERNMOST
            normalizedQuery.contains("northernmost") ||
                normalizedQuery.contains("north most") -> DirectionalRanking.NORTHERNMOST
            normalizedQuery.contains("easternmost") ||
                normalizedQuery.contains("east most") -> DirectionalRanking.EASTERNMOST
            normalizedQuery.contains("westernmost") ||
                normalizedQuery.contains("west most") -> DirectionalRanking.WESTERNMOST
            else -> null
        }
    }

    private fun extractRegions(normalizedQuery: String): Set<String> {
        val matchedAliases = buildSet {
            REGION_ALIASES.forEach { (alias, region) ->
                if (normalizedQuery.contains(alias)) add(region)
            }
        }
        if (matchedAliases.isNotEmpty()) return matchedAliases

        return Regions.ALL.filterTo(mutableSetOf()) { region ->
            normalizedQuery.contains(region.lowercase())
        }
    }

    private fun extractTextQuery(
        originalQuery: String,
        normalizedQuery: String,
        sortOrder: SortOrder,
        directionalRanking: DirectionalRanking?,
    ): String {
        val capitalPatterns = listOf(
            Regex("whose capital is (.+)", RegexOption.IGNORE_CASE),
            Regex("capital is (.+)", RegexOption.IGNORE_CASE),
            Regex("capital of (.+)", RegexOption.IGNORE_CASE),
            Regex("capital (.+)", RegexOption.IGNORE_CASE),
        )

        capitalPatterns.forEach { pattern ->
            val match = pattern.find(originalQuery)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }

        if (sortOrder != SortOrder.NAME_ASC || directionalRanking != null) {
            return ""
        }

        return normalizedQuery
            .replace(Regex("\\bcountries\\b|\\bcountry\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\bin\\b", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    companion object {
        private val REGION_ALIASES = mapOf(
            "south america" to Regions.AMERICAS,
            "north america" to Regions.AMERICAS,
            "central america" to Regions.AMERICAS,
        )
    }
}
