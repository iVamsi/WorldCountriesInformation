package com.vamsi.worldcountriesinformation.feature.widget.data

import com.vamsi.worldcountriesinformation.domainmodel.Country

/**
 * Data class representing the state of the country widget
 */
data class WidgetData(
    val featuredCountry: Country? = null,
    val totalCountries: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
)

/**
 * Widget size configurations
 */
enum class WidgetSize {
    SMALL,  // Shows only featured country name and flag
    MEDIUM, // Shows featured country with basic info
    LARGE   // Shows featured country with full details
}

