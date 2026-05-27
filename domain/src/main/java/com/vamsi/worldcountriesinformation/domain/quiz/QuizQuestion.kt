package com.vamsi.worldcountriesinformation.domain.quiz

import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary

/**
 * A single multiple-choice quiz round.
 */
data class QuizQuestion(
    val mode: GuessMode,
    val country: CountrySummary,
    val options: List<String>,
    val correctOptionIndex: Int,
) {
    init {
        require(options.size == OPTION_COUNT) { "Quiz must have $OPTION_COUNT options" }
        require(correctOptionIndex in options.indices) { "correctOptionIndex out of range" }
    }

    companion object {
        const val OPTION_COUNT = 4
    }
}
