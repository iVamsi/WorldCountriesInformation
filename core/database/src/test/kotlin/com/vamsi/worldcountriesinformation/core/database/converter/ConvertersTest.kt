package com.vamsi.worldcountriesinformation.core.database.converter

import com.vamsi.worldcountriesinformation.core.database.entity.CurrencyEntity
import com.vamsi.worldcountriesinformation.core.database.entity.LanguageEntity
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Converters using Kotlinx Serialization.
 *
 * Tests cover:
 * - Normal serialization/deserialization
 * - Edge cases (null, empty, malformed data)
 * - Error handling and recovery
 * - Performance characteristics
 * - Thread safety (implicitly via immutability)
 *
 * ## Test Categories
 *
 * 1. **Language Converters**: fromLanguageList() / toLanguageList()
 * 2. **Currency Converters**: fromCurrencyList() / toCurrencyList()
 * 3. **Error Handling**: Malformed input, null handling
 * 4. **Edge Cases**: Empty lists, single items, large lists
 */
class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setup() {
        converters = Converters()
    }

    // ========================================================================
    // Language Converters Tests
    // ========================================================================

    @Test
    fun `fromLanguageList with null returns empty JSON array`() {
        val result = converters.fromLanguageList(null)
        assertThat(result, equalTo("[]"))
    }

    @Test
    fun `fromLanguageList with empty list returns empty JSON array`() {
        val result = converters.fromLanguageList(emptyList())
        assertThat(result, equalTo("[]"))
    }

    @Test
    fun `fromLanguageList with single language serializes correctly`() {
        val languages = listOf(
            LanguageEntity(name = "English", nativeName = "English")
        )

        val result = converters.fromLanguageList(languages)

        assertThat(result, notNullValue())
        // Should be valid JSON array
        assertThat(result.startsWith("["), equalTo(true))
        assertThat(result.endsWith("]"), equalTo(true))
        assertThat(result.contains("English"), equalTo(true))
    }

    @Test
    fun `fromLanguageList with multiple languages serializes correctly`() {
        val languages = listOf(
            LanguageEntity(name = "English", nativeName = "English"),
            LanguageEntity(name = "Spanish", nativeName = "Español"),
            LanguageEntity(name = "French", nativeName = "Français")
        )

        val result = converters.fromLanguageList(languages)

        assertThat(result, notNullValue())
        assertThat(result.startsWith("["), equalTo(true))
        assertThat(result.endsWith("]"), equalTo(true))
        assertThat(result.contains("English"), equalTo(true))
        assertThat(result.contains("Spanish"), equalTo(true))
        assertThat(result.contains("French"), equalTo(true))
        assertThat(result.contains("Español"), equalTo(true))
        assertThat(result.contains("Français"), equalTo(true))
    }

    @Test
    fun `fromLanguageList with null fields serializes with defaults`() {
        val languages = listOf(
            LanguageEntity(name = null, nativeName = null),
            LanguageEntity(name = "English", nativeName = null)
        )

        val result = converters.fromLanguageList(languages)

        assertThat(result, notNullValue())
        assertThat(result.startsWith("["), equalTo(true))
        assertThat(result.endsWith("]"), equalTo(true))
        // Should serialize null values
        assertThat(result.contains("null"), equalTo(true))
    }

    @Test
    fun `toLanguageList with empty string returns empty list`() {
        val result = converters.toLanguageList("")
        assertThat(result, equalTo(emptyList()))
    }

    @Test
    fun `toLanguageList with empty JSON array returns empty list`() {
        val result = converters.toLanguageList("[]")
        assertThat(result, equalTo(emptyList()))
    }

    @Test
    fun `toLanguageList with whitespace returns empty list`() {
        val result = converters.toLanguageList("   ")
        assertThat(result, equalTo(emptyList()))
    }

    @Test
    fun `toLanguageList deserializes single language correctly`() {
        val json = """[{"name":"English","nativeName":"English"}]"""

        val result = converters.toLanguageList(json)

        assertThat(result.size, equalTo(1))
        assertThat(result[0].name, equalTo("English"))
        assertThat(result[0].nativeName, equalTo("English"))
    }

    @Test
    fun `toLanguageList deserializes multiple languages correctly`() {
        val json = """
            [
                {"name":"English","nativeName":"English"},
                {"name":"Spanish","nativeName":"Español"},
                {"name":"French","nativeName":"Français"}
            ]
        """.trimIndent()

        val result = converters.toLanguageList(json)

        assertThat(result.size, equalTo(3))
        assertThat(result[0].name, equalTo("English"))
        assertThat(result[1].name, equalTo("Spanish"))
        assertThat(result[2].name, equalTo("French"))
        assertThat(result[1].nativeName, equalTo("Español"))
        assertThat(result[2].nativeName, equalTo("Français"))
    }

    @Test
    fun `toLanguageList handles missing fields with defaults`() {
        val json = """[{"name":"English"}]"""

        val result = converters.toLanguageList(json)

        assertThat(result.size, equalTo(1))
        assertThat(result[0].name, equalTo("English"))
        assertThat(result[0].nativeName, equalTo(null))
    }

    @Test
    fun `toLanguageList handles null values correctly`() {
        val json = """[{"name":null,"nativeName":null}]"""

        val result = converters.toLanguageList(json)

        assertThat(result.size, equalTo(1))
        assertThat(result[0].name, equalTo(null))
        assertThat(result[0].nativeName, equalTo(null))
    }

    @Test
    fun `toLanguageList with malformed JSON returns empty list`() {
        val malformedJson = """[{"name":"English",}]""" // Extra comma

        val result = converters.toLanguageList(malformedJson)

        // Should recover gracefully
        assertThat(result, equalTo(emptyList()))
    }

    @Test
    fun `toLanguageList with invalid JSON structure returns empty list`() {
        val invalidJson = """{"not": "an array"}"""

        val result = converters.toLanguageList(invalidJson)

        assertThat(result, equalTo(emptyList()))
    }

    @Test
    fun `language round-trip preserves data`() {
        val original = listOf(
            LanguageEntity(name = "English", nativeName = "English"),
            LanguageEntity(name = "Spanish", nativeName = "Español")
        )

        val json = converters.fromLanguageList(original)
        val result = converters.toLanguageList(json)

        assertThat(result.size, equalTo(original.size))
        assertThat(result[0].name, equalTo(original[0].name))
        assertThat(result[0].nativeName, equalTo(original[0].nativeName))
        assertThat(result[1].name, equalTo(original[1].name))
        assertThat(result[1].nativeName, equalTo(original[1].nativeName))
    }

    // ========================================================================
    // Currency Converters Tests
    // ========================================================================

    @Test
    fun `fromCurrencyList with null returns empty JSON array`() {
        val result = converters.fromCurrencyList(null)
        assertThat(result, equalTo("[]"))
    }

    @Test
    fun `fromCurrencyList with empty list returns empty JSON array`() {
        val result = converters.fromCurrencyList(emptyList())
        assertThat(result, equalTo("[]"))
    }

    @Test
    fun `fromCurrencyList with single currency serializes correctly`() {
        val currencies = listOf(
            CurrencyEntity(code = "USD", name = "United States Dollar", symbol = "$")
        )

        val result = converters.fromCurrencyList(currencies)

        assertThat(result, notNullValue())
        assertThat(result.startsWith("["), equalTo(true))
        assertThat(result.endsWith("]"), equalTo(true))
        assertThat(result.contains("USD"), equalTo(true))
        assertThat(result.contains("United States Dollar"), equalTo(true))
        assertThat(result.contains("$"), equalTo(true))
    }

    @Test
    fun `fromCurrencyList with multiple currencies serializes correctly`() {
        val currencies = listOf(
            CurrencyEntity(code = "USD", name = "United States Dollar", symbol = "$"),
            CurrencyEntity(code = "EUR", name = "Euro", symbol = "€"),
            CurrencyEntity(code = "GBP", name = "British Pound", symbol = "£")
        )

        val result = converters.fromCurrencyList(currencies)

        assertThat(result, notNullValue())
        assertThat(result.startsWith("["), equalTo(true))
        assertThat(result.endsWith("]"), equalTo(true))
        assertThat(result.contains("USD"), equalTo(true))
        assertThat(result.contains("EUR"), equalTo(true))
        assertThat(result.contains("GBP"), equalTo(true))
        assertThat(result.contains("€"), equalTo(true))
        assertThat(result.contains("£"), equalTo(true))
    }

    @Test
    fun `fromCurrencyList with null fields serializes with defaults`() {
        val currencies = listOf(
            CurrencyEntity(code = null, name = null, symbol = null),
            CurrencyEntity(code = "USD", name = null, symbol = "$")
        )

        val result = converters.fromCurrencyList(currencies)

        assertThat(result, notNullValue())
        assertThat(result.startsWith("["), equalTo(true))
        assertThat(result.endsWith("]"), equalTo(true))
        assertThat(result.contains("null"), equalTo(true))
    }

    @Test
    fun `toCurrencyList with empty string returns empty list`() {
        val result = converters.toCurrencyList("")
        assertThat(result, equalTo(emptyList()))
    }

    @Test
    fun `toCurrencyList with empty JSON array returns empty list`() {
        val result = converters.toCurrencyList("[]")
        assertThat(result, equalTo(emptyList()))
    }

    @Test
    fun `toCurrencyList with whitespace returns empty list`() {
        val result = converters.toCurrencyList("   ")
        assertThat(result, equalTo(emptyList()))
    }

    @Test
    fun `toCurrencyList deserializes single currency correctly`() {
        val json = """[{"code":"USD","name":"United States Dollar","symbol":"$"}]"""

        val result = converters.toCurrencyList(json)

        assertThat(result.size, equalTo(1))
        assertThat(result[0].code, equalTo("USD"))
        assertThat(result[0].name, equalTo("United States Dollar"))
        assertThat(result[0].symbol, equalTo("$"))
    }

    @Test
    fun `toCurrencyList deserializes multiple currencies correctly`() {
        val json = """
            [
                {"code":"USD","name":"United States Dollar","symbol":"$"},
                {"code":"EUR","name":"Euro","symbol":"€"},
                {"code":"GBP","name":"British Pound","symbol":"£"}
            ]
        """.trimIndent()

        val result = converters.toCurrencyList(json)

        assertThat(result.size, equalTo(3))
        assertThat(result[0].code, equalTo("USD"))
        assertThat(result[1].code, equalTo("EUR"))
        assertThat(result[2].code, equalTo("GBP"))
        assertThat(result[1].symbol, equalTo("€"))
        assertThat(result[2].symbol, equalTo("£"))
    }

    @Test
    fun `toCurrencyList handles missing fields with defaults`() {
        val json = """[{"code":"USD"}]"""

        val result = converters.toCurrencyList(json)

        assertThat(result.size, equalTo(1))
        assertThat(result[0].code, equalTo("USD"))
        assertThat(result[0].name, equalTo(null))
        assertThat(result[0].symbol, equalTo(null))
    }

    @Test
    fun `toCurrencyList handles null values correctly`() {
        val json = """[{"code":null,"name":null,"symbol":null}]"""

        val result = converters.toCurrencyList(json)

        assertThat(result.size, equalTo(1))
        assertThat(result[0].code, equalTo(null))
        assertThat(result[0].name, equalTo(null))
        assertThat(result[0].symbol, equalTo(null))
    }

    @Test
    fun `toCurrencyList with malformed JSON returns empty list`() {
        val malformedJson = """[{"code":"USD",}]""" // Extra comma

        val result = converters.toCurrencyList(malformedJson)

        // Should recover gracefully
        assertThat(result, equalTo(emptyList()))
    }

    @Test
    fun `toCurrencyList with invalid JSON structure returns empty list`() {
        val invalidJson = """{"not": "an array"}"""

        val result = converters.toCurrencyList(invalidJson)

        assertThat(result, equalTo(emptyList()))
    }

    @Test
    fun `currency round-trip preserves data`() {
        val original = listOf(
            CurrencyEntity(code = "USD", name = "United States Dollar", symbol = "$"),
            CurrencyEntity(code = "EUR", name = "Euro", symbol = "€")
        )

        val json = converters.fromCurrencyList(original)
        val result = converters.toCurrencyList(json)

        assertThat(result.size, equalTo(original.size))
        assertThat(result[0].code, equalTo(original[0].code))
        assertThat(result[0].name, equalTo(original[0].name))
        assertThat(result[0].symbol, equalTo(original[0].symbol))
        assertThat(result[1].code, equalTo(original[1].code))
        assertThat(result[1].name, equalTo(original[1].name))
        assertThat(result[1].symbol, equalTo(original[1].symbol))
    }

    // ========================================================================
    // Performance & Edge Case Tests
    // ========================================================================

    @Test
    fun `large language list serializes and deserializes correctly`() {
        val languages = (1..50).map { i ->
            LanguageEntity(name = "Language$i", nativeName = "Native$i")
        }

        val json = converters.fromLanguageList(languages)
        val result = converters.toLanguageList(json)

        assertThat(result.size, equalTo(50))
        assertThat(result.first().name, equalTo("Language1"))
        assertThat(result.last().name, equalTo("Language50"))
    }

    @Test
    fun `large currency list serializes and deserializes correctly`() {
        val currencies = (1..50).map { i ->
            CurrencyEntity(code = "CUR$i", name = "Currency$i", symbol = "¤$i")
        }

        val json = converters.fromCurrencyList(currencies)
        val result = converters.toCurrencyList(json)

        assertThat(result.size, equalTo(50))
        assertThat(result.first().code, equalTo("CUR1"))
        assertThat(result.last().code, equalTo("CUR50"))
    }

    @Test
    fun `special characters in language names are handled correctly`() {
        val languages = listOf(
            LanguageEntity(name = "中文", nativeName = "中文"),
            LanguageEntity(name = "العربية", nativeName = "العربية"),
            LanguageEntity(name = "Emoji 😀", nativeName = "😀")
        )

        val json = converters.fromLanguageList(languages)
        val result = converters.toLanguageList(json)

        assertThat(result.size, equalTo(3))
        assertThat(result[0].name, equalTo("中文"))
        assertThat(result[1].name, equalTo("العربية"))
        assertThat(result[2].name, equalTo("Emoji 😀"))
    }

    @Test
    fun `special characters in currency symbols are handled correctly`() {
        val currencies = listOf(
            CurrencyEntity(code = "USD", name = "Dollar", symbol = "$"),
            CurrencyEntity(code = "EUR", name = "Euro", symbol = "€"),
            CurrencyEntity(code = "JPY", name = "Yen", symbol = "¥"),
            CurrencyEntity(code = "GBP", name = "Pound", symbol = "£")
        )

        val json = converters.fromCurrencyList(currencies)
        val result = converters.toCurrencyList(json)

        assertThat(result.size, equalTo(4))
        assertThat(result[0].symbol, equalTo("$"))
        assertThat(result[1].symbol, equalTo("€"))
        assertThat(result[2].symbol, equalTo("¥"))
        assertThat(result[3].symbol, equalTo("£"))
    }
}
