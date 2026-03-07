package com.vamsi.worldcountriesinformation.data.countries.search

import com.vamsi.worldcountriesinformation.domain.search.CountryQueryInterpreter
import com.vamsi.worldcountriesinformation.domain.search.StructuredCountryQuery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FallbackCountryQueryInterpreterTest {

    @Test
    fun `interpret - when llm result unavailable - falls back to rule based interpreter`() = runTest {
        val ruleBasedInterpreter = RuleBasedCountryQueryInterpreter()
        val interpreter = FallbackCountryQueryInterpreter(
            onDeviceLlmCountryQueryInterpreter = object : OnDeviceLlmCountryQueryInterpreter {
                override suspend fun interpretOrNull(query: String): StructuredCountryQuery? = null
            },
            ruleBasedCountryQueryInterpreter = ruleBasedInterpreter,
        )

        val result = interpreter.interpret("country with highest population")

        assertEquals(1, result.limit)
        assertEquals(ruleBasedInterpreter.interpret("country with highest population"), result)
    }

    @Test
    fun `interpret - when llm returns structured query - uses llm result`() = runTest {
        val llmResult = StructuredCountryQuery(textQuery = "tokyo")
        val interpreter = FallbackCountryQueryInterpreter(
            onDeviceLlmCountryQueryInterpreter = object : OnDeviceLlmCountryQueryInterpreter {
                override suspend fun interpretOrNull(query: String): StructuredCountryQuery? = llmResult
            },
            ruleBasedCountryQueryInterpreter = RuleBasedCountryQueryInterpreter(),
        )

        val result = interpreter.interpret("country whose capital is tokyo")

        assertEquals(llmResult, result)
    }

    @Test
    fun `interpret - when llm throws - falls back to rule based interpreter`() = runTest {
        val ruleBasedInterpreter = RuleBasedCountryQueryInterpreter()
        val interpreter = FallbackCountryQueryInterpreter(
            onDeviceLlmCountryQueryInterpreter = object : OnDeviceLlmCountryQueryInterpreter {
                override suspend fun interpretOrNull(query: String): StructuredCountryQuery? {
                    throw IllegalStateException("runtime failure")
                }
            },
            ruleBasedCountryQueryInterpreter = ruleBasedInterpreter,
        )

        val result = interpreter.interpret("country with highest population")

        assertEquals(ruleBasedInterpreter.interpret("country with highest population"), result)
    }
}
