package com.vamsi.worldcountriesinformation.feature.countrydetails.map

import android.graphics.Paint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

/**
 * Draws a simplified rectangular border around a country pin when GeoJSON is unavailable.
 * Uses lat/lng bounds derived from center point for a lightweight offline overlay.
 */
object CountryBorderOverlay {

    private const val DELTA = 3.0

    fun applyApproximateBorder(mapView: MapView, latitude: Double, longitude: Double) {
        val points = listOf(
            GeoPoint(latitude + DELTA, longitude - DELTA),
            GeoPoint(latitude + DELTA, longitude + DELTA),
            GeoPoint(latitude - DELTA, longitude + DELTA),
            GeoPoint(latitude - DELTA, longitude - DELTA),
        )
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

    private const val BORDER_TAG = "country_border"
}
