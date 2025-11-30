@file:Suppress("DEPRECATION")
package com.vamsi.worldcountriesinformation.core.navigation

/**
 * DEPRECATED: Navigation 2 extension functions.
 *
 * These extensions were used with Navigation 2's NavController.
 * With Navigation 3, use the [Navigator] class instead.
 *
 * **Migration Guide:**
 * - `navController.navigateToCountries()` → `navigator.navigate(CountriesRoute)`
 * - `navController.navigateToCountryDetails("USA")` → `navigator.navigate(CountryDetailsRoute("USA"))`
 * - `navController.navigateBack()` → `navigator.goBack()`
 *
 * @see Navigator for Navigation 3 navigation handling
 * @deprecated Use Navigation 3 APIs with Navigator class instead
 */

// All extension functions below are deprecated and kept only for backward compatibility
// during the migration period. They should not be used in new code.
