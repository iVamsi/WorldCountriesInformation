package com.vamsi.worldcountriesinformation.core.network

import com.vamsi.worldcountriesinformation.core.common.Constants.ALL
import com.vamsi.worldcountriesinformation.model.CountriesResponseItem
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API interface for World Countries Information
 */
interface WorldCountriesApi {

    /**
     * Fetch all countries information
     */
    @GET(ALL)
    suspend fun fetchWorldCountriesInformation(): List<CountriesResponseItem>

    /**
     * Fetch countries by name
     * @param countryName Name of the country to search for
     */
    @GET("name/{name}")
    suspend fun fetchCountriesByName(
        @Path("name") countryName: String
    ): List<CountriesResponseItem>
}
