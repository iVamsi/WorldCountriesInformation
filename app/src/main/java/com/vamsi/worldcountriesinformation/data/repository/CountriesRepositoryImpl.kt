package com.vamsi.worldcountriesinformation.data.repository

import com.vamsi.worldcountriesinformation.data.mappers.toCountries
import com.vamsi.worldcountriesinformation.data.remote.WorldCountriesInformationAPI
import com.vamsi.worldcountriesinformation.domain.core.CurrentState
import com.vamsi.worldcountriesinformation.domain.countries.CountriesRepository
import com.vamsi.worldcountriesinformation.domain.countries.Country
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class CountriesRepositoryImpl @Inject constructor(
    private val countriesApi: WorldCountriesInformationAPI
): CountriesRepository {

    override fun getCountries(): Flow<CurrentState<List<Country>>> {
        return flow {
            // Emit the state to show progress bar
            emit(CurrentState.Loading)

            try {
                val countriesInfo = countriesApi.fetchWorldCountriesInformation()
                emit(CurrentState.Success(countriesInfo.toCountries()))
            } catch (exception: Exception) {
                Timber.d(exception)
                emit(CurrentState.Error(exception))
            }
        }
    }
}