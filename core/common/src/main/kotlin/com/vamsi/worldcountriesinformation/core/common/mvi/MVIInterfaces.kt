package com.vamsi.worldcountriesinformation.core.common.mvi

/**
 * Marker interface for all MVI intents.
 *
 * Intents represent user actions or system events that trigger state changes.
 * Every user interaction should be modeled as an intent.
 *
 * ## Examples
 *
 * ```kotlin
 * sealed interface CountriesIntent : MVIIntent {
 *     data object LoadCountries : CountriesIntent
 *     data class SearchQueryChanged(val query: String) : CountriesIntent
 *     data class CountryClicked(val countryCode: String) : CountriesIntent
 * }
 * ```
 *
 * @since 1.0.0
 */
interface MVIIntent

/**
 * Marker interface for all MVI states.
 *
 * States represent the complete UI state at any given moment.
 * Should be immutable and contain all data needed to render the UI.
 *
 * ## Best Practices
 *
 * - Use data classes for immutability
 * - Include all UI-related data
 * - Use computed properties for derived state
 * - Keep state serializable for state restoration
 *
 * ## Examples
 *
 * ```kotlin
 * data class CountriesState(
 *     val isLoading: Boolean = false,
 *     val countries: List<Country> = emptyList(),
 *     val errorMessage: String? = null
 * ) : MVIState {
 *     val showError: Boolean get() = errorMessage != null && !isLoading
 * }
 * ```
 *
 * @since 1.0.0
 */
interface MVIState

/**
 * Marker interface for all MVI effects.
 *
 * Effects represent one-time side effects that don't belong in the state.
 * Examples: navigation, showing toasts, playing sounds.
 *
 * ## Characteristics
 *
 * - One-time events (not persisted across configuration changes)
 * - Not part of the UI state
 * - Should be consumed immediately in the UI
 *
 * ## Examples
 *
 * ```kotlin
 * sealed interface CountriesEffect : MVIEffect {
 *     data class NavigateToDetails(val countryCode: String) : CountriesEffect
 *     data class ShowToast(val message: String) : CountriesEffect
 *     data class ShowError(val error: String) : CountriesEffect
 * }
 * ```
 *
 * @since 1.0.0
 */
interface MVIEffect
