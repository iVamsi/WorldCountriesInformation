package com.vamsi.worldcountriesinformation.core.ai

import com.vamsi.worldcountriesinformation.domainmodel.CountryDetailsModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CountrySummaryGeneratorTest {

    private val capabilityChecker = mockk<AiCapabilityChecker>()

    private val generator = CountrySummaryGeneratorImpl(capabilityChecker)

    private val japanDetails = listOf(
        CountryDetailsModel(key = "name", value = "Japan"),
        CountryDetailsModel(key = "capital", value = "Tokyo"),
        CountryDetailsModel(key = "region", value = "Asia"),
        CountryDetailsModel(key = "population", value = "125000000"),
        CountryDetailsModel(key = "languages", value = "Japanese"),
    )

    @Test
    fun `returns template when on-device AI unavailable`() = runTest {
        every { capabilityChecker.isOnDeviceGenerationAvailable() } returns false

        val summary = generator.generateSummary(japanDetails)

        assertNotNull(summary)
        assertTrue(summary!!.contains("Japan"))
        assertTrue(summary.contains("Asia"))
        assertTrue(summary.contains("Tokyo"))
        assertTrue(summary.contains("125,000,000"))
        assertTrue(summary.contains("Japanese"))
    }

    @Test
    fun `returns null when name missing`() = runTest {
        every { capabilityChecker.isOnDeviceGenerationAvailable() } returns false

        val summary = generator.generateSummary(
            listOf(CountryDetailsModel(key = "capital", value = "Tokyo")),
        )

        assertNull(summary)
    }

    @Test
    fun `template omits empty optional fields`() = runTest {
        every { capabilityChecker.isOnDeviceGenerationAvailable() } returns false

        val summary = generator.generateSummary(
            listOf(CountryDetailsModel(key = "name", value = "Nauru")),
        )

        assertEquals("Nauru.", summary)
    }
}
