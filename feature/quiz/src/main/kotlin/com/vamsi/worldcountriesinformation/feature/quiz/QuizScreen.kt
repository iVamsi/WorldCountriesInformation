package com.vamsi.worldcountriesinformation.feature.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vamsi.worldcountriesinformation.core.common.error.message
import com.vamsi.worldcountriesinformation.core.common.testing.UiTestTags
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme
import com.vamsi.worldcountriesinformation.core.designsystem.component.ErrorState
import com.vamsi.worldcountriesinformation.domain.quiz.GuessMode
import com.vamsi.worldcountriesinformation.domain.quiz.QuizQuestion
import kotlinx.coroutines.flow.collectLatest

@Composable
fun QuizRoute(
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is QuizContract.Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    QuizScreen(
        state = state,
        onIntent = viewModel::processIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuizScreen(
    state: QuizContract.State,
    onIntent: (QuizContract.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag(UiTestTags.QUIZ_SCREEN),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.quiz_title)) },
                navigationIcon = {
                    IconButton(onClick = { onIntent(QuizContract.Intent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.quiz_navigate_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.answered > 0 || state.mode != null) {
                Text(
                    text = stringResource(
                        R.string.quiz_score,
                        state.score,
                        state.answered,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                if (state.highScore > 0 || state.bestStreak > 0) {
                    Text(
                        text = stringResource(
                            R.string.quiz_best_stats,
                            state.highScore,
                            state.bestStreak,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (state.currentStreak > 1) {
                    Text(
                        text = stringResource(R.string.quiz_current_streak, state.currentStreak),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            when {
                state.isLoading -> LoadingContent()

                state.showModePicker -> ModePicker(onSelectMode = { onIntent(QuizContract.Intent.SelectMode(it)) })

                state.showError -> QuizErrorContent(
                    message = state.error?.let { LocalQuizErrorMessage(it) }
                        ?: stringResource(R.string.quiz_error_load),
                    onRetry = { onIntent(QuizContract.Intent.LoadQuestion) },
                    onChangeMode = { onIntent(QuizContract.Intent.ClearMode) },
                )

                state.showQuestion -> QuestionContent(
                    question = state.question!!,
                    selectedIndex = state.selectedIndex,
                    lastAnswerCorrect = state.lastAnswerCorrect,
                    onAnswer = { onIntent(QuizContract.Intent.AnswerSelected(it)) },
                    onNext = { onIntent(QuizContract.Intent.NextQuestion) },
                    onChangeMode = { onIntent(QuizContract.Intent.ClearMode) },
                )
            }
        }
    }
}

@Composable
private fun LocalQuizErrorMessage(error: com.vamsi.worldcountriesinformation.core.common.error.AppError): String {
    val resources = androidx.compose.ui.platform.LocalResources.current
    return resources.message(error)
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularWavyProgressIndicator(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun ModePicker(onSelectMode: (GuessMode) -> Unit) {
    Text(
        text = stringResource(R.string.quiz_pick_mode),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .semantics { heading() },
    )
    ModeButton(label = stringResource(R.string.quiz_mode_flag)) { onSelectMode(GuessMode.FLAG) }
    ModeButton(label = stringResource(R.string.quiz_mode_capital)) { onSelectMode(GuessMode.CAPITAL) }
    ModeButton(label = stringResource(R.string.quiz_mode_region)) { onSelectMode(GuessMode.REGION) }
}

@Composable
private fun ModeButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(label)
    }
}

@Composable
private fun QuizErrorContent(
    message: String,
    onRetry: () -> Unit,
    onChangeMode: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ErrorState(
            message = message,
            onRetry = onRetry,
            modifier = Modifier.fillMaxWidth(),
            retryLabel = stringResource(R.string.quiz_retry),
        )
        OutlinedButton(onClick = onChangeMode, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.quiz_change_mode))
        }
    }
}

@Composable
private fun ColumnScope.QuestionContent(
    question: QuizQuestion,
    selectedIndex: Int?,
    lastAnswerCorrect: Boolean?,
    onAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onChangeMode: () -> Unit,
) {
    val context = LocalContext.current
    val prompt = when (question.mode) {
        GuessMode.FLAG -> stringResource(R.string.quiz_prompt_flag)
        GuessMode.CAPITAL -> stringResource(R.string.quiz_prompt_capital, question.country.name)
        GuessMode.REGION -> stringResource(R.string.quiz_prompt_region, question.country.name)
    }

    Text(
        text = prompt,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp),
    )

    if (question.mode == GuessMode.FLAG) {
        val flagResourceName = "${question.country.twoLetterCode.lowercase()}_flag"
        val flagResourceId = remember(question.country.twoLetterCode) {
            context.resources.getIdentifier(flagResourceName, "drawable", context.packageName)
        }
        if (flagResourceId != 0) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(flagResourceId),
                contentDescription = question.country.name,
                modifier = Modifier
                    .size(120.dp, 80.dp)
                    .align(Alignment.CenterHorizontally),
            )
        } else {
            Text(
                text = flagEmoji(question.country.twoLetterCode),
                fontSize = 64.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
            )
        }
    }

    question.options.forEachIndexed { index, option ->
        val answered = selectedIndex != null
        val isSelected = selectedIndex == index
        val isCorrectOption = index == question.correctOptionIndex
        val showResult = answered && (isSelected || isCorrectOption)

        val answerDescription = stringResource(R.string.quiz_answer_option, option)

        OutlinedButton(
            onClick = { if (!answered) onAnswer(index) },
            enabled = !answered,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = answerDescription },
        ) {
            if (showResult) {
                Icon(
                    imageVector = if (isCorrectOption) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (isCorrectOption) {
                        stringResource(R.string.quiz_answer_correct)
                    } else if (isSelected) {
                        stringResource(R.string.quiz_answer_incorrect)
                    } else {
                        null
                    },
                    tint = if (isCorrectOption) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = option,
                fontWeight = if (showResult) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }

    if (selectedIndex != null && lastAnswerCorrect != null) {
        val correctAnswer = question.options[question.correctOptionIndex]
        Text(
            text = if (lastAnswerCorrect) {
                stringResource(R.string.quiz_correct)
            } else {
                stringResource(R.string.quiz_wrong, correctAnswer)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = if (lastAnswerCorrect) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        )
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.quiz_next))
        }
    }

    OutlinedButton(onClick = onChangeMode, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.quiz_change_mode))
    }
}

private fun flagEmoji(countryCode: String): String = try {
    val code = countryCode.uppercase()
    val first = Character.codePointAt(code, 0) - 0x41 + 0x1F1E6
    val second = Character.codePointAt(code, 1) - 0x41 + 0x1F1E6
    String(Character.toChars(first)) + String(Character.toChars(second))
} catch (_: Exception) {
    "🏳️"
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun QuizModePickerPreview() {
    WorldCountriesTheme {
        QuizScreen(
            state = QuizContract.State(),
            onIntent = {},
        )
    }
}
