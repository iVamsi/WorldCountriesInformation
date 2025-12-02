package com.vamsi.worldcountriesinformation.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation keys representing all navigation destinations in the app.
 *
 * This file serves as the **single source of truth** for navigation routes,
 * preventing route drift and inconsistencies across the application. It provides
 * type-safe navigation with compile-time guarantees using Navigation 3's NavKey interface.
 *
 * **Architecture Benefits:**
 * - Single Source of Truth: All routes defined in one place
 * - Type Safety: No string-based routing errors
 * - Compile-Time Validation: Invalid routes caught at compile time
 * - Refactoring Safety: IDE can track all route usages
 * - Serializable: Keys can be saved and restored across process death
 * - Testability: Easy to test navigation flows
 *
 * **Navigation 3 Migration:**
 * - Routes now implement NavKey interface
 * - Uses @Serializable annotation for state persistence
 * - Parameters are properties on data classes instead of route patterns
 *
 * @see androidx.navigation3.runtime.NavKey
 * @see androidx.navigation3.ui.NavDisplay
 *
 * @since 2.0.0
 */

/**
 * Countries list screen - the main landing screen of the app.
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
 * navigator.navigate(CountriesRoute)
 * ```
 */
@Serializable
data object CountriesRoute : NavKey

/**
 * Settings screen - user preferences and app configuration.
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
 * navigator.navigate(SettingsRoute)
 * ```
 */
@Serializable
data object SettingsRoute : NavKey

/**
 * Country details screen - shows detailed information about a specific country.
 *
 * **Purpose:**
 * - Displays comprehensive information about a country
 * - Shows flag, map, statistics, and details
 * - Allows navigation back to countries list
 *
 * **Parameters:**
 * - [countryCode]: Three-letter country code (ISO 3166-1 alpha-3)
 *
 * **Example:**
 * ```kotlin
 * navigator.navigate(CountryDetailsRoute("USA"))
 * ```
 *
 * **Validation:**
 * Country code should be:
 * - Exactly 3 characters
 * - ISO 3166-1 alpha-3 format
 * - Examples: USA, GBR, JPN, IND, FRA
 */
@Serializable
data class CountryDetailsRoute(val countryCode: String) : NavKey
