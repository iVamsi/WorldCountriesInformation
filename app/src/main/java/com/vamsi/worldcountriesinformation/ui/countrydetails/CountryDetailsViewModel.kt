package com.vamsi.worldcountriesinformation.ui.countrydetails

import com.vamsi.worldcountriesinformation.core.BaseViewModel
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CountryDetailsViewModel @Inject constructor(
    private val countriesUseCase: GetCountriesUseCase
) : BaseViewModel() {
    //TODO
}