package com.vamsi.worldcountriesinformation.feature.compare

import com.vamsi.worldcountriesinformation.domainmodel.Country
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Builds a simple template comparison insight when AI summaries are enabled.
 */
object CompareInsightGenerator {

    fun generate(countries: List<Country>): String? {
        if (countries.size < 2) return null

        val byPopulation = countries.sortedByDescending { it.population }
        val largest = byPopulation.first()
        val smallest = byPopulation.last()

        if (largest.population <= 0 || smallest.population <= 0) return null

        val ratio = largest.population.toDouble() / smallest.population
        val nf = NumberFormat.getNumberInstance(Locale.getDefault())

        return if (ratio >= 1.5) {
            "${largest.name} has about ${formatRatio(ratio)}× the population of ${smallest.name} " +
                "(${nf.format(largest.population)} vs ${nf.format(smallest.population)})."
        } else {
            val names = countries.joinToString(" and ") { it.name }
            "$names have similar population sizes (around ${nf.format(largest.population)})."
        }
    }

    private fun formatRatio(ratio: Double): String {
        val rounded = (ratio * 10).roundToInt() / 10.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }
}
