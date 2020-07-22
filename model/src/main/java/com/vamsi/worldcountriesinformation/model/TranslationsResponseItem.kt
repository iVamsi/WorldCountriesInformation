package com.vamsi.worldcountriesinformation.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TranslationsResponseItem(
    @Json(name = "br")
    val br: String,
    @Json(name = "de")
    val de: String,
    @Json(name = "es")
    val es: String,
    @Json(name = "fa")
    val fa: String,
    @Json(name = "fr")
    val fr: String,
    @Json(name = "hr")
    val hr: String,
    @Json(name = "it")
    val `it`: String,
    @Json(name = "ja")
    val ja: String,
    @Json(name = "nl")
    val nl: String,
    @Json(name = "pt")
    val pt: String
)