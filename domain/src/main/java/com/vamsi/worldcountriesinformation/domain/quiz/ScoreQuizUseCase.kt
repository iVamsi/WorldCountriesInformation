package com.vamsi.worldcountriesinformation.domain.quiz

import javax.inject.Inject

/**
 * Returns whether the selected option index matches the correct answer.
 */
class ScoreQuizUseCase
@Inject
constructor() {
    operator fun invoke(
        question: QuizQuestion,
        selectedIndex: Int,
    ): Boolean = selectedIndex == question.correctOptionIndex
}
