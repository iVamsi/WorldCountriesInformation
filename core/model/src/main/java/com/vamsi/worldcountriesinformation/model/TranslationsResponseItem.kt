package com.vamsi.worldcountriesinformation.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TranslationsResponseItem(
    @Json(name = "br")
    val br: String? = null,
    @Json(name = "de")
    val de: String? = null,
    @Json(name = "es")
    val es: String? = null,
    @Json(name = "fa")
    val fa: String? = null,
    @Json(name = "fr")
    val fr: String? = null,
    @Json(name = "hr")
    val hr: String? = null,
    @Json(name = "it")
    val `it`: String? = null,
    @Json(name = "ja")
    val ja: String? = null,
    @Json(name = "nl")
    val nl: String? = null,
    @Json(name = "pt")
    val pt: String? = null
)