package com.vamsi.worldcountriesinformation.feature.quiz

import app.cash.turbine.test
import com.vamsi.worldcountriesinformation.core.datastore.QuizStats
import com.vamsi.worldcountriesinformation.core.datastore.QuizStatsDataSource
import com.vamsi.worldcountriesinformation.domain.quiz.GenerateQuizQuestionUseCase
import com.vamsi.worldcountriesinformation.domain.quiz.GuessMode
import com.vamsi.worldcountriesinformation.domain.quiz.QuizQuestion
import com.vamsi.worldcountriesinformation.domain.quiz.ScoreQuizUseCase
import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var generateQuizQuestionUseCase: GenerateQuizQuestionUseCase
    private lateinit var scoreQuizUseCase: ScoreQuizUseCase
    private lateinit var quizStatsDataSource: QuizStatsDataSource
    private lateinit var viewModel: QuizViewModel

    private val statsFlow = MutableStateFlow(QuizStats(highScore = 5, bestStreak = 3))

    private val sampleQuestion = QuizQuestion(
        mode = GuessMode.CAPITAL,
        country = CountrySummary(
            name = "India",
            capital = "New Delhi",
            region = "Asia",
            population = 1_000_000,
            twoLetterCode = "IN",
            threeLetterCode = "IND",
            latitude = 20.0,
            longitude = 77.0,
        ),
        options = listOf("New Delhi", "Tokyo", "Paris", "Cairo"),
        correctOptionIndex = 0,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        generateQuizQuestionUseCase = mockk()
        scoreQuizUseCase = ScoreQuizUseCase()
        quizStatsDataSource = mockk(relaxed = true)
        every { quizStatsDataSource.quizStats } returns statsFlow
        coEvery { quizStatsDataSource.recordSessionResult(any(), any()) } just Runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): QuizViewModel = QuizViewModel(
        generateQuizQuestionUseCase = generateQuizQuestionUseCase,
        scoreQuizUseCase = scoreQuizUseCase,
        quizStatsDataSource = quizStatsDataSource,
    )

    @Test
    fun `stats from datastore update high score and streak`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(5, viewModel.state.value.highScore)
        assertEquals(3, viewModel.state.value.bestStreak)
    }

    @Test
    fun `select mode loads question`() = runTest {
        coEvery { generateQuizQuestionUseCase(GuessMode.CAPITAL) } returns sampleQuestion

        viewModel = createViewModel()
        viewModel.processIntent(QuizContract.Intent.SelectMode(GuessMode.CAPITAL))
        advanceUntilIdle()

        assertEquals(GuessMode.CAPITAL, viewModel.state.value.mode)
        assertNotNull(viewModel.state.value.question)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `correct answer increments score and streak`() = runTest {
        coEvery { generateQuizQuestionUseCase(GuessMode.CAPITAL) } returns sampleQuestion

        viewModel = createViewModel()
        viewModel.processIntent(QuizContract.Intent.SelectMode(GuessMode.CAPITAL))
        advanceUntilIdle()
        viewModel.processIntent(QuizContract.Intent.AnswerSelected(0))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.score)
        assertEquals(1, viewModel.state.value.currentStreak)
        assertTrue(viewModel.state.value.lastAnswerCorrect == true)
    }

    @Test
    fun `wrong answer resets streak`() = runTest {
        coEvery { generateQuizQuestionUseCase(GuessMode.CAPITAL) } returns sampleQuestion

        viewModel = createViewModel()
        viewModel.processIntent(QuizContract.Intent.SelectMode(GuessMode.CAPITAL))
        advanceUntilIdle()
        viewModel.processIntent(QuizContract.Intent.AnswerSelected(2))
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.score)
        assertEquals(0, viewModel.state.value.currentStreak)
        assertTrue(viewModel.state.value.lastAnswerCorrect == false)
    }

    @Test
    fun `navigate back emits effect`() = runTest {
        viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.processIntent(QuizContract.Intent.NavigateBack)
            assertEquals(QuizContract.Effect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `clear mode resets quiz state`() = runTest {
        coEvery { generateQuizQuestionUseCase(GuessMode.CAPITAL) } returns sampleQuestion

        viewModel = createViewModel()
        viewModel.processIntent(QuizContract.Intent.SelectMode(GuessMode.CAPITAL))
        advanceUntilIdle()
        viewModel.processIntent(QuizContract.Intent.ClearMode)
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.mode)
        assertEquals(null, viewModel.state.value.question)
        assertEquals(0, viewModel.state.value.score)
    }
}
