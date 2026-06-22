package com.vamsi.worldcountriesinformation.feature.countrydetails

import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.core.common.error.AppError
import com.vamsi.worldcountriesinformation.core.common.error.toAppError
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIViewModel
import com.vamsi.worldcountriesinformation.core.datastore.SearchPreferencesPort
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.core.onError
import com.vamsi.worldcountriesinformation.domain.core.onLoading
import com.vamsi.worldcountriesinformation.domain.core.onSuccess
import com.vamsi.worldcountriesinformation.domain.countries.CountryByCodeParams
import com.vamsi.worldcountriesinformation.domain.countries.GenerateCountrySummaryUseCase
import com.vamsi.worldcountriesinformation.domain.countries.GetCountryByCodeUseCase
import com.vamsi.worldcountriesinformation.domain.countries.GetNearbyCountriesUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.GetUserDataPolicyUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.ObserveFavoritesUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.ToggleFavoriteUseCase
import com.vamsi.worldcountriesinformation.domain.preferences.UserPreferencesPort
import com.vamsi.worldcountriesinformation.domainmodel.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.NumberFormat
import java.time.Clock
import java.util.Locale
import javax.inject.Inject
import com.vamsi.worldcountriesinformation.core.common.R as CommonR

/**
 * Central state holder for the country details experience.
 *
 * Reacts to user intents, coordinates domain use cases, and emits a single immutable state
 * stream for the UI. Supports cache policies, pull-to-refresh, sharing, maps integration,
 * nearby countries, and error handling.
 */
@HiltViewModel
class CountryDetailsViewModel @Inject constructor(
    private val getCountryByCodeUseCase: GetCountryByCodeUseCase,
    private val getNearbyCountriesUseCase: GetNearbyCountriesUseCase,
    private val searchPreferencesDataSource: SearchPreferencesPort,
    private val userPreferencesPort: UserPreferencesPort,
    private val getUserDataPolicyUseCase: GetUserDataPolicyUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val generateCountrySummaryUseCase: GenerateCountrySummaryUseCase,
    private val clock: Clock,
) : MVIViewModel<CountryDetailsContract.Intent, CountryDetailsContract.State, CountryDetailsContract.Effect>(
    initialState = CountryDetailsContract.State(),
) {

    init {
        viewModelScope.launch {
            userPreferencesPort.userPreferences.collect { prefs ->
                setState { copy(showMapBorders = prefs.showMapBorders) }
            }
        }
        viewModelScope.launch {
            observeFavoritesUseCase().collect { favorites ->
                val code = state.value.country?.threeLetterCode?.uppercase()
                setState {
                    copy(isFavorite = code != null && code in favorites)
                }
            }
        }
    }

    override fun handleIntent(intent: CountryDetailsContract.Intent) {
        when (intent) {
            is CountryDetailsContract.Intent.LoadCountryDetails -> loadCountryDetails(intent.countryCode)
            is CountryDetailsContract.Intent.RetryLoading -> retry(intent.countryCode)
            is CountryDetailsContract.Intent.RefreshCountry -> refresh(intent.countryCode)
            is CountryDetailsContract.Intent.ToggleFavorite -> toggleFavorite()
            is CountryDetailsContract.Intent.NavigateBack -> navigateBack()
            is CountryDetailsContract.Intent.ErrorShown -> clearError()
            is CountryDetailsContract.Intent.ShareCountry -> shareCountry()
            is CountryDetailsContract.Intent.OpenInMaps -> openInMaps()
            is CountryDetailsContract.Intent.NearbyCountryClicked -> navigateToCountry(intent.countryCode)
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
        policy: CachePolicy? = null,
    ) {
        viewModelScope.launch {
            val effectivePolicy = policy ?: getUserDataPolicyUseCase().first()
            Timber.d("Loading country details for: $countryCode with policy: $effectivePolicy")

            getCountryByCodeUseCase(CountryByCodeParams(countryCode, effectivePolicy))
                .catch { exception ->
                    if (exception is CancellationException) throw exception
                    Timber.e(exception, "Unexpected error loading country: $countryCode")
                    val error = exception.toAppError(
                        fallback = AppError.Generic(CommonR.string.error_load_country_details_failed),
                    )
                    setState {
                        copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error,
                        )
                    }
                    setEffect { CountryDetailsContract.Effect.ShowError(error = error) }
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
                            val favorites = observeFavoritesUseCase().first()
                            setState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    country = country,
                                    isFavorite = country.threeLetterCode.uppercase() in favorites,
                                    lastUpdated = clock.millis(),
                                    error = null,
                                )
                            }
                            viewModelScope.launch {
                                searchPreferencesDataSource.addToRecentlyViewedCountry(
                                    country.threeLetterCode,
                                )
                            }
                            loadAiSummary(country)
                            // Load nearby countries once we have the country data
                            loadNearbyCountries(country.region, country.threeLetterCode)
                        }
                        .onError { exception ->
                            Timber.e(exception, "Error loading country: $countryCode")
                            val error = when (
                                val mapped = exception.toAppError(
                                    fallback = AppError.Generic(CommonR.string.error_load_country_details_failed),
                                )
                            ) {
                                is AppError.NotFound -> AppError.NotFound(identifier = countryCode)
                                else -> mapped
                            }
                            setState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    error = error,
                                )
                            }
                            setEffect { CountryDetailsContract.Effect.ShowError(error = error) }
                        }
                }
        }
    }

    /**
     * Loads an on-device AI summary when the user has opted in via settings.
     */
    private fun loadAiSummary(country: Country) {
        viewModelScope.launch {
            val aiSummaryEnabled = userPreferencesPort.userPreferences
                .first()
                .aiSummaryEnabled

            if (!aiSummaryEnabled) {
                setState { copy(aiSummary = CountryDetailsContract.AiSummaryState.Disabled) }
                return@launch
            }

            setState { copy(aiSummary = CountryDetailsContract.AiSummaryState.Loading) }

            val summary = runCatching {
                generateCountrySummaryUseCase(country)
            }.onFailure { error ->
                if (error is CancellationException) throw error
                Timber.w(error, "Failed to generate AI summary for ${country.name}")
            }.getOrNull()

            setState {
                copy(
                    aiSummary = when {
                        summary != null -> CountryDetailsContract.AiSummaryState.Ready(summary)
                        else -> CountryDetailsContract.AiSummaryState.Unavailable
                    },
                )
            }
        }
    }

    /**
     * Loads nearby countries in the same region, excluding the current country.
     */
    private fun loadNearbyCountries(region: String, currentCountryCode: String) {
        viewModelScope.launch {
            setState { copy(isLoadingNearby = true) }

            getNearbyCountriesUseCase(region, currentCountryCode)
                .catch { exception ->
                    Timber.e(exception, "Error loading nearby countries for region: $region")
                    setState { copy(isLoadingNearby = false) }
                }
                .collect { countries ->
                    Timber.d("Loaded ${countries.size} nearby countries in $region")
                    setState {
                        copy(
                            nearbyCountries = countries,
                            isLoadingNearby = false,
                        )
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
        loadCountryDetails(countryCode)
    }

    private fun toggleFavorite() {
        val countryCode = state.value.country?.threeLetterCode ?: return
        viewModelScope.launch {
            val wasFavorite = state.value.isFavorite
            toggleFavoriteUseCase(countryCode)
            val messageRes = if (wasFavorite) {
                R.string.details_removed_from_favorites
            } else {
                R.string.details_added_to_favorites
            }
            setEffect { CountryDetailsContract.Effect.ShowMessage(messageRes = messageRes) }
        }
    }

    /**
     * Formats and shares country information via the Android share sheet.
     */
    private fun shareCountry() {
        val country = state.value.country ?: return
        Timber.d("Share requested for country: ${country.name}")

        val shareText = formatShareText(country)
        setEffect { CountryDetailsContract.Effect.ShareCountryCard(shareText) }
    }

    /**
     * Opens the country's location in an external maps application.
     */
    private fun openInMaps() {
        val country = state.value.country ?: return
        Timber.d("Open in Maps requested for: ${country.name}")

        if (country.latitude == 0.0 && country.longitude == 0.0) {
            setEffect {
                CountryDetailsContract.Effect.ShowError(
                    messageRes = R.string.details_location_unavailable,
                    formatArgs = listOf(country.name),
                )
            }
            return
        }

        setEffect {
            CountryDetailsContract.Effect.OpenInMaps(
                latitude = country.latitude,
                longitude = country.longitude,
                countryName = country.name,
            )
        }
    }

    /**
     * Navigates to another country's details screen.
     */
    private fun navigateToCountry(countryCode: String) {
        Timber.d("Navigate to country: $countryCode")
        setEffect { CountryDetailsContract.Effect.NavigateToCountryDetails(countryCode) }
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
        setState { copy(error = null) }
    }

    /**
     * Gets human-readable cache age description.
     */
    fun getCacheAge(): String {
        val timestamp = state.value.lastUpdated
        return if (timestamp > 0) {
            CachePolicy.getCacheAgeDescription(timestamp, clock.millis())
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
            CachePolicy.isCacheFresh(timestamp, nowMillis = clock.millis())
        } else {
            false
        }
    }

    companion object {

        /**
         * Converts a two-letter country code to a flag emoji.
         *
         * Uses Unicode Regional Indicator Symbols to render the flag.
         * Example: "US" → "🇺🇸", "GB" → "🇬🇧"
         */
        internal fun countryCodeToFlagEmoji(code: String): String {
            if (code.length != 2) return ""
            val upperCode = code.uppercase(Locale.US)
            val firstLetter = Character.codePointAt(upperCode, 0) - 0x41 + 0x1F1E6
            val secondLetter = Character.codePointAt(upperCode, 1) - 0x41 + 0x1F1E6
            return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
        }

        /**
         * Formats country data into a shareable text card.
         */
        internal fun formatShareText(country: Country): String {
            val flag = countryCodeToFlagEmoji(country.twoLetterCode)
            val populationFormatted = NumberFormat.getNumberInstance(Locale.US)
                .format(country.population)
            val languages = country.languages
                .mapNotNull { it.name }
                .joinToString(", ")
                .ifEmpty { "N/A" }
            val currencies = country.currencies
                .mapNotNull { currency ->
                    currency.name?.let { name ->
                        if (currency.code != null) "$name (${currency.code})" else name
                    }
                }
                .joinToString(", ")
                .ifEmpty { "N/A" }

            return buildString {
                appendLine("$flag ${country.name}")
                appendLine()
                appendLine("Capital: ${country.capital.ifEmpty { "N/A" }}")
                appendLine("Region: ${country.region}")
                appendLine("Population: $populationFormatted")
                appendLine("Languages: $languages")
                appendLine("Currencies: $currencies")
                if (country.callingCode.isNotEmpty()) {
                    appendLine("Calling Code: ${country.callingCode}")
                }
                appendLine()
                append("Shared via World Countries Info")
            }
        }
    }
}
