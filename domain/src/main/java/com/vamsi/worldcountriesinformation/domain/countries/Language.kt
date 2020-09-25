package com.vamsi.worldcountriesinformation.domain.countries

import java.io.Serializable

data class Language(
    val name: String? = "",
    val nativeName: String? = ""
): Serializable