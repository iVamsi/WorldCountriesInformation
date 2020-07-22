package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.CurrentState
import kotlinx.coroutines.flow.Flow

interface CountriesRepository {
    fun getCountries(): Flow<CurrentState<List<Country>>>
}