package com.vamsi.worldcountriesinformation.bdd

import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * BDD-style DSL for writing Gherkin-like UI tests.
 *
 * Usage:
 * ```
 * @Test
 * fun userNavigatesToSettings() = Feature("Countries Screen") {
 *     Scenario("User navigates to settings") {
 *         Given("the countries screen is displayed") {
 *             // setup code
 *         }
 *         When("user clicks settings button") {
 *             // action code
 *         }
 *         Then("navigation to settings is triggered") {
 *             // assertion code
 *         }
 *     }
 * }
 * ```
 */

/**
 * Represents a BDD Feature containing multiple scenarios.
 */
class Feature(
    val name: String,
    private val scenarios: MutableList<Scenario> = mutableListOf()
) {
    fun addScenario(scenario: Scenario) {
        scenarios.add(scenario)
    }

    fun execute() {
        scenarios.forEach { scenario ->
            println("Feature: $name")
            scenario.execute()
        }
    }
}

/**
 * Represents a BDD Scenario with Given/When/Then steps.
 */
class Scenario(val name: String) {
    private val steps = mutableListOf<Step>()

    fun addStep(step: Step) {
        steps.add(step)
    }

    fun execute() {
        println("  Scenario: $name")
        steps.forEach { step ->
            println("    ${step.type.keyword} ${step.description}")
            step.action()
        }
    }
}

/**
 * Represents a single step in a scenario.
 */
data class Step(
    val type: StepType,
    val description: String,
    val action: () -> Unit
)

/**
 * Types of BDD steps.
 */
enum class StepType(val keyword: String) {
    GIVEN("Given"),
    WHEN("When"),
    THEN("Then"),
    AND("And")
}

/**
 * DSL builder for creating features.
 */
class FeatureBuilder(private val name: String) {
    private val feature = Feature(name)

    fun Scenario(name: String, block: ScenarioBuilder.() -> Unit) {
        val builder = ScenarioBuilder(name)
        builder.block()
        feature.addScenario(builder.build())
    }

    fun build(): Feature = feature
}

/**
 * DSL builder for creating scenarios.
 */
class ScenarioBuilder(private val name: String) {
    private val scenario = Scenario(name)
    private var lastStepType: StepType = StepType.GIVEN

    fun Given(description: String, action: () -> Unit = {}) {
        lastStepType = StepType.GIVEN
        scenario.addStep(Step(StepType.GIVEN, description, action))
    }

    fun When(description: String, action: () -> Unit = {}) {
        lastStepType = StepType.WHEN
        scenario.addStep(Step(StepType.WHEN, description, action))
    }

    fun Then(description: String, action: () -> Unit = {}) {
        lastStepType = StepType.THEN
        scenario.addStep(Step(StepType.THEN, description, action))
    }

    fun And(description: String, action: () -> Unit = {}) {
        scenario.addStep(Step(StepType.AND, description, action))
    }

    fun build(): Scenario = scenario
}

/**
 * Entry point for creating a feature.
 */
fun Feature(name: String, block: FeatureBuilder.() -> Unit): Feature {
    val builder = FeatureBuilder(name)
    builder.block()
    val feature = builder.build()
    feature.execute()
    return feature
}

/**
 * Extension for ComposeContentTestRule to integrate with BDD DSL.
 */
class ComposeBddContext(
    val composeTestRule: ComposeContentTestRule
) {
    private var currentFeature: String = ""
    private var currentScenario: String = ""

    fun Feature(name: String, block: ComposeBddFeatureBuilder.() -> Unit) {
        currentFeature = name
        println("\nFeature: $name")
        val builder = ComposeBddFeatureBuilder(this)
        builder.block()
    }

    inner class ComposeBddFeatureBuilder(private val context: ComposeBddContext) {

        fun Scenario(name: String, block: ComposeBddScenarioBuilder.() -> Unit) {
            context.currentScenario = name
            println("  Scenario: $name")
            val builder = ComposeBddScenarioBuilder(context)
            builder.block()
        }
    }

    inner class ComposeBddScenarioBuilder(private val context: ComposeBddContext) {

        fun Given(description: String, action: ComposeContentTestRule.() -> Unit) {
            println("    Given $description")
            context.composeTestRule.action()
        }

        fun When(description: String, action: ComposeContentTestRule.() -> Unit) {
            println("    When $description")
            context.composeTestRule.action()
        }

        fun Then(description: String, action: ComposeContentTestRule.() -> Unit) {
            println("    Then $description")
            context.composeTestRule.action()
        }

        fun And(description: String, action: ComposeContentTestRule.() -> Unit) {
            println("    And $description")
            context.composeTestRule.action()
        }
    }
}

/**
 * Creates a BDD context for Compose tests.
 */
fun ComposeContentTestRule.bdd(block: ComposeBddContext.() -> Unit) {
    val context = ComposeBddContext(this)
    context.block()
}
