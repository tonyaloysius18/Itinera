package com.itinera.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * OpenStreetMap (osmdroid) trip view: a pin per city, a line per leg, camera framed
 * to fit all points. Keyless and account-free; "© OpenStreetMap contributors"
 * attribution is drawn by the CopyrightOverlay (required by OSM's tile policy,
 * along with the app-specific user agent set below).
 *
 * (A Google Maps implementation of this same contract exists in the project notes —
 * swapping back is dependency + manifest-key + this one file.)
 */
@Composable
actual fun TripMapView(
    markers: List<MapMarker>,
    routes: List<MapRoute>,
    modifier: Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // OSM tile policy: identify the app (default user agents get blocked)
            Configuration.getInstance().userAgentValue = ctx.packageName

            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                overlays.add(CopyrightOverlay(ctx))

                markers.forEach { m ->
                    overlays.add(Marker(this).apply {
                        position = GeoPoint(m.lat, m.lng)
                        title = m.label
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    })
                }

                routes.forEach { r ->
                    overlays.add(Polyline(this).apply {
                        setPoints(listOf(GeoPoint(r.fromLat, r.fromLng), GeoPoint(r.toLat, r.toLng)))
                        outlinePaint.color = android.graphics.Color.parseColor("#2D7FF9")
                        outlinePaint.strokeWidth = 8f
                    })
                }

                // frame all points after layout (bounding box needs a measured view)
                if (markers.isNotEmpty()) {
                    post {
                        if (markers.size == 1) {
                            controller.setZoom(9.0)
                            controller.setCenter(GeoPoint(markers[0].lat, markers[0].lng))
                        } else {
                            val box = BoundingBox.fromGeoPointsSafe(
                                markers.map { GeoPoint(it.lat, it.lng) },
                            )
                            zoomToBoundingBox(box.increaseByScale(1.4f), false, 60)
                        }
                    }
                }
            }
        },
    )
}