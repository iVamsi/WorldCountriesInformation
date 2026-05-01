package com.vamsi.worldcountriesinformation.domain.search

import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import javax.inject.Inject
import kotlin.math.max

/**
 * Ranks countries against a search query using a layered scoring strategy:
 *
 * 1. Exact name match
 * 2. Prefix match on name or capital
 * 3. Substring match on name, capital, or three-letter code
 * 4. Fuzzy match (Jaro-Winkler) above a threshold
 * 5. Boosts for recently viewed and favorited countries
 *
 * Pure Kotlin so it is fully unit-testable and free of Android deps.
 */
class SearchRanker @Inject constructor() {

    fun rank(
        query: String,
        countries: List<CountrySummary>,
        recentCodes: Set<String> = emptySet(),
        favoriteCodes: Set<String> = emptySet(),
    ): List<CountrySummary> {
        if (countries.isEmpty()) return emptyList()
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) {
            return countries.sortedWith(
                compareByDescending<CountrySummary> { it.threeLetterCode in favoriteCodes }
                    .thenByDescending { it.threeLetterCode in recentCodes }
                    .thenBy { it.name },
            )
        }
        return countries
            .map { it to scoreCountry(it, normalizedQuery, recentCodes, favoriteCodes) }
            .filter { (_, score) -> score > 0.0 }
            .sortedWith(
                compareByDescending<Pair<CountrySummary, Double>> { it.second }
                    .thenBy { it.first.name },
            )
            .map { it.first }
    }

    private fun scoreCountry(
        country: CountrySummary,
        query: String,
        recentCodes: Set<String>,
        favoriteCodes: Set<String>,
    ): Double {
        val name = country.name.lowercase()
        val capital = country.capital.lowercase()
        val code3 = country.threeLetterCode.lowercase()
        val code2 = country.twoLetterCode.lowercase()

        var score = when {
            name == query -> EXACT_MATCH
            code2 == query || code3 == query -> EXACT_MATCH - 1.0
            name.startsWith(query) -> PREFIX_NAME
            capital.startsWith(query) -> PREFIX_CAPITAL
            name.contains(query) -> SUBSTRING_NAME
            capital.contains(query) -> SUBSTRING_CAPITAL
            code3.contains(query) -> SUBSTRING_CODE
            else -> {
                val jw = max(jaroWinkler(name, query), jaroWinkler(capital, query))
                if (jw >= FUZZY_THRESHOLD) FUZZY_BASE * jw else 0.0
            }
        }

        if (score > 0.0) {
            if (country.threeLetterCode in recentCodes) score += RECENT_BOOST
            if (country.threeLetterCode in favoriteCodes) score += FAVORITE_BOOST
        }
        return score
    }

    /**
     * Jaro-Winkler similarity in [0.0, 1.0]. Tuned for short strings such as
     * country names, with the standard prefix scaling factor (0.1, capped at 4).
     */
    internal fun jaroWinkler(s1: String, s2: String): Double {
        if (s1.isEmpty() || s2.isEmpty()) return 0.0
        if (s1 == s2) return 1.0

        val matchDistance = max(s1.length, s2.length) / 2 - 1
        val s1Matches = BooleanArray(s1.length)
        val s2Matches = BooleanArray(s2.length)
        var matches = 0
        for (i in s1.indices) {
            val start = maxOf(0, i - matchDistance)
            val end = minOf(i + matchDistance + 1, s2.length)
            for (j in start until end) {
                if (s2Matches[j]) continue
                if (s1[i] != s2[j]) continue
                s1Matches[i] = true
                s2Matches[j] = true
                matches++
                break
            }
        }
        if (matches == 0) return 0.0

        var transpositions = 0
        var k = 0
        for (i in s1.indices) {
            if (!s1Matches[i]) continue
            while (!s2Matches[k]) k++
            if (s1[i] != s2[k]) transpositions++
            k++
        }
        transpositions /= 2

        val m = matches.toDouble()
        val jaro = (m / s1.length + m / s2.length + (m - transpositions) / m) / 3.0

        var prefixLength = 0
        val limit = minOf(4, s1.length, s2.length)
        for (i in 0 until limit) {
            if (s1[i] == s2[i]) prefixLength++ else break
        }
        return jaro + prefixLength * PREFIX_SCALE * (1.0 - jaro)
    }

    private companion object {
        const val EXACT_MATCH = 1000.0
        const val PREFIX_NAME = 500.0
        const val PREFIX_CAPITAL = 400.0
        const val SUBSTRING_NAME = 250.0
        const val SUBSTRING_CAPITAL = 200.0
        const val SUBSTRING_CODE = 180.0
        const val FUZZY_BASE = 100.0
        const val FUZZY_THRESHOLD = 0.85
        const val RECENT_BOOST = 40.0
        const val FAVORITE_BOOST = 80.0
        const val PREFIX_SCALE = 0.1
    }
}
