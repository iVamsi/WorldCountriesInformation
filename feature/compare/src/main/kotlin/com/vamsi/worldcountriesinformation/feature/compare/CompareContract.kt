package com.vamsi.worldcountriesinformation.feature.compare

import com.vamsi.worldcountriesinformation.core.common.error.AppError
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIEffect
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIIntent
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIState
import com.vamsi.worldcountriesinformation.domainmodel.Country

/**
 * MVI contract for the compare-countries feature.
 */
object CompareContract {

    sealed interface Intent : MVIIntent {
        data class LoadCountries(val codes: List<String>) : Intent
        data class RetryLoading(val codes: List<String>) : Intent
        data object NavigateBack : Intent
    }

    data class State(
        val isLoading: Boolean = false,
        val countries: List<Country> = emptyList(),
        val error: AppError? = null,
    ) : MVIState {
        val showError: Boolean get() = error != null && !isLoading
        val showLoading: Boolean get() = isLoading && countries.isEmpty()
        val hasData: Boolean get() = countries.size >= 2 && !isLoading && error == null
    }

    sealed interface Effect : MVIEffect {
        data object NavigateBack : Effect
        data class ShowError(val error: AppError) : Effect
    }
}
