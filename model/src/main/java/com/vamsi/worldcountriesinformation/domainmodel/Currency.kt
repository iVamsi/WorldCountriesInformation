package com.vamsi.worldcountriesinformation.domainmodel

import java.io.Serializable

data class Currency(
    val code: String? = "",
    val name: String? = "",
    val symbol: String? = ""
): Serializable