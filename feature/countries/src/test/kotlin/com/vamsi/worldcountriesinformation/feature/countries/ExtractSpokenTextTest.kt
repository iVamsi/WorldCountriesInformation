package com.vamsi.worldcountriesinformation.feature.countries

import android.app.Activity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExtractSpokenTextTest {

    @Test
    fun `returns spoken text when result is OK and text is present`() {
        val results = listOf("Hello World")
        assertEquals("Hello World", extractSpokenText(Activity.RESULT_OK, results))
    }

    @Test
    fun `trims whitespace from spoken text`() {
        val results = listOf("  Hello World  ")
        assertEquals("Hello World", extractSpokenText(Activity.RESULT_OK, results))
    }

    @Test
    fun `returns first result when multiple results present`() {
        val results = listOf("First", "Second", "Third")
        assertEquals("First", extractSpokenText(Activity.RESULT_OK, results))
    }

    @Test
    fun `returns null when result code is not OK`() {
        val results = listOf("Hello")
        assertNull(extractSpokenText(Activity.RESULT_CANCELED, results))
    }

    @Test
    fun `returns null when results is null`() {
        assertNull(extractSpokenText(Activity.RESULT_OK, null))
    }

    @Test
    fun `returns null when results list is empty`() {
        assertNull(extractSpokenText(Activity.RESULT_OK, emptyList()))
    }

    @Test
    fun `returns null when spoken text is blank`() {
        val results = listOf("   ")
        assertNull(extractSpokenText(Activity.RESULT_OK, results))
    }

    @Test
    fun `returns null when spoken text is empty`() {
        val results = listOf("")
        assertNull(extractSpokenText(Activity.RESULT_OK, results))
    }

    @Test
    fun `returns null for arbitrary non-OK result code`() {
        val results = listOf("Hello")
        assertNull(extractSpokenText(42, results))
    }
}
