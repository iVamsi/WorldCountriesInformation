package com.vamsi.worldcountriesinformation.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

/**
 * Startup baseline-profile capture for the :app module. Run on a device or emulator:
 * `./gradlew :benchmark:connectedDebugAndroidTest`
 * Then merge the generated profile into [app/src/main/baseline-prof.txt] if you replace the checked-in rules.
 */
class BaselineProfileGenerator {

    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun startup() {
        baselineRule.collect(
            packageName = "com.vamsi.worldcountriesinformation",
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndWait()
        }
    }
}
