package com.itinera.app

/**
 * Opens a place in the platform's maps app — Google Maps on Android, Apple Maps
 * on iOS (or whichever app has registered for the scheme).
 *
 * [latitude]/[longitude] are optional. When present the pin is exact; when null
 * the maps app searches for [label], which is why the address stored on an
 * Activity is worth passing rather than just its name.
 *
 * Silently does nothing if no maps app can handle it — a dead tap is better
 * than a crash for a convenience action.
 */
expect fun openInMaps(label: String, latitude: Double? = null, longitude: Double? = null)