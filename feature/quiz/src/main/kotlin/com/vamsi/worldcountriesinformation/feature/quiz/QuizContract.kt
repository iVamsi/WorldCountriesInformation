package com.vamsi.worldcountriesinformation.feature.quiz

import com.vamsi.worldcountriesinformation.core.common.error.AppError
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIEffect
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIIntent
import com.vamsi.worldcountriesinformation.core.common.mvi.MVIState
import com.vamsi.worldcountriesinformation.domain.quiz.GuessMode
import com.vamsi.worldcountriesinformation.domain.quiz.QuizQuestion

object QuizContract {

    sealed interface Intent : MVIIntent {
        data class SelectMode(val mode: GuessMode) : Intent
        data object LoadQuestion : Intent
        data class AnswerSelected(val index: Int) : Intent
        data object NextQuestion : Intent
        data object ClearMode : Intent
        data object NavigateBack : Intent
    }

    data class State(
        val mode: GuessMode? = null,
        val question: QuizQuestion? = null,
        val isLoading: Boolean = false,
        val score: Int = 0,
        val answered: Int = 0,
        val currentStreak: Int = 0,
        val highScore: Int = 0,
        val bestStreak: Int = 0,
        val selectedIndex: Int? = null,
        val lastAnswerCorrect: Boolean? = null,
        val error: AppError? = null,
    ) : MVIState {
        val showModePicker: Boolean get() = mode == null && !isLoading
        val showQuestion: Boolean get() = mode != null && question != null && !isLoading
        val showError: Boolean get() = error != null && !isLoading && question == null
        val hasAnswered: Boolean get() = selectedIndex != null
    }

    sealed interface Effect : MVIEffect {
        data object NavigateBack : Effect
    }
}
