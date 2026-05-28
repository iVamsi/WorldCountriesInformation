package com.vamsi.worldcountriesinformation.feature.countrydetails.map

import android.content.Context
import android.graphics.Paint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

/**
 * Draws country border overlays from bundled GeoJSON-like assets, with a
 * rectangular fallback when no polygon exists for the alpha-3 code.
 */
object CountryBorderOverlay {

    private const val DELTA = 3.0
    private const val BORDER_TAG = "country_border"

    fun applyBorder(
        context: Context,
        mapView: MapView,
        alpha3Code: String,
        latitude: Double,
        longitude: Double,
    ) {
        val loader = CountryBorderLoader(context.applicationContext)
        val points = loader.polygonFor(alpha3Code)
            ?: approximatePoints(latitude, longitude)
        drawPolygon(mapView, points)
    }

    private fun approximatePoints(latitude: Double, longitude: Double): List<GeoPoint> =
        listOf(
            GeoPoint(latitude + DELTA, longitude - DELTA),
            GeoPoint(latitude + DELTA, longitude + DELTA),
            GeoPoint(latitude - DELTA, longitude + DELTA),
            GeoPoint(latitude - DELTA, longitude - DELTA),
        )

    private fun drawPolygon(mapView: MapView, points: List<GeoPoint>) {
        mapView.overlays.removeAll { it is Polygon && it.title == BORDER_TAG }
        val polygon = Polygon().apply {
            title = BORDER_TAG
            setPoints(points)
            fillPaint.color = android.graphics.Color.argb(30, 33, 150, 243)
            outlinePaint.color = android.graphics.Color.argb(180, 33, 150, 243)
            outlinePaint.strokeWidth = 3f
            outlinePaint.style = Paint.Style.STROKE
        }
        mapView.overlays.add(0, polygon)
        mapView.invalidate()
    }
}
