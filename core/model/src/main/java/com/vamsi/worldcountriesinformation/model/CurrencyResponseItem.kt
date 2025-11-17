package com.vamsi.worldcountriesinformation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyResponseItem(
    @SerialName("code")
    val code: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("symbol")
    val symbol: String? = null,
)
