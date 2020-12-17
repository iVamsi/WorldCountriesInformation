package com.vamsi.worldcountriesinformation.domainmodel

import java.io.Serializable

data class Language(
    val name: String? = "",
    val nativeName: String? = ""
): Serializable