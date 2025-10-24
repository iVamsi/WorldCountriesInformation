package com.vamsi.worldcountriesinformation.core.navigation

/**
 * Sealed class hierarchy representing all navigation destinations in the app.
 *
 * This class serves as the **single source of truth** for navigation routes,
 * preventing route drift and inconsistencies across the application. It provides
 * type-safe navigation with compile-time guarantees.
 *
 * **Architecture Benefits:**
 * - Single Source of Truth: All routes defined in one place
 * - Type Safety: No string-based routing errors
 * - Compile-Time Validation: Invalid routes caught at compile time
 * - Refactoring Safety: IDE can track all route usages
 * - Deep Link Support: Routes can be used for deep linking
 * - Testability: Easy to test navigation flows
 *
 * **Design Patterns:**
 * - Sealed class hierarchy for exhaustive when statements
 * - Data objects for singleton route definitions
 * - Builder pattern for parameterized routes
 * - Constant definitions for argument keys
 *
 * **Usage in Features:**
 * Features should NOT create their own route definitions. Instead, they should:
 * 1. Receive navigation callbacks as lambdas
 * 2. Accept route parameters as function parameters
 * 3. Remain agnostic of navigation structure
 *
 * **Usage in App Module:**
 * The app module uses these routes to build the navigation graph:
 * ```kotlin
 * NavHost(startDestination = Screen.Countries.route) {
 *     composable(Screen.Countries.route) { CountriesScreen(...) }
 *     composable(Screen.CountryDetails.route) { CountryDetailsScreen(...) }
 * }
 * ```
 *
 * **Deep Linking:**
 * Routes can be converted to deep links:
 * ```
 * myapp://countries
 * myapp://country_details/USA
 * ```
 *
 * @property route The string route pattern used by Navigation Compose
 *
 * @see androidx.navigation.compose.NavHost
 * @see androidx.navigation.compose.composable
 *
 * @since 1.0.0
 */
sealed class Screen(val route: String) {
    
    /**
     * Countries list screen - the main landing screen of the app.
     *
     * **Route:** `"countries"`
     *
     * **Purpose:**
     * - Displays a scrollable list of all countries
     * - Allows searching and filtering countries
     * - Provides navigation to country details
     *
     * **Parameters:** None
     *
     * **Example:**
     * ```kotlin
     * navController.navigate(Screen.Countries.route)
     * ```
     *
     * **Deep Link:**
     * ```
     * myapp://countries
     * ```
     */
    data object Countries : Screen("countries")

    /**
     * Settings screen - user preferences and app configuration.
     *
     * **Route:** `"settings"`
     *
     * **Purpose:**
     * - Configure cache policy preferences
     * - Toggle offline mode
     * - View cache statistics
     * - Clear cached data
     * - View app information
     *
     * **Parameters:** None
     *
     * **Example:**
     * ```kotlin
     * navController.navigate(Screen.Settings.route)
     * ```
     *
     * **Deep Link:**
     * ```
     * myapp://settings
     * ```
     */
    data object Settings : Screen("settings")

    /**
     * Country details screen - shows detailed information about a specific country.
     *
     * **Route Pattern:** `"country_details/{countryCode}"`
     *
     * **Purpose:**
     * - Displays comprehensive information about a country
     * - Shows flag, map, statistics, and details
     * - Allows navigation back to countries list
     *
     * **Parameters:**
     * - [ARG_COUNTRY_CODE]: Three-letter country code (ISO 3166-1 alpha-3)
     *
     * **Route Creation:**
     * Use [createRoute] to create a valid route with parameters:
     * ```kotlin
     * val route = Screen.CountryDetails.createRoute("USA")
     * navController.navigate(route)
     * // Results in: "country_details/USA"
     * ```
     *
     * **Argument Extraction:**
     * ```kotlin
     * composable(Screen.CountryDetails.route) { backStackEntry ->
     *     val code = backStackEntry.arguments?.getString(
     *         Screen.CountryDetails.ARG_COUNTRY_CODE
     *     )
     * }
     * ```
     *
     * **Deep Link:**
     * ```
     * myapp://country_details/USA
     * myapp://country_details/GBR
     * myapp://country_details/JPN
     * ```
     *
     * **Validation:**
     * Country code should be:
     * - Exactly 3 characters
     * - ISO 3166-1 alpha-3 format
     * - Examples: USA, GBR, JPN, IND, FRA
     */
    data object CountryDetails : Screen("country_details/{countryCode}") {
        
        /**
         * Argument key for the country code parameter.
         *
         * Use this constant when:
         * - Defining navigation arguments
         * - Extracting arguments from back stack
         * - Testing navigation
         *
         * **Example:**
         * ```kotlin
         * navArgument(Screen.CountryDetails.ARG_COUNTRY_CODE) {
         *     type = NavType.StringType
         * }
         * ```
         */
        const val ARG_COUNTRY_CODE = "countryCode"
        
        /**
         * Creates a concrete route with the specified country code.
         *
         * This function substitutes the `{countryCode}` placeholder with the
         * actual country code, creating a valid navigation route.
         *
         * **Input Validation:**
         * While this function doesn't validate the country code format,
         * the [GetCountryByCodeUseCase] will validate it at the domain layer.
         *
         * **Thread Safety:**
         * This function is thread-safe and can be called from any thread.
         *
         * @param countryCode The three-letter country code (ISO 3166-1 alpha-3)
         *                    Examples: "USA", "GBR", "JPN", "IND"
         *                    Case-sensitive (should be uppercase)
         *
         * @return A complete navigation route string
         *         Example: "country_details/USA"
         *
         * @see ARG_COUNTRY_CODE for extracting the parameter
         *
         * Example:
         * ```kotlin
         * // In a click handler
         * val route = Screen.CountryDetails.createRoute(country.threeLetterCode)
         * navController.navigate(route)
         * ```
         */
        fun createRoute(countryCode: String): String = "country_details/$countryCode"
    }
}

/**
 * Object containing constants for navigation argument keys.
 *
 * This object provides a centralized location for all navigation argument
 * keys used throughout the app. While [Screen] objects have their own
 * argument constants, this object can be used for shared or cross-cutting
 * argument keys.
 *
 * **Purpose:**
 * - Centralized argument key management
 * - Avoid string duplication
 * - Enable IDE refactoring support
 * - Document argument contracts
 *
 * **Current Arguments:**
 * - [COUNTRY_CODE]: Three-letter country code for country details
 *
 * **Future Arguments:**
 * As the app grows, add more argument keys here:
 * - SEARCH_QUERY: For search results
 * - FILTER_TYPE: For filtered lists
 * - COMPARISON_IDS: For country comparison
 *
 * @since 1.0.0
 */
object NavigationArgs {
    /**
     * Argument key for country code parameter.
     *
     * **Type:** String
     * **Format:** ISO 3166-1 alpha-3 (e.g., "USA", "GBR", "JPN")
     * **Required:** Yes
     * **Default:** None
     *
     * **Used in:**
     * - Country details screen navigation
     * - Deep link handling
     * - Country sharing
     *
     * Example:
     * ```kotlin
     * val countryCode = arguments?.getString(NavigationArgs.COUNTRY_CODE)
     * ```
     */
    const val COUNTRY_CODE = "countryCode"
}
