package com.vamsi.worldcountriesinformation.core.common.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel implementing the MVI (Model-View-Intent) pattern.
 *
 * ## MVI Architecture
 *
 * The MVI pattern provides:
 * - **Unidirectional data flow**: User actions → Intents → State updates → UI renders
 * - **Single source of truth**: One immutable state object
 * - **Predictable state management**: Clear state transitions
 * - **Testability**: Easy to test state changes and side effects
 *
 * ## Key Components
 *
 * 1. **Intent**: User actions or system events
 * 2. **State**: Complete UI state (immutable)
 * 3. **Effect**: One-time side effects (navigation, toasts, etc.)
 *
 * ## Usage
 *
 * ```kotlin
 * @HiltViewModel
 * class CountriesViewModel @Inject constructor(
 *     private val getCountriesUseCase: GetCountriesUseCase
 * ) : MVIViewModel<CountriesIntent, CountriesState, CountriesEffect>(
 *     initialState = CountriesState()
 * ) {
 *
 *     init {
 *         processIntent(CountriesIntent.LoadCountries)
 *     }
 *
 *     override fun handleIntent(intent: CountriesIntent) {
 *         when (intent) {
 *             is CountriesIntent.LoadCountries -> loadCountries()
 *             is CountriesIntent.SearchQueryChanged -> updateSearch(intent.query)
 *             is CountriesIntent.CountryClicked -> navigateToDetails(intent.countryCode)
 *         }
 *     }
 *
 *     private fun loadCountries() {
 *         viewModelScope.launch {
 *             setState { copy(isLoading = true) }
 *
 *             getCountriesUseCase()
 *                 .onSuccess { countries ->
 *                     setState { copy(isLoading = false, countries = countries) }
 *                 }
 *                 .onFailure { error ->
 *                     setState { copy(isLoading = false, errorMessage = error.message) }
 *                 }
 *         }
 *     }
 *
 *     private fun navigateToDetails(countryCode: String) {
 *         setEffect { CountriesEffect.NavigateToDetails(countryCode) }
 *     }
 * }
 * ```
 *
 * @param Intent Type of intents this ViewModel handles
 * @param State Type of state this ViewModel manages
 * @param Effect Type of effects this ViewModel emits
 * @param initialState The initial state of the ViewModel
 *
 * @since 1.0.0
 */
abstract class MVIViewModel<Intent : MVIIntent, State : MVIState, Effect : MVIEffect>(
    initialState: State,
) : ViewModel() {

    // ============================================================================
    // State Management
    // ============================================================================

    /**
     * Internal mutable state.
     * Only accessible within this ViewModel for state updates.
     */
    private val _state = MutableStateFlow(initialState)

    /**
     * Public read-only state exposed to the UI.
     * UI components should collect from this flow using collectAsStateWithLifecycle().
     *
     * ## Usage in UI
     * ```kotlin
     * val state by viewModel.state.collectAsStateWithLifecycle()
     * ```
     */
    val state: StateFlow<State> = _state.asStateFlow()

    // ============================================================================
    // Effect Management
    // ============================================================================

    /**
     * Internal effect channel.
     * Uses Channel instead of SharedFlow to ensure effects are only consumed once.
     */
    private val _effect = Channel<Effect>(Channel.BUFFERED)

    /**
     * Public effect flow exposed to the UI.
     * UI components should collect effects using collectAsEffect extension.
     *
     * ## Usage in UI
     * ```kotlin
     * viewModel.effect.collectAsEffect { effect ->
     *     when (effect) {
     *         is MyEffect.NavigateToDetails -> navigate(effect.id)
     *         is MyEffect.ShowToast -> showToast(effect.message)
     *     }
     * }
     * ```
     */
    val effect = _effect.receiveAsFlow()

    // ============================================================================
    // Intent Processing
    // ============================================================================

    /**
     * Processes user intents and triggers appropriate state changes.
     *
     * This is the main entry point for all user actions.
     * Subclasses must implement this to handle specific intents.
     *
     * ## Implementation Guidelines
     *
     * - Use `when` expression for exhaustive intent handling
     * - Delegate to private functions for business logic
     * - Keep this function simple and readable
     *
     * ## Example
     * ```kotlin
     * override fun handleIntent(intent: CountriesIntent) {
     *     when (intent) {
     *         is CountriesIntent.LoadCountries -> loadCountries()
     *         is CountriesIntent.SearchQueryChanged -> updateSearch(intent.query)
     *         is CountriesIntent.CountryClicked -> navigateToDetails(intent.countryCode)
     *     }
     * }
     * ```
     *
     * @param intent The intent to process
     */
    abstract fun handleIntent(intent: Intent)

    /**
     * Public function to send intents from the UI.
     *
     * UI components should call this to trigger state changes.
     *
     * ## Usage in UI
     * ```kotlin
     * Button(onClick = { viewModel.processIntent(MyIntent.ButtonClicked) }) {
     *     Text("Click me")
     * }
     * ```
     *
     * @param intent The intent to process
     */
    fun processIntent(intent: Intent) {
        handleIntent(intent)
    }

    // ============================================================================
    // State Update Functions
    // ============================================================================

    /**
     * Updates the state using a reducer function.
     *
     * The reducer function receives the current state and returns the new state.
     * State updates are atomic and thread-safe.
     *
     * ## Usage
     * ```kotlin
     * setState { copy(isLoading = true) }
     * setState { copy(isLoading = false, data = newData) }
     * ```
     *
     * @param reducer Function that transforms the current state to the new state
     */
    protected fun setState(reducer: State.() -> State) {
        _state.value = _state.value.reducer()
    }

    /**
     * Directly sets a new state.
     *
     * Use this when you have a completely new state object.
     * Prefer [setState] with a reducer for partial updates.
     *
     * @param state The new state
     */
    protected fun setState(state: State) {
        _state.value = state
    }

    // ============================================================================
    // Effect Emission Functions
    // ============================================================================

    /**
     * Emits a one-time effect to the UI.
     *
     * Effects are delivered once and are not persisted across configuration changes.
     * Use effects for navigation, toasts, and other one-time events.
     *
     * ## Usage
     * ```kotlin
     * setEffect { MyEffect.NavigateToDetails(id) }
     * setEffect { MyEffect.ShowToast("Success!") }
     * ```
     *
     * @param builder Lambda that creates the effect
     */
    protected fun setEffect(builder: () -> Effect) {
        val effect = builder()
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * Directly emits an effect.
     *
     * Alternative to setEffect with builder function.
     *
     * @param effect The effect to emit
     */
    protected fun setEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
