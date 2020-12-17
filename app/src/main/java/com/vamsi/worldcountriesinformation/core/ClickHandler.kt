package com.vamsi.worldcountriesinformation.core

import com.vamsi.worldcountriesinformation.domainmodel.Country


interface ClickHandler {
    fun onItemClick(country: Country)
}