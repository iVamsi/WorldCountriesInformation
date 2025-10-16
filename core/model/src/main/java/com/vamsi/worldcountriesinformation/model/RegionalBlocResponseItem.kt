package com.vamsi.worldcountriesinformation.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegionalBlocResponseItem(
    @Json(name = "acronym")
    val acronym: String? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "otherAcronyms")
    val otherAcronyms: List<Any>? = null,
    @Json(name = "otherNames")
    val otherNames: List<String>? = null
)