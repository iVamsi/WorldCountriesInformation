package com.vamsi.worldcountriesinformation.feature.countries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.UiState
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.domainmodel.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val countriesUseCase: GetCountriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Country>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Country>>> = _uiState.asStateFlow()

    init {
        loadCountries()
    }

    fun loadCountries() {
        viewModelScope.launch {
            countriesUseCase(true)
                .catch { exception ->
                    Timber.e(exception, "Error loading countries")
                    _uiState.value = UiState.Error(
                        exception = Exception(exception),
                        message = "Failed to load countries. Please try again."
                    )
                }
                .collect { apiResponse ->
                    _uiState.value = when (apiResponse) {
                        is ApiResponse.Loading -> UiState.Loading
                        is ApiResponse.Success -> UiState.Success(apiResponse.data)
                        is ApiResponse.Error -> UiState.Error(
                            exception = apiResponse.exception,
                            message = "Failed to load countries. Please try again."
                        )
                    }
                }
        }
    }

    fun retry() {
        loadCountries()
    }
}
