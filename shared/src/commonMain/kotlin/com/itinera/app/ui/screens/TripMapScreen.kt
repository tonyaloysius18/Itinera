package com.itinera.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itinera.app.model.Trip
import com.itinera.app.ui.components.MapMarker
import com.itinera.app.ui.components.MapRoute
import com.itinera.app.ui.components.TopBar
import com.itinera.app.ui.components.TripMapView

/**
 * Full-screen map of the trip: one pin per distinct city (with coordinates),
 * one line per leg whose two endpoints are geocoded. Legs not yet geocoded are
 * skipped; onBackfill re-requests their coordinates when the screen opens.
 */
@Composable
fun TripMapScreen(
    trip: Trip,
    onBack: () -> Unit,
    onBackfill: () -> Unit = {},
) {
    // ask the repository to fill any missing coordinates (no-op when all present)
    LaunchedEffect(trip.id) { onBackfill() }

    val hasCoords = { lat: Double, lng: Double -> lat != 0.0 || lng != 0.0 }

    val markers = remember(trip.legs) {
        buildList {
            for (leg in trip.legs) {
                if (hasCoords(leg.fromLat, leg.fromLng)) add(MapMarker(leg.fromLat, leg.fromLng, leg.fromCity))
                if (hasCoords(leg.toLat, leg.toLng)) add(MapMarker(leg.toLat, leg.toLng, leg.toCity))
            }
        }.distinctBy { it.label.trim().lowercase() }
    }

    val routes = remember(trip.legs) {
        trip.legs
            .filter { hasCoords(it.fromLat, it.fromLng) && hasCoords(it.toLat, it.toLng) }
            .map { MapRoute(it.fromLat, it.fromLng, it.toLat, it.toLng) }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(trip.title, onBack = onBack)

        if (markers.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Locating your cities…\nAdd legs (online) to see them on the map.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                )
            }
        } else {
            TripMapView(
                markers = markers,
                routes = routes,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}