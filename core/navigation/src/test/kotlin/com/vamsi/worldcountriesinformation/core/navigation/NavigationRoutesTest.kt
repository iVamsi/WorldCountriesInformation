package com.vamsi.worldcountriesinformation.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Navigation 3 route definitions.
 *
 * These tests verify:
 * - Route data classes are correctly defined
 * - Route parameters work correctly
 * - NavKey interface implementation
 * - Type safety guarantees
 *
 * **Test Strategy:**
 * - Pure unit tests (no Android dependencies)
 * - Fast execution (no emulator needed)
 * - Verify route contracts
 * - Catch route drift early
 *
 * @see CountriesRoute
 * @see SettingsRoute
 * @see CountryDetailsRoute
 */
class NavigationRoutesTest {

    /**
     * Test: CountriesRoute is a data object.
     *
     * Given: CountriesRoute
     * When: Creating/accessing it
     * Then: It should be a singleton data object
     */
    @Test
    fun `countries route is data object`() {
        // When
        val route = CountriesRoute

        // Then
        assertNotNull(route)
        // Verify it's the same instance (singleton)
        assertTrue(route === CountriesRoute)
    }

    /**
     * Test: SettingsRoute is a data object.
     *
     * Given: SettingsRoute
     * When: Creating/accessing it
     * Then: It should be a singleton data object
     */
    @Test
    fun `settings route is data object`() {
        // When
        val route = SettingsRoute

        // Then
        assertNotNull(route)
        // Verify it's the same instance (singleton)
        assertTrue(route === SettingsRoute)
    }

    /**
     * Test: CountryDetailsRoute stores country code correctly.
     *
     * Given: A country code "USA"
     * When: Creating CountryDetailsRoute with the country code
     * Then: The countryCode property should contain "USA"
     */
    @Test
    fun `country details route stores country code correctly`() {
        // Given
        val countryCode = "USA"

        // When
        val route = CountryDetailsRoute(countryCode)

        // Then
        assertEquals("USA", route.countryCode)
    }

    /**
     * Test: CountryDetailsRoute works with different country codes.
     *
     * Given: Various country codes
     * When: Creating routes for each code
     * Then: Each route should store the correct code
     */
    @Test
    fun `country details route works with different codes`() {
        // Test cases
        val testCodes = listOf("USA", "GBR", "JPN", "IND", "FRA", "DEU")

        // Verify each test case
        testCodes.forEach { code ->
            val route = CountryDetailsRoute(code)
            assertEquals(
                "Route for code '$code' should be correct",
                code,
                route.countryCode
            )
        }
    }

    /**
     * Test: CountryDetailsRoute equals works correctly.
     *
     * Given: Two routes with the same country code
     * When: Comparing them
     * Then: They should be equal
     */
    @Test
    fun `country details route equals works correctly`() {
        // Given
        val route1 = CountryDetailsRoute("USA")
        val route2 = CountryDetailsRoute("USA")
        val route3 = CountryDetailsRoute("GBR")

        // Then
        assertEquals(route1, route2)
        assertTrue(route1 != route3)
    }

    /**
     * Test: CountryDetailsRoute preserves case.
     *
     * Given: A lowercase country code
     * When: Creating a route
     * Then: The code should be preserved as-is
     */
    @Test
    fun `country details route preserves case`() {
        // Given
        val lowercaseCode = "usa"
        val uppercaseCode = "USA"

        // When
        val lowercaseRoute = CountryDetailsRoute(lowercaseCode)
        val uppercaseRoute = CountryDetailsRoute(uppercaseCode)

        // Then
        assertEquals("usa", lowercaseRoute.countryCode)
        assertEquals("USA", uppercaseRoute.countryCode)

        // Routes should be different
        assertTrue(lowercaseRoute != uppercaseRoute)
    }

    /**
     * Test: CountryDetailsRoute handles empty string.
     *
     * Given: An empty country code
     * When: Creating a route
     * Then: Route should still be created (validation happens elsewhere)
     */
    @Test
    fun `country details route handles empty country code`() {
        // Given
        val emptyCode = ""

        // When
        val route = CountryDetailsRoute(emptyCode)

        // Then
        assertEquals("", route.countryCode)
    }

    /**
     * Test: CountryDetailsRoute handles special characters.
     *
     * Given: Country codes with special characters
     * When: Creating routes
     * Then: Routes should be created without exceptions
     */
    @Test
    fun `country details route handles special characters`() {
        // Given
        val specialCodes = listOf("US/A", "US A", "US%A", "US?A")

        // When & Then
        specialCodes.forEach { code ->
            val route = CountryDetailsRoute(code)
            assertEquals(code, route.countryCode)
        }
    }

    /**
     * Test: CountryDetailsRoute copy works correctly.
     *
     * Given: A country details route
     * When: Creating a copy with different country code
     * Then: The copy should have the new code
     */
    @Test
    fun `country details route copy works correctly`() {
        // Given
        val originalRoute = CountryDetailsRoute("USA")

        // When
        val copiedRoute = originalRoute.copy(countryCode = "GBR")

        // Then
        assertEquals("USA", originalRoute.countryCode)
        assertEquals("GBR", copiedRoute.countryCode)
    }

    /**
     * Test: CountryDetailsRoute hashCode is consistent.
     *
     * Given: Two routes with the same country code
     * When: Getting their hash codes
     * Then: Hash codes should be equal
     */
    @Test
    fun `country details route hashCode is consistent`() {
        // Given
        val route1 = CountryDetailsRoute("USA")
        val route2 = CountryDetailsRoute("USA")

        // Then
        assertEquals(route1.hashCode(), route2.hashCode())
    }

    // ==================== Legacy Screen tests (for backward compatibility) ====================

    @Suppress("DEPRECATION")
    @Test
    fun `legacy countries route is correct`() {
        val route = Screen.Countries.route
        assertEquals("countries", route)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `legacy country details route pattern is correct`() {
        val route = Screen.CountryDetails.route
        assertEquals("country_details/{countryCode}", route)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `legacy country details createRoute substitutes parameter correctly`() {
        val countryCode = "USA"
        val route = Screen.CountryDetails.createRoute(countryCode)
        assertEquals("country_details/USA", route)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `legacy country details argument key is correct`() {
        val argKey = Screen.CountryDetails.ARG_COUNTRY_CODE
        assertEquals("countryCode", argKey)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `legacy navigation args country code matches screen constant`() {
        val argsKey = NavigationArgs.COUNTRY_CODE
        val screenKey = Screen.CountryDetails.ARG_COUNTRY_CODE
        assertEquals(screenKey, argsKey)
    }
}
