package com.vamsi.worldcountriesinformation.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LanguageResponseItem(
    @Json(name = "iso639_1")
    val iso6391: String,
    @Json(name = "iso639_2")
    val iso6392: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "nativeName")
    val nativeName: String
)