package com.vamsi.worldcountriesinformation.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Display serif for country names as headings, screen titles, and quiz prompts. One bundled weight. */
val Fraunces = FontFamily(Font(R.font.fraunces_semibold, FontWeight.SemiBold))

private val Serif = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold)

private val DisplayLarge = Serif.copy(fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.5).sp)
private val DisplayMedium = Serif.copy(fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = (-0.25).sp)
private val DisplaySmall = Serif.copy(fontSize = 36.sp, lineHeight = 44.sp)
private val HeadlineLarge = Serif.copy(fontSize = 32.sp, lineHeight = 40.sp)
private val HeadlineMedium = Serif.copy(fontSize = 28.sp, lineHeight = 36.sp)
private val HeadlineSmall = Serif.copy(fontSize = 24.sp, lineHeight = 32.sp)
private val TitleLarge = Serif.copy(fontSize = 22.sp, lineHeight = 28.sp)

/**
 * Two voices: Fraunces for display, headline, and titleLarge; Roboto (Material defaults) for
 * everything read at length or scanned in lists. Nothing in the serif is smaller than 22 sp.
 */
val Typography = Typography().let { roboto ->
    roboto.copy(
        displayLarge = DisplayLarge,
        displayMedium = DisplayMedium,
        displaySmall = DisplaySmall,
        headlineLarge = HeadlineLarge,
        headlineMedium = HeadlineMedium,
        headlineSmall = HeadlineSmall,
        titleLarge = TitleLarge,
        displayLargeEmphasized = DisplayLarge,
        displayMediumEmphasized = DisplayMedium,
        displaySmallEmphasized = DisplaySmall,
        headlineLargeEmphasized = HeadlineLarge,
        headlineMediumEmphasized = HeadlineMedium,
        headlineSmallEmphasized = HeadlineSmall,
        titleLargeEmphasized = TitleLarge,
        titleMedium = roboto.titleMedium.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.1.sp),
        titleMediumEmphasized = roboto.titleMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
    )
}
