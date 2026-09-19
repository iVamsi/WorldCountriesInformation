package com.vamsi.worldcountriesinformation.feature.quiz

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest
import com.vamsi.worldcountriesinformation.core.designsystem.AppBackground
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme

@Composable
private fun Screenshot(state: QuizContract.State) {
    WorldCountriesTheme(dynamicColor = false) {
        AppBackground { QuizScreen(state = state, onIntent = {}) }
    }
}

@PreviewTest
@PreviewLightDark
@Preview(name = "200%", fontScale = 2f)
@Composable
private fun QuizModePicker() = Screenshot(QuizContract.State())

@PreviewTest
@PreviewLightDark
@Preview(name = "Expanded", widthDp = 840, heightDp = 700)
@Composable
private fun QuizQuestion() = Screenshot(previewQuestionState())

@PreviewTest
@Preview
@Preview(name = "200%", fontScale = 2f, heightDp = 1100)
@Composable
private fun QuizAnsweredWrong() = Screenshot(previewQuestionState(selectedIndex = 0))

@PreviewTest
@Preview(name = "RTL", locale = "ar")
@Composable
private fun QuizQuestionRtl() = Screenshot(previewQuestionState())

@PreviewTest
@Preview
@Composable
private fun QuizAnsweredCorrect() = Screenshot(previewQuestionState(selectedIndex = 1))
