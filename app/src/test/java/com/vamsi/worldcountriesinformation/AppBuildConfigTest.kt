package com.vamsi.worldcountriesinformation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppBuildConfigTest {

    @Test
    fun applicationIdMatchesManifest() {
        assertEquals("com.vamsi.worldcountriesinformation", BuildConfig.APPLICATION_ID)
    }
}
