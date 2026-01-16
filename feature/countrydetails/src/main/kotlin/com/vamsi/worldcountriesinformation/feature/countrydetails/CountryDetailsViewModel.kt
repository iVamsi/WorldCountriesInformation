package com.vamsi.worldcountriesinformation.feature.countrydetails

import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIViewModel
import com.vamsi.worldcountriesinformation.core.datastore.SearchPreferencesDataSource
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.core.onError
import com.vamsi.worldcountriesinformation.domain.core.onLoading
import com.vamsi.worldcountriesinformation.domain.core.onSuccess
import com.vamsi.worldcountriesinformation.domain.countries.CountryByCodeParams
import com.vamsi.worldcountriesinformation.domain.countries.GetCountryByCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Central state holder for the country details experience.
 *
 * Reacts to user intents, coordinates domain use cases, and emits a single immutable state
 * stream for the UI. Supports cache policies, pull-to-refresh, and error handling.
 */
@HiltViewModel
class CountryDetailsViewModel @Inject constructor(
    private val getCountryByCodeUseCase: GetCountryByCodeUseCase,
    private val searchPreferencesDataSource: SearchPreferencesDataSource,
) : MVIViewModel<CountryDetailsContract.Intent, CountryDetailsContract.State, CountryDetailsContract.Effect>(
    initialState = CountryDetailsContract.State()
) {

    override fun handleIntent(intent: CountryDetailsContract.Intent) {
        when (intent) {
            is CountryDetailsContract.Intent.LoadCountryDetails -> loadCountryDetails(intent.countryCode)
            is CountryDetailsContract.Intent.RetryLoading -> retry(intent.countryCode)
            is CountryDetailsContract.Intent.RefreshCountry -> refresh(intent.countryCode)
            is CountryDetailsContract.Intent.ToggleFavorite -> toggleFavorite()
            is CountryDetailsContract.Intent.NavigateBack -> navigateBack()
            is CountryDetailsContract.Intent.ErrorShown -> clearError()
        }
    }

    /**
     * Loads country details for the specified country code with configurable cache policy.
     *
     * Uses NETWORK_FIRST by default to ensure we get complete data including calling code (idd),
     * which is not included in the country list API response due to API limitations.
     */
    private fun loadCountryDetails(
        countryCode: String,
        policy: CachePolicy = CachePolicy.NETWORK_FIRST,
    ) {
        viewModelScope.launch {
            Timber.d("Loading country details for: $countryCode with policy: $policy")

            getCountryByCodeUseCase(CountryByCodeParams(countryCode, policy))
                .catch { exception ->
                    Timber.e(exception, "Unexpected error loading country: $countryCode")
                    setState {
                        copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = "Failed to load country details. Please try again."
                        )
                    }
                    setEffect { CountryDetailsContract.Effect.ShowError("Failed to load country details. Please try again.") }
                }
                .collect { response ->
                    response
                        .onLoading {
                            Timber.d("Country details loading...")
                            if (!state.value.isRefreshing) {
                                setState { copy(isLoading = true) }
                            }
                        }
                        .onSuccess { country ->
                            Timber.d("Country details loaded: ${country.name}")
                            setState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    country = country,
                                    lastUpdated = System.currentTimeMillis(),
                                    errorMessage = null
                                )
                            }
                            viewModelScope.launch {
                                searchPreferencesDataSource.addToRecentlyViewedCountry(
                                    country.threeLetterCode
                                )
                            }
                        }
                        .onError { exception ->
                            Timber.e(exception, "Error loading country: $countryCode")

                            val errorMessage = when {
                                exception.message?.contains("No cached data", ignoreCase = true) == true -> {
                                    "No cached data available. Please connect to the internet."
                                }

                                exception.message?.contains("not found", ignoreCase = true) == true -> {
                                    "Country with code '$countryCode' not found"
                                }

                                exception.message?.contains("timeout", ignoreCase = true) == true -> {
                                    "Connection timeout. Please check your internet and try again."
                                }

                                exception.message?.contains("network", ignoreCase = true) == true -> {
                                    "Network error. Please check your connection and try again."
                                }

                                else -> {
                                    "Failed to load country details. Please try again."
                                }
                            }

                            setState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    errorMessage = errorMessage
                                )
                            }
                            setEffect { CountryDetailsContract.Effect.ShowError(errorMessage) }
                        }
                }
        }
    }

    /**
     * Refreshes country details from network (pull-to-refresh).
     */
    private fun refresh(countryCode: String) {
        Timber.d("Refresh requested for country: $countryCode")
        setState { copy(isRefreshing = true) }
        loadCountryDetails(countryCode, CachePolicy.FORCE_REFRESH)
    }

    /**
     * Retries loading country details after an error.
     */
    private fun retry(countryCode: String) {
        Timber.d("Retry requested for country: $countryCode")
        loadCountryDetails(countryCode, CachePolicy.NETWORK_FIRST)
    }

    /**
     * Toggle favorite status of the country.
     */
    private fun toggleFavorite() {
        setState { copy(isFavorite = !isFavorite) }
        val message = if (state.value.isFavorite) "Added to favorites" else "Removed from favorites"
        setEffect { CountryDetailsContract.Effect.ShowToast(message) }
    }

    /**
     * Navigate back to previous screen.
     */
    private fun navigateBack() {
        Timber.d("Navigate back requested")
        setEffect { CountryDetailsContract.Effect.NavigateBack }
    }

    /**
     * Clear error message.
     */
    private fun clearError() {
        setState { copy(errorMessage = null) }
    }

    /**
     * Gets human-readable cache age description.
     */
    fun getCacheAge(): String {
        val timestamp = state.value.lastUpdated
        return if (timestamp > 0) {
            CachePolicy.getCacheAgeDescription(timestamp)
        } else {
            "Never"
        }
    }

    /**
     * Checks if cached data is still fresh.
     */
    fun isCacheFresh(): Boolean {
        val timestamp = state.value.lastUpdated
        return if (timestamp > 0) {
            CachePolicy.isCacheFresh(timestamp)
        } else {
            false
        }
    }
}

