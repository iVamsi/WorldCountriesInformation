package com.vamsi.worldcountriesinformation.feature.compare

import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.core.common.error.AppError
import com.vamsi.worldcountriesinformation.core.common.error.toAppError
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIViewModel
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.CountryByCodeParams
import com.vamsi.worldcountriesinformation.domain.countries.GetCountryByCodeUseCase
import com.vamsi.worldcountriesinformation.domainmodel.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CompareViewModel @Inject constructor(
    private val getCountryByCodeUseCase: GetCountryByCodeUseCase,
) : MVIViewModel<CompareContract.Intent, CompareContract.State, CompareContract.Effect>(
    initialState = CompareContract.State(),
) {

    override fun handleIntent(intent: CompareContract.Intent) {
        when (intent) {
            is CompareContract.Intent.LoadCountries -> loadCountries(intent.codes)
            is CompareContract.Intent.RetryLoading -> loadCountries(intent.codes)
            is CompareContract.Intent.NavigateBack -> setEffect { CompareContract.Effect.NavigateBack }
        }
    }

    private fun loadCountries(codes: List<String>) {
        if (codes.size < 2) {
            val error = AppError.Generic(R.string.compare_error_min_selection)
            setState { copy(isLoading = false, error = error) }
            setEffect { CompareContract.Effect.ShowError(error) }
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                val countries = codes
                    .map { code ->
                        async { fetchCountry(code) }
                    }
                    .awaitAll()
                    .filterNotNull()

                if (countries.size < 2) {
                    val error = AppError.Generic(R.string.compare_error_load_failed)
                    setState { copy(isLoading = false, error = error) }
                    setEffect { CompareContract.Effect.ShowError(error) }
                } else {
                    setState {
                        copy(
                            isLoading = false,
                            countries = countries,
                            error = null,
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Compare: failed to load countries: $codes")
                val error = e.toAppError(
                    fallback = AppError.Generic(R.string.compare_error_load_failed),
                )
                setState { copy(isLoading = false, error = error) }
                setEffect { CompareContract.Effect.ShowError(error) }
            }
        }
    }

    private suspend fun fetchCountry(code: String): Country? {
        val response = getCountryByCodeUseCase(
            CountryByCodeParams(code, CachePolicy.CACHE_FIRST),
        ).firstOrNull { it !is ApiResponse.Loading }
        return when (response) {
            is ApiResponse.Success -> response.data
            else -> null
        }
    }
}
