package com.itinera.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** A city pin on the trip map. */
data class MapMarker(
    val lat: Double,
    val lng: Double,
    val label: String,
)

/** A travelled segment drawn as a line between two coordinates. */
data class MapRoute(
    val fromLat: Double,
    val fromLng: Double,
    val toLat: Double,
    val toLng: Double,
)

/**
 * Native MapLibre map on Android and iOS showing the trip's cities and the
 * lines between leg endpoints, automatically framed to fit every point.
 */
@Composable
expect fun TripMapView(
    markers: List<MapMarker>,
    routes: List<MapRoute>,
    modifier: Modifier = Modifier,
)
