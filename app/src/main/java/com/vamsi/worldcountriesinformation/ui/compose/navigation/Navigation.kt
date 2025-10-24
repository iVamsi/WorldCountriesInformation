package com.vamsi.worldcountriesinformation.ui.compose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vamsi.worldcountriesinformation.core.navigation.Screen
import com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen
import com.vamsi.worldcountriesinformation.feature.countrydetails.CountryDetailsRoute
import com.vamsi.worldcountriesinformation.feature.settings.SettingsScreen

/**
 * Main navigation configuration for the World Countries application.
 *
 * This composable sets up the navigation graph using Jetpack Compose Navigation.
 * It serves as the single source of navigation logic in the app, connecting
 * feature modules while keeping them independent of each other.
 *
 * **Architecture:**
 * - Uses [Screen] from `:core:navigation` as single source of truth for routes
 * - Features remain decoupled - they only know about their own screens
 * - App module is the only place where navigation graph is composed
 * - Follows single responsibility principle
 *
 * **Navigation Flow:**
 * ```
 * Countries List Screen
 *      ↓ (click country)
 * Country Details Screen
 *      ↓ (back button)
 * Countries List Screen
 * ```
 *
 * **Deep Link Support:**
 * Routes defined in [Screen] can be used for deep linking:
 * - `countries` → Countries list
 * - `country_details/{countryCode}` → Specific country details
 *
 * **Type Safety:**
 * - Uses sealed class [Screen] for compile-time route safety
 * - Navigation arguments are type-safe through NavType
 * - No string-based route errors
 *
 * @param navController The navigation controller for managing app navigation
 *
 * @see Screen for route definitions
 * @see CountriesScreen for the countries list screen
 * @see CountryDetailsRoute for the country details screen
 *
 * @since 1.0.0
 *
 * Example usage:
 * ```kotlin
 * @Composable
 * fun MyApp() {
 *     val navController = rememberNavController()
 *     WorldCountriesNavigation(navController = navController)
 * }
 * ```
 */
@Composable
fun WorldCountriesNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Countries.route
    ) {
        // Countries List Screen
        composable(Screen.Countries.route) {
            CountriesScreen(
                onCountryClick = { country ->
                    // Navigate to country details using type-safe route creation
                    navController.navigate(
                        Screen.CountryDetails.createRoute(country.threeLetterCode)
                    )
                },
                onNavigateToSettings = {
                    // Navigate to settings screen
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    // Pop back stack to return to countries list
                    navController.popBackStack()
                }
            )
        }

        // Country Details Screen
        composable(
            route = Screen.CountryDetails.route,
            arguments = listOf(
                navArgument(Screen.CountryDetails.ARG_COUNTRY_CODE) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            // Extract country code from navigation arguments
            val countryCode = backStackEntry.arguments?.getString(
                Screen.CountryDetails.ARG_COUNTRY_CODE
            ) ?: ""
            
            CountryDetailsRoute(
                countryCode = countryCode,
                onNavigateBack = {
                    // Pop back stack to return to countries list
                    navController.popBackStack()
                }
            )
        }
    }
}
