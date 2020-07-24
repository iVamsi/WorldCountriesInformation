package com.vamsi.worldcountriesinformation.data.remote

import com.vamsi.worldcountriesinformation.core.constants.Constants.ALL
import com.vamsi.worldcountriesinformation.core.constants.Constants.NAME
import com.vamsi.worldcountriesinformation.model.CountriesResponseItem
import retrofit2.http.GET
import retrofit2.http.Path

interface WorldCountriesInformationAPI {

    @GET(ALL)
    suspend fun fetchWorldCountriesInformation(): List<CountriesResponseItem>

    @GET(NAME)
    suspend fun fetchCountriesByName(
        @Path("name") countryName: String
    ): List<CountriesResponseItem>
}