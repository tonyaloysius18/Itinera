package com.itinera.app.data

import kotlinx.coroutines.flow.StateFlow

/**
 * Device compass. Emits the heading in degrees (0–360) from magnetic north,
 * where 0 = North, 90 = East, 180 = South, 270 = West.
 *
 * Android: SensorManager + ROTATION_VECTOR (no permission).
 * iOS: CLLocationManager heading updates (requires location-when-in-use permission).
 */
expect class Compass() {
    /** Current heading in degrees (0–360). 0 until [start] delivers a reading. */
    val heading: StateFlow<Float>

    /** True once we've received at least one sensor reading. */
    val available: StateFlow<Boolean>

    /** Begin listening to sensor updates. */
    fun start()

    /** Stop listening (call when leaving the screen to save battery). */
    fun stop()
}

/** "N", "NE", "E", … from a heading in degrees. */
fun cardinal(degrees: Float): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val idx = (((degrees % 360f) + 360f) % 360f / 45f).toInt() % 8
    return dirs[idx]
}