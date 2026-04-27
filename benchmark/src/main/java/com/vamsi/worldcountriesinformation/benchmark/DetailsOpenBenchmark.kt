package com.vamsi.worldcountriesinformation.benchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Macrobenchmark that taps the first country in the list to navigate into
 * the details screen, measuring frame timings during the transition.
 */
@RunWith(AndroidJUnit4::class)
class DetailsOpenBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun openCountryDetails() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
        },
    ) {
        device.wait(Until.hasObject(By.scrollable(true)), 5_000)
        val list = device.findObject(By.scrollable(true)) ?: return@measureRepeated
        val firstChild = list.children.firstOrNull() ?: return@measureRepeated
        firstChild.click()
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()
    }

    private companion object {
        const val TARGET_PACKAGE = "com.vamsi.worldcountriesinformation"
    }
}
