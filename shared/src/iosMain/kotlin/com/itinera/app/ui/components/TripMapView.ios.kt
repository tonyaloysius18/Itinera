@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.itinera.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.UIKit.UILabel
import platform.UIKit.UITextAlignmentCenter
import platform.UIKit.UIView

private val tripMapJson = Json { encodeDefaults = true }

@Serializable
private data class IosTripMapPayload(
    val markers: List<IosTripMapMarker>,
    val routes: List<IosTripMapRoute>,
)

@Serializable
private data class IosTripMapMarker(
    val lat: Double,
    val lng: Double,
    val label: String,
)

@Serializable
private data class IosTripMapRoute(
    val fromLat: Double,
    val fromLng: Double,
    val toLat: Double,
    val toLng: Double,
)

/**
 * MapLibre lives in the Swift application target. This bridge keeps the shared
 * KMP framework independent of that binary SDK while returning a native UIView.
 */
@Composable
actual fun TripMapView(
    markers: List<MapMarker>,
    routes: List<MapRoute>,
    modifier: Modifier,
) {
    val payload = tripMapJson.encodeToString(
        IosTripMapPayload(
            markers = markers.map { IosTripMapMarker(it.lat, it.lng, it.label) },
            routes = routes.map {
                IosTripMapRoute(it.fromLat, it.fromLng, it.toLat, it.toLng)
            },
        ),
    )

    key(payload) {
        UIKitView(
            factory = {
                IosMapLibreTripMap.provider?.invoke(payload) ?: unavailableMapView()
            },
            modifier = modifier,
        )
    }
}

/** Assigned by iOSApp.swift after MapLibre is configured. */
object IosMapLibreTripMap {
    var provider: ((payloadJson: String) -> UIView)? = null
}

private fun unavailableMapView(): UIView = UILabel().apply {
    text = "The trip map is not configured."
    textAlignment = UITextAlignmentCenter
    numberOfLines = 0
}
