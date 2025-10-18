package com.vamsi.worldcountriesinformation.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for navigation route definitions and utilities.
 *
 * These tests verify:
 * - Route string correctness
 * - Route parameter substitution
 * - Argument key constants
 * - Type safety guarantees
 *
 * **Test Strategy:**
 * - Pure unit tests (no Android dependencies)
 * - Fast execution (no emulator needed)
 * - Verify route contracts
 * - Catch route drift early
 *
 * @see Screen
 * @see NavigationArgs
 */
class NavigationRoutesTest {

    /**
     * Test: Countries route has correct string value.
     *
     * Given: Screen.Countries sealed object
     * When: Accessing the route property
     * Then: Route should be "countries"
     *
     * This test ensures the route string doesn't change unexpectedly,
     * which would break deep links and saved navigation state.
     */
    @Test
    fun `countries route is correct`() {
        // When
        val route = Screen.Countries.route
        
        // Then
        assertEquals("countries", route)
    }

    /**
     * Test: CountryDetails route has correct pattern with parameter placeholder.
     *
     * Given: Screen.CountryDetails sealed object
     * When: Accessing the route property
     * Then: Route should contain the parameter placeholder
     *
     * The route pattern is used by Navigation Compose to match incoming routes
     * and extract parameters.
     */
    @Test
    fun `country details route pattern is correct`() {
        // When
        val route = Screen.CountryDetails.route
        
        // Then
        assertEquals("country_details/{countryCode}", route)
    }

    /**
     * Test: CountryDetails createRoute function substitutes parameter correctly.
     *
     * Given: A country code "USA"
     * When: Calling createRoute with the country code
     * Then: The {countryCode} placeholder should be replaced with "USA"
     *
     * This ensures the route builder works correctly for navigation.
     */
    @Test
    fun `country details createRoute substitutes parameter correctly`() {
        // Given
        val countryCode = "USA"
        
        // When
        val route = Screen.CountryDetails.createRoute(countryCode)
        
        // Then
        assertEquals("country_details/USA", route)
    }

    /**
     * Test: createRoute works with different country codes.
     *
     * Given: Various country codes
     * When: Creating routes for each code
     * Then: Each route should be correctly formatted
     *
     * This verifies the route builder works for all valid inputs.
     */
    @Test
    fun `country details createRoute works with different codes`() {
        // Test cases: code -> expected route
        val testCases = mapOf(
            "USA" to "country_details/USA",
            "GBR" to "country_details/GBR",
            "JPN" to "country_details/JPN",
            "IND" to "country_details/IND",
            "FRA" to "country_details/FRA",
            "DEU" to "country_details/DEU"
        )
        
        // Verify each test case
        testCases.forEach { (code, expectedRoute) ->
            val actualRoute = Screen.CountryDetails.createRoute(code)
            assertEquals(
                "Route for code '$code' should be correct",
                expectedRoute,
                actualRoute
            )
        }
    }

    /**
     * Test: ARG_COUNTRY_CODE constant has correct value.
     *
     * Given: Screen.CountryDetails.ARG_COUNTRY_CODE constant
     * When: Accessing the constant
     * Then: Value should match the placeholder in the route
     *
     * This ensures consistency between route pattern and argument extraction.
     */
    @Test
    fun `country details argument key is correct`() {
        // When
        val argKey = Screen.CountryDetails.ARG_COUNTRY_CODE
        
        // Then
        assertEquals("countryCode", argKey)
        
        // Verify it matches the route pattern placeholder
        val route = Screen.CountryDetails.route
        assert(route.contains("{$argKey}")) {
            "Route pattern should contain the argument placeholder {$argKey}"
        }
    }

    /**
     * Test: NavigationArgs.COUNTRY_CODE matches Screen constant.
     *
     * Given: Two argument key constants
     * When: Comparing their values
     * Then: They should be identical
     *
     * This prevents inconsistencies between different parts of navigation code.
     */
    @Test
    fun `navigation args country code matches screen constant`() {
        // When
        val argsKey = NavigationArgs.COUNTRY_CODE
        val screenKey = Screen.CountryDetails.ARG_COUNTRY_CODE
        
        // Then
        assertEquals(
            "NavigationArgs.COUNTRY_CODE should match Screen.CountryDetails.ARG_COUNTRY_CODE",
            screenKey,
            argsKey
        )
    }

    /**
     * Test: Route pattern doesn't contain actual country code.
     *
     * Given: Screen.CountryDetails route pattern
     * When: Checking if it contains a placeholder
     * Then: It should use {countryCode} not an actual code
     *
     * This ensures we're using a pattern, not a hardcoded value.
     */
    @Test
    fun `country details route uses placeholder not actual code`() {
        // When
        val route = Screen.CountryDetails.route
        
        // Then
        // Should contain placeholder
        assert(route.contains("{countryCode}")) {
            "Route should contain parameter placeholder {countryCode}"
        }
        
        // Should NOT contain actual country codes
        val actualCodes = listOf("USA", "GBR", "JPN", "IND")
        actualCodes.forEach { code ->
            assert(!route.contains(code)) {
                "Route should not contain actual country code $code"
            }
        }
    }

    /**
     * Test: createRoute handles lowercase input.
     *
     * Given: A lowercase country code
     * When: Creating a route
     * Then: The code should be preserved as-is (validation happens elsewhere)
     *
     * Note: Country code validation happens in the domain layer,
     * not in the navigation layer.
     */
    @Test
    fun `createRoute preserves case of country code`() {
        // Given
        val lowercaseCode = "usa"
        val uppercaseCode = "USA"
        
        // When
        val lowercaseRoute = Screen.CountryDetails.createRoute(lowercaseCode)
        val uppercaseRoute = Screen.CountryDetails.createRoute(uppercaseCode)
        
        // Then
        assertEquals("country_details/usa", lowercaseRoute)
        assertEquals("country_details/USA", uppercaseRoute)
        
        // Routes should be different
        assert(lowercaseRoute != uppercaseRoute) {
            "Routes with different case codes should be different"
        }
    }

    /**
     * Test: createRoute handles empty string.
     *
     * Given: An empty country code
     * When: Creating a route
     * Then: Route should still be created (validation happens elsewhere)
     *
     * This tests that route building doesn't fail, even with invalid input.
     * The actual validation happens in GetCountryByCodeUseCase.
     */
    @Test
    fun `createRoute handles empty country code`() {
        // Given
        val emptyCode = ""
        
        // When
        val route = Screen.CountryDetails.createRoute(emptyCode)
        
        // Then
        assertEquals("country_details/", route)
    }

    /**
     * Test: createRoute handles special characters.
     *
     * Given: Country codes with special characters
     * When: Creating routes
     * Then: Routes should be created (URL encoding happens elsewhere)
     *
     * This verifies the route builder doesn't crash on unexpected input.
     */
    @Test
    fun `createRoute handles special characters`() {
        // Given
        val specialCodes = listOf("US/A", "US A", "US%A", "US?A")
        
        // When & Then
        specialCodes.forEach { code ->
            val route = Screen.CountryDetails.createRoute(code)
            // Should not throw exception
            assert(route.startsWith("country_details/")) {
                "Route should start with prefix"
            }
        }
    }
}
