package com.vamsi.worldcountriesinformation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TranslationsResponseItem(
    @SerialName("br")
    val br: String? = null,
    @SerialName("de")
    val de: String? = null,
    @SerialName("es")
    val es: String? = null,
    @SerialName("fa")
    val fa: String? = null,
    @SerialName("fr")
    val fr: String? = null,
    @SerialName("hr")
    val hr: String? = null,
    @SerialName("it")
    val `it`: String? = null,
    @SerialName("ja")
    val ja: String? = null,
    @SerialName("nl")
    val nl: String? = null,
    @SerialName("pt")
    val pt: String? = null,
)
