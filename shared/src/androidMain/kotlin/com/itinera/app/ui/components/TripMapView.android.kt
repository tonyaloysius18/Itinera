package com.itinera.app.ui.components

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val OpenFreeMapStyle = "https://tiles.openfreemap.org/styles/liberty"

/**
 * Keyless MapLibre map backed by OpenFreeMap's OpenStreetMap-based vector tiles.
 * MapLibre's built-in attribution control remains enabled for both providers.
 */
@Composable
actual fun TripMapView(
    markers: List<MapMarker>,
    routes: List<MapRoute>,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply { onCreate(null) }
    }

    DisposableEffect(mapView, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView, markers, routes) {
        mapView.getMapAsync { map ->
            map.setStyle(Style.Builder().fromUri(OpenFreeMapStyle)) {
                map.clear()
                markers.forEach { marker ->
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(marker.lat, marker.lng))
                            .title(marker.label),
                    )
                }
                routes.forEach { route ->
                    map.addPolyline(
                        PolylineOptions()
                            .add(
                                LatLng(route.fromLat, route.fromLng),
                                LatLng(route.toLat, route.toLng),
                            )
                            .color(Color.rgb(45, 127, 249))
                            .width(5f),
                    )
                }

                map.uiSettings.isAttributionEnabled = true
                map.uiSettings.isLogoEnabled = true
                mapView.post { frameTrip(map, markers) }
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}

private fun frameTrip(
    map: org.maplibre.android.maps.MapLibreMap,
    markers: List<MapMarker>,
) {
    when (markers.size) {
        0 -> Unit
        1 -> map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(markers.first().lat, markers.first().lng),
                9.0,
            ),
        )
        else -> {
            val bounds = LatLngBounds.Builder()
                .includes(markers.map { LatLng(it.lat, it.lng) })
                .build()
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80))
        }
    }
}
