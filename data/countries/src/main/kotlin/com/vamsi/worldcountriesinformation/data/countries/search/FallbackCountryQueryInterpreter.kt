package com.vamsi.worldcountriesinformation.data.countries.search

import com.vamsi.worldcountriesinformation.domain.search.CountryQueryInterpreter
import com.vamsi.worldcountriesinformation.domain.search.StructuredCountryQuery
import timber.log.Timber
import javax.inject.Inject

/**
 * Uses the on-device LLM when available and falls back to deterministic rules otherwise.
 */
class FallbackCountryQueryInterpreter @Inject constructor(
    private val onDeviceLlmCountryQueryInterpreter: OnDeviceLlmCountryQueryInterpreter,
    private val ruleBasedCountryQueryInterpreter: RuleBasedCountryQueryInterpreter,
) : CountryQueryInterpreter {

    override suspend fun interpret(query: String): StructuredCountryQuery {
        return runCatching {
            onDeviceLlmCountryQueryInterpreter.interpretOrNull(query)
        }.onFailure { exception ->
            Timber.w(exception, "On-device LLM interpreter failed. Falling back to rules.")
        }.getOrNull()
            ?: ruleBasedCountryQueryInterpreter.interpret(query)
    }
}
