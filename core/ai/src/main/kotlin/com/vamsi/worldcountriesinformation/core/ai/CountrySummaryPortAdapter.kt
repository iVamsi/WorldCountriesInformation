package com.vamsi.worldcountriesinformation.core.ai

import com.vamsi.worldcountriesinformation.domain.countries.CountrySummaryPort
import com.vamsi.worldcountriesinformation.domainmodel.Country
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountrySummaryPortAdapter @Inject constructor(
    private val countrySummaryGenerator: CountrySummaryGenerator,
) : CountrySummaryPort {

    override suspend fun generateSummary(country: Country): String? = countrySummaryGenerator.generateSummary(country.toSummaryDetails())
}
