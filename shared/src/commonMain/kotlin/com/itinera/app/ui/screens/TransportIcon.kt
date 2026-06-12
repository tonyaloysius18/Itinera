package com.itinera.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Train
import androidx.compose.ui.graphics.vector.ImageVector
import com.itinera.app.model.TransportType

/** Maps a transport type to its Material icon. */
fun transportIcon(type: TransportType): ImageVector = when (type) {
    TransportType.FLIGHT -> Icons.Filled.Flight
    TransportType.TRAIN -> Icons.Filled.Train
    TransportType.BUS -> Icons.Filled.DirectionsBus
    TransportType.FERRY -> Icons.Filled.DirectionsBoat
    TransportType.CAR -> Icons.Filled.DirectionsCar
}
