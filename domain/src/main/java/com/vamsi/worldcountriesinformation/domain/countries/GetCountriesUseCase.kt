package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.CurrentState
import com.vamsi.worldcountriesinformation.domain.core.FlowUseCase
import com.vamsi.worldcountriesinformation.domain.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class GetCountriesUseCase @Inject constructor(
    private val repository: CountriesRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : FlowUseCase<Boolean, List<Country>>(ioDispatcher) {

    override fun execute(parameters: Boolean): Flow<CurrentState<List<Country>>> {
        return repository.getCountries()
    }
}