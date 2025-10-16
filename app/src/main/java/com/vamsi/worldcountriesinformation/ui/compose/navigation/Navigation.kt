package com.vamsi.worldcountriesinformation.ui.compose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen
import com.vamsi.worldcountriesinformation.feature.countrydetails.CountryDetailsRoute

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Countries : Screen("countries")
    object CountryDetails : Screen("country_details/{countryCode}") {
        fun createRoute(countryCode: String) = "country_details/$countryCode"
    }
}

@Composable
fun WorldCountriesNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Countries.route
    ) {
        composable(Screen.Countries.route) {
            CountriesScreen(
                onCountryClick = { country ->
                    navController.navigate(
                        Screen.CountryDetails.createRoute(country.threeLetterCode)
                    )
                }
            )
        }

        composable(
            route = Screen.CountryDetails.route,
            arguments = listOf(
                navArgument("countryCode") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val countryCode = backStackEntry.arguments?.getString("countryCode") ?: ""
            CountryDetailsRoute(
                countryCode = countryCode,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
