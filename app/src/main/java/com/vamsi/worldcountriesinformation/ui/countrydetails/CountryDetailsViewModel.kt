package com.vamsi.worldcountriesinformation.ui.countrydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.UiState
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.domainmodel.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountryDetailsViewModel @Inject constructor(
    private val countriesUseCase: GetCountriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Country>>(UiState.Idle)
    val uiState: StateFlow<UiState<Country>> = _uiState.asStateFlow()

    fun loadCountryDetails(countryCode: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            countriesUseCase(false)
                .catch { e ->
                    val exception = if (e is Exception) e else Exception(e.message ?: "Unknown error")
                    _uiState.value = UiState.Error(exception)
                }
                .collect { response ->
                    when (response) {
                        is ApiResponse.Loading -> {
                            _uiState.value = UiState.Loading
                        }
                        is ApiResponse.Success -> {
                            val country = response.data.firstOrNull {
                                it.threeLetterCode.equals(countryCode, ignoreCase = true)
                            }
                            if (country != null) {
                                _uiState.value = UiState.Success(country)
                            } else {
                                _uiState.value = UiState.Error(Exception("Country not found"))
                            }
                        }
                        is ApiResponse.Error -> {
                            _uiState.value = UiState.Error(response.exception)
                        }
                    }
                }
        }
    }

    fun retry(countryCode: String) {
        loadCountryDetails(countryCode)
    }
}