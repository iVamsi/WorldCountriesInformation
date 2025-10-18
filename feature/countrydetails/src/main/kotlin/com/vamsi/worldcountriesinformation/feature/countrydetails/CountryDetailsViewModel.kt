package com.vamsi.worldcountriesinformation.feature.countrydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.UiState
import com.vamsi.worldcountriesinformation.domain.countries.GetCountryByCodeUseCase
import com.vamsi.worldcountriesinformation.domainmodel.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Country Details screen.
 *
 * This ViewModel manages the state and business logic for displaying detailed
 * information about a single country. It uses the [GetCountryByCodeUseCase] to
 * efficiently fetch a single country from the local database without loading
 * the entire countries list.
 *
 * **Architecture:**
 * - Follows MVVM pattern with unidirectional data flow
 * - Uses StateFlow for reactive UI updates
 * - Delegates business logic to use cases (Clean Architecture)
 * - Manages UI state with sealed [UiState] class
 *
 * **State Management:**
 * - [UiState.Idle] - Initial state before any data load
 * - [UiState.Loading] - Data is being fetched
 * - [UiState.Success] - Country data loaded successfully
 * - [UiState.Error] - Error occurred during data fetch
 *
 * **Performance:**
 * - Loads only the requested country (not entire list)
 * - Uses indexed database query for O(1) lookup
 * - Cancels ongoing operations when ViewModel is cleared
 *
 * **Error Handling:**
 * - Catches and logs all exceptions
 * - Provides user-friendly error messages
 * - Supports retry functionality
 *
 * @param getCountryByCodeUseCase Use case for fetching a single country
 *
 * @see GetCountryByCodeUseCase
 * @see UiState
 * @see Country
 *
 * @since 1.1.0
 *
 * Example usage:
 * ```kotlin
 * @Composable
 * fun CountryDetailsRoute(
 *     countryCode: String,
 *     viewModel: CountryDetailsViewModel = hiltViewModel()
 * ) {
 *     LaunchedEffect(countryCode) {
 *         viewModel.loadCountryDetails(countryCode)
 *     }
 *
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *     // Render UI based on state
 * }
 * ```
 */
@HiltViewModel
class CountryDetailsViewModel @Inject constructor(
    private val getCountryByCodeUseCase: GetCountryByCodeUseCase
) : ViewModel() {

    /**
     * Internal mutable state for country details.
     * Only this ViewModel can modify the state.
     */
    private val _uiState = MutableStateFlow<UiState<Country>>(UiState.Idle)
    
    /**
     * Public read-only state flow for observing country details.
     * UI components should collect from this flow to receive state updates.
     *
     * @see UiState for possible states
     */
    val uiState: StateFlow<UiState<Country>> = _uiState.asStateFlow()

    /**
     * Loads country details for the specified country code.
     *
     * This method initiates an asynchronous country fetch operation. It:
     * 1. Sets state to [UiState.Loading]
     * 2. Calls [GetCountryByCodeUseCase] with the provided code
     * 3. Transforms [ApiResponse] to [UiState]
     * 4. Handles errors gracefully with user-friendly messages
     *
     * **State Transitions:**
     * ```
     * Idle/Error → Loading → Success/Error
     * ```
     *
     * **Thread Safety:**
     * - Executes on viewModelScope (main-safe)
     * - Use case executes on IO dispatcher
     * - State updates on main thread
     *
     * **Error Handling:**
     * - Network errors: "Failed to load country details"
     * - Country not found: "Country not found"
     * - Invalid code: "Invalid country code"
     *
     * @param countryCode The three-letter country code (ISO 3166-1 alpha-3)
     *                    Examples: "USA", "GBR", "JPN"
     *                    Case-insensitive (will be normalized)
     *
     * Example:
     * ```kotlin
     * viewModel.loadCountryDetails("USA")
     * ```
     */
    fun loadCountryDetails(countryCode: String) {
        viewModelScope.launch {
            // Set loading state immediately
            _uiState.value = UiState.Loading
            
            Timber.d("Loading country details for code: $countryCode")

            getCountryByCodeUseCase(countryCode)
                .catch { exception ->
                    // Handle unexpected errors from Flow
                    val error = if (exception is Exception) {
                        exception
                    } else {
                        Exception(exception.message ?: "Unknown error occurred")
                    }
                    
                    Timber.e(error, "Error loading country details for code: $countryCode")
                    
                    _uiState.value = UiState.Error(
                        exception = error,
                        message = "Failed to load country details. Please try again."
                    )
                }
                .collect { apiResponse ->
                    // Transform ApiResponse to UiState
                    _uiState.value = when (apiResponse) {
                        is ApiResponse.Loading -> {
                            Timber.d("Country details loading...")
                            UiState.Loading
                        }
                        
                        is ApiResponse.Success -> {
                            Timber.d("Country details loaded: ${apiResponse.data.name}")
                            UiState.Success(apiResponse.data)
                        }
                        
                        is ApiResponse.Error -> {
                            val errorMessage = when {
                                apiResponse.exception.message?.contains("not found", ignoreCase = true) == true -> {
                                    "Country with code '$countryCode' not found"
                                }
                                else -> {
                                    "Failed to load country details. Please try again."
                                }
                            }
                            
                            Timber.e(apiResponse.exception, "Country details error: $errorMessage")
                            
                            UiState.Error(
                                exception = apiResponse.exception,
                                message = errorMessage
                            )
                        }
                    }
                }
        }
    }

    /**
     * Retries loading country details after an error.
     *
     * This is a convenience method that calls [loadCountryDetails] again
     * with the same country code. Useful for retry buttons in error states.
     *
     * @param countryCode The three-letter country code to retry
     *
     * Example:
     * ```kotlin
     * Button(onClick = { viewModel.retry(countryCode) }) {
     *     Text("Retry")
     * }
     * ```
     */
    fun retry(countryCode: String) {
        Timber.d("Retrying country details load for: $countryCode")
        loadCountryDetails(countryCode)
    }
}

