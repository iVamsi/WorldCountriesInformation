package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import javax.inject.Inject

/**
 * Picks a deterministic "country of the day" from a list using day-of-year rotation.
 */
class FeaturedCountrySelector
@Inject
constructor() {
    fun select(countries: List<CountrySummary>): CountrySummary? {
        if (countries.isEmpty()) return null
        val dayOfYear = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val index = (dayOfYear % countries.size).toInt()
        return countries[index]
    }
}
