package com.vamsi.worldcountriesinformation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegionalBlocResponseItem(
    @SerialName("acronym")
    val acronym: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("otherAcronyms")
    val otherAcronyms: List<String>? = null,
    @SerialName("otherNames")
    val otherNames: List<String>? = null,
)
