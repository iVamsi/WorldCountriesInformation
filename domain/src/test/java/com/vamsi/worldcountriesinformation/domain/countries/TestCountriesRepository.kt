package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.tests_shared.TestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object TestCountriesRepository: CountriesRepository {
    override fun getCountries(): Flow<ApiResponse<List<Country>>> {
        return flowOf(ApiResponse.Success(TestData.getCountries()))
    }
}