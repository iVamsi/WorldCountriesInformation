package com.vamsi.worldcountriesinformation.core.network

import com.vamsi.worldcountriesinformation.core.common.Constants.ALL
import com.vamsi.worldcountriesinformation.model.CountriesV3ResponseItem
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API interface for World Countries Information
 *
 * Using REST Countries API v3.1
 * Base URL: https://restcountries.com/v3.1/
 *
 * API Documentation: https://restcountries.com
 */
interface WorldCountriesApi {

    /**
     * Fetch all countries information from REST Countries API v3.1
     *
     * Endpoint: GET /v3.1/all
     * Returns: List of all countries with complete information
     *
     * Note: v3.1 returns different structure compared to v2:
     * - currencies is now a Map instead of List
     * - languages is now a Map instead of List
     * - name is now a complex object
     * - capital is now a List<String>
     */
    @GET(ALL)
    suspend fun fetchWorldCountriesInformation(): List<CountriesV3ResponseItem>

    /**
     * Fetch countries by name from REST Countries API v3.1
     *
     * Endpoint: GET /v3.1/name/{name}
     * @param countryName Name of the country to search for (partial match supported)
     * Returns: List of countries matching the name
     */
    @GET("name/{name}")
    suspend fun fetchCountriesByName(
        @Path("name") countryName: String
    ): List<CountriesV3ResponseItem>

    /**
     * Fetch a single country by its alpha code from REST Countries API v3.1
     *
     * Endpoint: GET /v3.1/alpha/{code}
     * @param code The country code (alpha-2, alpha-3, or numeric)
     *             Examples: "US", "USA", "840"
     * Returns: Single country matching the code
     *
     * **Optimization Benefits:**
     * - Fetches only requested country (not entire list)
     * - Reduces bandwidth and response time
     * - Perfect for country details screen
     * - Supports field filtering for minimal payload
     *
     * Note: Returns a single-item list for consistency with other endpoints
     */
    @GET("alpha/{code}")
    suspend fun fetchCountryByCode(
        @Path("code") code: String
    ): List<CountriesV3ResponseItem>
}
