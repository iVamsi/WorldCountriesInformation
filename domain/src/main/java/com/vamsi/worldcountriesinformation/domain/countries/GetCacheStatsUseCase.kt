package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.UseCase
import com.vamsi.worldcountriesinformation.domain.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetCacheStatsUseCase
@Inject
constructor(
    private val countriesRepository: CountriesRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) : UseCase<Unit, CountryCacheSnapshot>(ioDispatcher) {
    override suspend fun execute(parameters: Unit): CountryCacheSnapshot = countriesRepository.getCountryCacheSnapshot()
}
