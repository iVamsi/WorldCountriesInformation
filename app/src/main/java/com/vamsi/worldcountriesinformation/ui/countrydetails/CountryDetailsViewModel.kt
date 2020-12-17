package com.vamsi.worldcountriesinformation.ui.countrydetails

import androidx.hilt.lifecycle.ViewModelInject
import com.vamsi.worldcountriesinformation.core.BaseViewModel
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase

class CountryDetailsViewModel @ViewModelInject constructor(
    private val countriesUseCase: GetCountriesUseCase
) : BaseViewModel() {
    //TODO
}