package com.vamsi.worldcountriesinformation.feature.quiz

import androidx.lifecycle.viewModelScope
import com.vamsi.worldcountriesinformation.core.common.error.AppError
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIViewModel
import com.vamsi.worldcountriesinformation.domain.quiz.GenerateQuizQuestionUseCase
import com.vamsi.worldcountriesinformation.domain.quiz.GuessMode
import com.vamsi.worldcountriesinformation.domain.quiz.ScoreQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val generateQuizQuestionUseCase: GenerateQuizQuestionUseCase,
    private val scoreQuizUseCase: ScoreQuizUseCase,
) : MVIViewModel<QuizContract.Intent, QuizContract.State, QuizContract.Effect>(
    initialState = QuizContract.State(),
) {

    override fun handleIntent(intent: QuizContract.Intent) {
        when (intent) {
            is QuizContract.Intent.SelectMode -> selectMode(intent.mode)
            is QuizContract.Intent.LoadQuestion -> loadQuestion()
            is QuizContract.Intent.AnswerSelected -> submitAnswer(intent.index)
            is QuizContract.Intent.NextQuestion -> loadQuestion()
            is QuizContract.Intent.ClearMode -> setState {
                copy(
                    mode = null,
                    question = null,
                    selectedIndex = null,
                    lastAnswerCorrect = null,
                    error = null,
                )
            }
            is QuizContract.Intent.NavigateBack -> setEffect { QuizContract.Effect.NavigateBack }
        }
    }

    private fun selectMode(mode: GuessMode) {
        setState {
            copy(
                mode = mode,
                question = null,
                selectedIndex = null,
                lastAnswerCorrect = null,
                error = null,
            )
        }
        loadQuestion()
    }

    private fun loadQuestion() {
        val mode = state.value.mode ?: return
        viewModelScope.launch {
            setState {
                copy(
                    isLoading = true,
                    question = null,
                    selectedIndex = null,
                    lastAnswerCorrect = null,
                    error = null,
                )
            }
            try {
                val question = generateQuizQuestionUseCase(mode)
                if (question == null) {
                    setState {
                        copy(
                            isLoading = false,
                            error = AppError.Generic(R.string.quiz_error_load),
                        )
                    }
                } else {
                    setState { copy(isLoading = false, question = question, error = null) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Quiz: failed to load question")
                setState {
                    copy(
                        isLoading = false,
                        error = AppError.Generic(R.string.quiz_error_load),
                    )
                }
            }
        }
    }

    private fun submitAnswer(index: Int) {
        val question = state.value.question ?: return
        if (state.value.selectedIndex != null) return

        val correct = scoreQuizUseCase(question, index)
        setState {
            copy(
                selectedIndex = index,
                lastAnswerCorrect = correct,
                score = score + if (correct) 1 else 0,
                answered = answered + 1,
            )
        }
    }
}
