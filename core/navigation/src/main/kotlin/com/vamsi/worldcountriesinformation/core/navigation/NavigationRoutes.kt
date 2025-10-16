package com.vamsi.worldcountriesinformation.core.navigation

/**
 * Navigation routes for the app.
 * Uses sealed class for type-safe navigation.
 */
sealed class Screen(val route: String) {
    /**
     * Countries list screen
     */
    data object Countries : Screen("countries")

    /**
     * Country details screen
     * @param countryCode Three-letter country code
     */
    data object CountryDetails : Screen("country_details/{countryCode}") {
        fun createRoute(countryCode: String) = "country_details/$countryCode"

        const val ARG_COUNTRY_CODE = "countryCode"
    }
}

/**
 * Navigation argument keys
 */
object NavigationArgs {
    const val COUNTRY_CODE = "countryCode"
}
