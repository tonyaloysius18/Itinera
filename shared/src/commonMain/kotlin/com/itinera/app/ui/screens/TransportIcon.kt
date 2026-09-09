package com.itinera.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Train
import androidx.compose.ui.graphics.vector.ImageVector
import com.itinera.app.model.TransportType
import com.itinera.app.resources.Res
import com.itinera.app.resources.bus
import com.itinera.app.resources.car
import com.itinera.app.resources.ferry
import com.itinera.app.resources.flight
import com.itinera.app.resources.train
import org.jetbrains.compose.resources.DrawableResource

/** Maps a transport type to its Material icon. */
fun transportIcon(type: TransportType): ImageVector = when (type) {
    TransportType.FLIGHT -> Icons.Filled.Flight
    TransportType.CAR -> Icons.Filled.DirectionsCar
    TransportType.TRAIN -> Icons.Filled.Train
    TransportType.BUS -> Icons.Filled.DirectionsBus
    TransportType.FERRY -> Icons.Filled.DirectionsBoat

}

/** Maps a transport type to its custom image resource. */
fun transportResource(type: TransportType): DrawableResource = when (type) {
    TransportType.FLIGHT -> Res.drawable.flight
    TransportType.CAR -> Res.drawable.car
    TransportType.TRAIN -> Res.drawable.train
    TransportType.BUS -> Res.drawable.bus
    TransportType.FERRY -> Res.drawable.ferry

}
