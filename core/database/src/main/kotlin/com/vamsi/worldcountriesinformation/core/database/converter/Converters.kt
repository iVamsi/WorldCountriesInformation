package com.vamsi.worldcountriesinformation.core.database.converter

import androidx.room.TypeConverter
import com.vamsi.worldcountriesinformation.core.database.entity.CurrencyEntity
import com.vamsi.worldcountriesinformation.core.database.entity.LanguageEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * Room TypeConverters for complex data types
 * Using simple JSON approach for better compatibility
 */
class Converters {

    // Language List Converters
    @TypeConverter
    fun fromLanguageList(value: List<LanguageEntity>?): String {
        if (value == null) return "[]"
        val jsonArray = JSONArray()
        value.forEach { language ->
            val jsonObject = JSONObject()
            jsonObject.put("name", language.name)
            jsonObject.put("nativeName", language.nativeName)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toLanguageList(value: String): List<LanguageEntity> {
        val list = mutableListOf<LanguageEntity>()
        try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                list.add(
                    LanguageEntity(
                        name = jsonObject.optString("name"),
                        nativeName = jsonObject.optString("nativeName")
                    )
                )
            }
        } catch (e: Exception) {
            // Return empty list on error
        }
        return list
    }

    // Currency List Converters
    @TypeConverter
    fun fromCurrencyList(value: List<CurrencyEntity>?): String {
        if (value == null) return "[]"
        val jsonArray = JSONArray()
        value.forEach { currency ->
            val jsonObject = JSONObject()
            jsonObject.put("code", currency.code)
            jsonObject.put("name", currency.name)
            jsonObject.put("symbol", currency.symbol)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toCurrencyList(value: String): List<CurrencyEntity> {
        val list = mutableListOf<CurrencyEntity>()
        try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                list.add(
                    CurrencyEntity(
                        code = jsonObject.optString("code"),
                        name = jsonObject.optString("name"),
                        symbol = jsonObject.optString("symbol")
                    )
                )
            }
        } catch (e: Exception) {
            // Return empty list on error
        }
        return list
    }
}
