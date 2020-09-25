package com.vamsi.worldcountriesinformation.core

import com.vamsi.worldcountriesinformation.domain.countries.Country

interface ClickHandler {
    fun onItemClick(country: Country)
}