package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.di.IoDispatcher
import com.vamsi.worldcountriesinformation.domainmodel.Country
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GenerateCountrySummaryUseCase
@Inject
constructor(
    private val countrySummaryPort: CountrySummaryPort,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(country: Country): String? = withContext(ioDispatcher) {
        countrySummaryPort.generateSummary(country)
    }
}
