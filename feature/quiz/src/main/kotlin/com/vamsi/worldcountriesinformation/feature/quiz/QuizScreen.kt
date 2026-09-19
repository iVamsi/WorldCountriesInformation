@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.vamsi.worldcountriesinformation.feature.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vamsi.worldcountriesinformation.core.common.error.AppError
import com.vamsi.worldcountriesinformation.core.common.error.message
import com.vamsi.worldcountriesinformation.core.common.testing.UiTestTags
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme
import com.vamsi.worldcountriesinformation.core.designsystem.component.ErrorState
import com.vamsi.worldcountriesinformation.core.designsystem.component.FactTile
import com.vamsi.worldcountriesinformation.core.designsystem.component.FlagImage
import com.vamsi.worldcountriesinformation.core.designsystem.component.SectionHeader
import com.vamsi.worldcountriesinformation.domain.quiz.GuessMode
import com.vamsi.worldcountriesinformation.domain.quiz.QuizQuestion
import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
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

@Composable
internal fun QuizScreen(
    state: QuizContract.State,
    onIntent: (QuizContract.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag(UiTestTags.QUIZ_SCREEN),
        containerColor = Color.Transparent,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = CONTENT_MAX_WIDTH)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.answered > 0 || state.mode != null) {
                    ScoreStrip(state = state)
                }

                when {
                    state.isLoading -> LoadingContent()

                    state.showModePicker -> ModePicker(onSelectMode = { onIntent(QuizContract.Intent.SelectMode(it)) })

                    state.showError -> QuizErrorContent(
                        message = state.error?.let { errorMessage(it) }
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
}

@Composable
private fun errorMessage(error: AppError): String = LocalResources.current.message(error)

@Composable
private fun ScoreStrip(state: QuizContract.State, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FactTile(
            label = stringResource(R.string.quiz_stat_score),
            value = stringResource(R.string.quiz_stat_score_value, state.score, state.answered),
            modifier = Modifier.weight(1f),
        )
        FactTile(
            label = stringResource(R.string.quiz_stat_streak),
            value = state.currentStreak.toString(),
            modifier = Modifier.weight(1f),
        )
        FactTile(
            label = stringResource(R.string.quiz_stat_best),
            value = state.highScore.toString(),
            modifier = Modifier.weight(1f),
        )
    }
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
    SectionHeader(text = stringResource(R.string.quiz_pick_mode))
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        ModeRow(
            icon = Icons.Outlined.Flag,
            title = stringResource(R.string.quiz_mode_flag),
            description = stringResource(R.string.quiz_mode_flag_desc),
            onClick = { onSelectMode(GuessMode.FLAG) },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ModeRow(
            icon = Icons.Outlined.LocationCity,
            title = stringResource(R.string.quiz_mode_capital),
            description = stringResource(R.string.quiz_mode_capital_desc),
            onClick = { onSelectMode(GuessMode.CAPITAL) },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ModeRow(
            icon = Icons.Outlined.Public,
            title = stringResource(R.string.quiz_mode_region),
            description = stringResource(R.string.quiz_mode_region_desc),
            onClick = { onSelectMode(GuessMode.REGION) },
        )
    }
}

@Composable
private fun ModeRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(description) },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
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
        TextButton(onClick = onChangeMode, modifier = Modifier.align(Alignment.CenterHorizontally)) {
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
    val prompt = when (question.mode) {
        GuessMode.FLAG -> stringResource(R.string.quiz_prompt_flag)
        GuessMode.CAPITAL -> stringResource(R.string.quiz_prompt_capital, question.country.name)
        GuessMode.REGION -> stringResource(R.string.quiz_prompt_region, question.country.name)
    }

    Text(
        text = prompt,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(top = 8.dp),
    )

    if (question.mode == GuessMode.FLAG) {
        FlagImage(
            twoLetterCode = question.country.twoLetterCode,
            contentDescription = null,
            modifier = Modifier
                .width(QUIZ_FLAG_WIDTH)
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
        )
    }

    val answered = selectedIndex != null
    question.options.forEachIndexed { index, option ->
        AnswerOption(
            text = option,
            answered = answered,
            isSelected = selectedIndex == index,
            isCorrectOption = index == question.correctOptionIndex,
            onClick = { if (!answered) onAnswer(index) },
        )
    }

    if (selectedIndex != null && lastAnswerCorrect != null) {
        val correctAnswer = question.options[question.correctOptionIndex]
        Text(
            text = if (lastAnswerCorrect) {
                stringResource(R.string.quiz_correct)
            } else {
                stringResource(R.string.quiz_wrong, correctAnswer)
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (lastAnswerCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.quiz_next))
        }
    }

    TextButton(onClick = onChangeMode, modifier = Modifier.align(Alignment.CenterHorizontally)) {
        Text(stringResource(R.string.quiz_change_mode))
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun AnswerOption(
    text: String,
    answered: Boolean,
    isSelected: Boolean,
    isCorrectOption: Boolean,
    onClick: () -> Unit,
) {
    val showResult = answered && (isSelected || isCorrectOption)
    val answerDescription = stringResource(R.string.quiz_answer_option, text)
    val colors = when {
        showResult && isCorrectOption -> ButtonDefaults.outlinedButtonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        showResult -> ButtonDefaults.outlinedButtonColors(
            disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
            disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
        )

        else -> ButtonDefaults.outlinedButtonColors(
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    OutlinedButton(
        onClick = onClick,
        enabled = !answered,
        colors = colors,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = ANSWER_MIN_HEIGHT)
            .semantics { contentDescription = answerDescription },
    ) {
        if (showResult) {
            Icon(
                imageVector = if (isCorrectOption) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (isCorrectOption) {
                    stringResource(R.string.quiz_answer_correct)
                } else {
                    stringResource(R.string.quiz_answer_incorrect)
                },
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
}

private val CONTENT_MAX_WIDTH = 560.dp
private val QUIZ_FLAG_WIDTH = 216.dp
private val ANSWER_MIN_HEIGHT = 56.dp

// -----------------------------------------------------------------------------
// Previews
// -----------------------------------------------------------------------------

internal fun previewQuestionState(selectedIndex: Int? = null) = QuizContract.State(
    mode = GuessMode.FLAG,
    score = 3,
    answered = 4,
    currentStreak = 2,
    highScore = 9,
    bestStreak = 5,
    selectedIndex = selectedIndex,
    lastAnswerCorrect = selectedIndex?.let { it == 1 },
    question = QuizQuestion(
        mode = GuessMode.FLAG,
        country = CountrySummary(
            name = "Japan",
            capital = "Tokyo",
            twoLetterCode = "JP",
            threeLetterCode = "JPN",
            population = 125_000_000,
            region = "Asia",
            latitude = 36.0,
            longitude = 138.0,
        ),
        options = listOf("South Korea", "Japan", "Bangladesh", "Palau"),
        correctOptionIndex = 1,
    ),
)

@PreviewLightDark
@Preview(showBackground = true)
@Composable
private fun QuizModePickerPreview() {
    WorldCountriesTheme(dynamicColor = false) {
        QuizScreen(state = QuizContract.State(), onIntent = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizQuestionPreview() {
    WorldCountriesTheme(dynamicColor = false) {
        QuizScreen(state = previewQuestionState(selectedIndex = 0), onIntent = {})
    }
}
