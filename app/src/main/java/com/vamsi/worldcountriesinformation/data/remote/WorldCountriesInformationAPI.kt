package com.vamsi.worldcountriesinformation.data.remote

import com.vamsi.worldcountriesinformation.constants.Constants.ALL
import com.vamsi.worldcountriesinformation.constants.Constants.NAME
import com.vamsi.worldcountriesinformation.model.CountriesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface WorldCountriesInformationAPI {

    @GET(ALL)
    suspend fun fetchWorldCountriesInformation(): CountriesResponse

    @GET(NAME)
    suspend fun fetchCountriesByName(
        @Path("name") countryName: String
    ): CountriesResponse
}