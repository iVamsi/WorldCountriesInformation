package com.vamsi.worldcountriesinformation.benchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Macrobenchmark that scrolls the country list and measures frame timing.
 *
 * Run on a physical device:
 * `./gradlew :benchmark:connectedBenchmarkAndroidTest`
 */
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollCountriesList() = benchmarkRule.measureRepeated(
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
        list.setGestureMargin(device.displayWidth / 5)
        repeat(3) {
            list.fling(Direction.DOWN)
            device.waitForIdle()
        }
    }

    private companion object {
        const val TARGET_PACKAGE = "com.vamsi.worldcountriesinformation"
    }
}
