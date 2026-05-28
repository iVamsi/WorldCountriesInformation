package com.vamsi.worldcountriesinformation.feature.countrydetails.map

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.osmdroid.util.GeoPoint

/**
 * Loads simplified offline border polygons bundled in assets.
 */
class CountryBorderLoader(context: Context) {

    private val borders: Map<String, List<GeoPoint>> by lazy {
        runCatching {
            context.assets.open(ASSET_NAME).bufferedReader().use { reader ->
                Json.decodeFromString<CountryBordersFile>(reader.readText()).borders
                    .mapValues { (_, points) ->
                        points.map { GeoPoint(it.lat, it.lng) }
                    }
            }
        }.getOrDefault(emptyMap())
    }

    fun polygonFor(alpha3Code: String): List<GeoPoint>? {
        return borders[alpha3Code.uppercase()]
    }

    @Serializable
    private data class CountryBordersFile(
        val borders: Map<String, List<BorderPoint>>,
    )

    @Serializable
    private data class BorderPoint(
        val lat: Double,
        val lng: Double,
    )

    companion object {
        private const val ASSET_NAME = "country_borders.json"
    }
}
