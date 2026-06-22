package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domainmodel.Country

interface CountrySummaryPort {
    suspend fun generateSummary(country: Country): String?
}
