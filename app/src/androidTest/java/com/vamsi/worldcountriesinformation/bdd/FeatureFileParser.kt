package com.vamsi.worldcountriesinformation.bdd

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Parser for Gherkin .feature files.
 *
 * Reads feature files from assets/features/ directory and parses them
 * into executable test scenarios.
 *
 * Feature file format:
 * ```gherkin
 * Feature: Countries Screen
 *   As a user
 *   I want to see a list of countries
 *   So that I can explore country information
 *
 *   Scenario: Display countries list
 *     Given the app is launched
 *     When the countries screen is displayed
 *     Then I should see the countries list
 *     And I should see the search field
 *
 *   Scenario: Navigate to settings
 *     Given the countries screen is displayed
 *     When I click the settings button
 *     Then I should navigate to settings screen
 * ```
 */
class FeatureFileParser(private val context: Context) {

    /**
     * Parses a feature file from assets.
     *
     * @param fileName Name of the feature file (e.g., "countries.feature")
     * @return Parsed FeatureFile object
     */
    fun parseFeatureFile(fileName: String): FeatureFile {
        val inputStream = context.assets.open("features/$fileName")
        val reader = BufferedReader(InputStreamReader(inputStream))

        var featureName = ""
        var featureDescription = ""
        val scenarios = mutableListOf<ParsedScenario>()

        var currentScenario: ParsedScenario? = null
        var inFeatureDescription = false

        reader.useLines { lines ->
            lines.forEach { line ->
                val trimmedLine = line.trim()

                when {
                    trimmedLine.startsWith("Feature:") -> {
                        featureName = trimmedLine.removePrefix("Feature:").trim()
                        inFeatureDescription = true
                    }

                    trimmedLine.startsWith("Scenario:") || trimmedLine.startsWith("Scenario Outline:") -> {
                        inFeatureDescription = false
                        currentScenario?.let { scenarios.add(it) }
                        val scenarioName = trimmedLine
                            .removePrefix("Scenario:")
                            .removePrefix("Scenario Outline:")
                            .trim()
                        currentScenario = ParsedScenario(scenarioName)
                    }

                    trimmedLine.startsWith("Given ") -> {
                        inFeatureDescription = false
                        currentScenario?.steps?.add(
                            ParsedStep(StepType.GIVEN, trimmedLine.removePrefix("Given ").trim())
                        )
                    }

                    trimmedLine.startsWith("When ") -> {
                        currentScenario?.steps?.add(
                            ParsedStep(StepType.WHEN, trimmedLine.removePrefix("When ").trim())
                        )
                    }

                    trimmedLine.startsWith("Then ") -> {
                        currentScenario?.steps?.add(
                            ParsedStep(StepType.THEN, trimmedLine.removePrefix("Then ").trim())
                        )
                    }

                    trimmedLine.startsWith("And ") -> {
                        currentScenario?.steps?.add(
                            ParsedStep(StepType.AND, trimmedLine.removePrefix("And ").trim())
                        )
                    }

                    trimmedLine.startsWith("But ") -> {
                        currentScenario?.steps?.add(
                            ParsedStep(StepType.AND, trimmedLine.removePrefix("But ").trim())
                        )
                    }

                    inFeatureDescription && trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#") -> {
                        featureDescription += if (featureDescription.isEmpty()) trimmedLine else "\n$trimmedLine"
                    }
                }
            }
        }

        // Add the last scenario
        currentScenario?.let { scenarios.add(it) }

        return FeatureFile(featureName, featureDescription, scenarios)
    }

    /**
     * Lists all feature files in the assets/features directory.
     */
    fun listFeatureFiles(): List<String> {
        return try {
            context.assets.list("features")?.filter { it.endsWith(".feature") } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * Represents a parsed feature file.
 */
data class FeatureFile(
    val name: String,
    val description: String,
    val scenarios: List<ParsedScenario>
)

/**
 * Represents a parsed scenario.
 */
data class ParsedScenario(
    val name: String,
    val steps: MutableList<ParsedStep> = mutableListOf()
)

/**
 * Represents a parsed step.
 */
data class ParsedStep(
    val type: StepType,
    val text: String
)

/**
 * Base class for step definitions.
 * Extend this class to define step implementations.
 */
abstract class StepDefinitions {
    private val stepPatterns = mutableMapOf<Regex, (MatchResult) -> Unit>()

    /**
     * Registers a step pattern with its implementation.
     */
    protected fun step(pattern: String, action: (MatchResult) -> Unit) {
        stepPatterns[Regex(pattern)] = action
    }

    /**
     * Finds and executes a step matching the given text.
     */
    fun executeStep(stepText: String) {
        for ((pattern, action) in stepPatterns) {
            val match = pattern.matchEntire(stepText)
            if (match != null) {
                action(match)
                return
            }
        }
        throw StepNotDefinedException("No step definition found for: $stepText")
    }

    /**
     * Checks if a step is defined.
     */
    fun hasStep(stepText: String): Boolean {
        return stepPatterns.any { (pattern, _) -> pattern.matches(stepText) }
    }
}

class StepNotDefinedException(message: String) : Exception(message)

/**
 * Runner for executing parsed feature files with step definitions.
 */
class FeatureRunner(
    private val stepDefinitions: List<StepDefinitions>
) {
    fun runFeature(featureFile: FeatureFile) {
        println("\nFeature: ${featureFile.name}")
        if (featureFile.description.isNotEmpty()) {
            println("  ${featureFile.description.replace("\n", "\n  ")}")
        }

        featureFile.scenarios.forEach { scenario ->
            runScenario(scenario)
        }
    }

    fun runScenario(scenario: ParsedScenario) {
        println("\n  Scenario: ${scenario.name}")

        scenario.steps.forEach { step ->
            runStep(step)
        }
    }

    private fun runStep(step: ParsedStep) {
        println("    ${step.type.keyword} ${step.text}")

        for (definitions in stepDefinitions) {
            if (definitions.hasStep(step.text)) {
                definitions.executeStep(step.text)
                return
            }
        }

        throw StepNotDefinedException("No step definition found for: ${step.text}")
    }
}
