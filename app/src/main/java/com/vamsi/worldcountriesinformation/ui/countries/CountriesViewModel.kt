package com.vamsi.worldcountriesinformation.ui.countries

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.core.BaseViewModel
import com.vamsi.worldcountriesinformation.domain.core.successOr
import com.vamsi.worldcountriesinformation.domain.countries.Country
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CountriesViewModel @ViewModelInject constructor(
    private val countriesUseCase: GetCountriesUseCase
) : BaseViewModel() {

    private val _countries = MutableLiveData<List<Country>>()
    val countries: LiveData<List<Country>> = _countries

    init {
        viewModelScope.launch {
            countriesUseCase(true)
                .map { it.successOr(emptyList()) }
                .collect {
                    _countries.value = it
                }
        }
    }
}