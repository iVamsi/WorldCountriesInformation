package com.vamsi.worldcountriesinformation.core.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.CountryDetailsModel
import com.vamsi.worldcountriesinformation.domainmodel.Language
import timber.log.Timber
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface CountrySummaryGenerator {
    suspend fun generateSummary(details: List<CountryDetailsModel>): String?
}

@Singleton
class CountrySummaryGeneratorImpl @Inject constructor(
    private val capabilityChecker: AiCapabilityChecker,
) : CountrySummaryGenerator {

    private val generativeModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = AiCapabilityChecker.ON_DEVICE_MODEL_NAME,
            apiKey = AiCapabilityChecker.LOCAL_ONLY_API_KEY,
            generationConfig = generationConfig {
                maxOutputTokens = 256
                temperature = 0.4f
            },
        )
    }

    override suspend fun generateSummary(details: List<CountryDetailsModel>): String? {
        val fields = details.associateBy({ it.key.lowercase(Locale.US) }, { it.value })
        val name = fields["name"].orEmpty()
        if (name.isBlank()) {
            return null
        }

        val capital = fields["capital"].orEmpty()
        val region = fields["region"].orEmpty()
        val population = fields["population"]?.toIntOrNull() ?: 0
        val languages = fields["languages"].orEmpty()

        val templateSummary = buildTemplateSummary(
            name = name,
            capital = capital,
            region = region,
            population = population,
            languages = languages,
        )

        if (!capabilityChecker.isOnDeviceGenerationAvailable()) {
            return templateSummary
        }

        val prompt = buildPrompt(
            name = name,
            capital = capital,
            region = region,
            population = population,
            languages = languages,
        )

        return runCatching {
            generativeModel.generateContent(prompt).text
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        }.onFailure { error ->
            Timber.w(error, "On-device summary generation failed; using template fallback")
        }.getOrNull() ?: templateSummary
    }

    private fun buildPrompt(
        name: String,
        capital: String,
        region: String,
        population: Int,
        languages: String,
    ): String = buildString {
        appendLine("Write one concise paragraph about this country for a travel reference app.")
        appendLine("Use only the facts below. Do not invent details.")
        appendLine()
        appendLine("Name: $name")
        appendLine("Capital: ${capital.ifEmpty { "N/A" }}")
        appendLine("Region: ${region.ifEmpty { "N/A" }}")
        appendLine("Population: ${formatPopulation(population)}")
        appendLine("Languages: ${languages.ifEmpty { "N/A" }}")
    }

    private fun buildTemplateSummary(
        name: String,
        capital: String,
        region: String,
        population: Int,
        languages: String,
    ): String = buildString {
        append(name)
        if (region.isNotEmpty()) {
            append(" is located in ")
            append(region)
        }
        append(".")
        if (capital.isNotEmpty()) {
            append(" Its capital is ")
            append(capital)
            append(".")
        }
        if (population > 0) {
            append(" The population is about ")
            append(formatPopulation(population))
            append(".")
        }
        if (languages.isNotEmpty()) {
            append(" Common languages include ")
            append(languages)
            append(".")
        }
    }

    private fun formatPopulation(population: Int): String = if (population > 0) {
        NumberFormat.getNumberInstance(Locale.US).format(population)
    } else {
        "N/A"
    }
}

fun Country.toSummaryDetails(): List<CountryDetailsModel> {
    val languages = languages
        .mapNotNull(Language::name)
        .filter { it.isNotBlank() }
        .joinToString(", ")

    return listOf(
        CountryDetailsModel(key = "name", value = name),
        CountryDetailsModel(key = "capital", value = capital),
        CountryDetailsModel(key = "region", value = region),
        CountryDetailsModel(key = "population", value = population.toString()),
        CountryDetailsModel(key = "languages", value = languages),
    )
}
