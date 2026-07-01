package com.vamsi.worldcountriesinformation.domain.quiz

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.random.Random

/**
 * Builds a random multiple-choice question from cached countries.
 */
class GenerateQuizQuestionUseCase
@Inject
constructor(
    private val getCountriesUseCase: GetCountriesUseCase,
) {
    private val random: Random = Random.Default

    suspend operator fun invoke(mode: GuessMode): QuizQuestion? {
        val countries = loadCountries() ?: return null
        if (countries.size < QuizQuestion.OPTION_COUNT) return null

        val correctCountry = countries.random(random)
        val wrongPool =
            countries
                .filter { it.threeLetterCode != correctCountry.threeLetterCode }
                .map { optionText(it, mode) }
                .filter { it.isNotBlank() && it != optionText(correctCountry, mode) }
                .distinct()

        if (wrongPool.size < QuizQuestion.OPTION_COUNT - 1) return null

        val correctAnswer = optionText(correctCountry, mode)
        val wrongAnswers = wrongPool.shuffled(random).take(QuizQuestion.OPTION_COUNT - 1)
        val options = (wrongAnswers + correctAnswer).shuffled(random)
        val correctIndex = options.indexOf(correctAnswer)

        return QuizQuestion(
            mode = mode,
            country = correctCountry,
            options = options,
            correctOptionIndex = correctIndex,
        )
    }

    private suspend fun loadCountries(): List<CountrySummary>? {
        val response =
            getCountriesUseCase(CachePolicy.CACHE_ONLY)
                .firstOrNull { it !is ApiResponse.Loading }
        return when (response) {
            is ApiResponse.Success -> response.data
            else -> null
        }
    }

    private fun optionText(
        country: CountrySummary,
        mode: GuessMode,
    ): String = when (mode) {
        GuessMode.FLAG -> country.name
        GuessMode.CAPITAL -> country.capital
        GuessMode.REGION -> country.region
    }
}
