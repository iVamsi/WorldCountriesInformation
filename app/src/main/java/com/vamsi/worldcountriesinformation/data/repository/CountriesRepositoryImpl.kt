package com.vamsi.worldcountriesinformation.data.repository

import com.vamsi.worldcountriesinformation.data.mappers.toCountries
import com.vamsi.worldcountriesinformation.data.remote.WorldCountriesInformationAPI
import com.vamsi.worldcountriesinformation.domain.core.CurrentState
import com.vamsi.worldcountriesinformation.domain.countries.CountriesRepository
import com.vamsi.worldcountriesinformation.domain.countries.Country
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CountriesRepositoryImpl @Inject constructor(
    private val countriesApi: WorldCountriesInformationAPI
): CountriesRepository {

    override fun getCountries(): Flow<CurrentState<List<Country>>> {
        return flow {
            try {
                val countriesInfo = countriesApi.fetchWorldCountriesInformation()
                CurrentState.Success(countriesInfo.toCountries())
            } catch (exception: Exception) {
                CurrentState.Error(exception)
            }
        }
    }
}