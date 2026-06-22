package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.UseCase
import com.vamsi.worldcountriesinformation.domain.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class ClearCacheUseCase
@Inject
constructor(
    private val countriesRepository: CountriesRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) : UseCase<Unit, Unit>(ioDispatcher) {
    override suspend fun execute(parameters: Unit) {
        countriesRepository.clearCountryCache()
    }
}
