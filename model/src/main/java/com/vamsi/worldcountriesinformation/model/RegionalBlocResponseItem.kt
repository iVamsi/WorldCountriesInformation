package com.vamsi.worldcountriesinformation.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegionalBlocResponseItem(
    @Json(name = "acronym")
    val acronym: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "otherAcronyms")
    val otherAcronyms: List<Any>,
    @Json(name = "otherNames")
    val otherNames: List<Any>
)