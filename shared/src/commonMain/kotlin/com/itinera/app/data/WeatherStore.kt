package com.itinera.app.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** A city the user saved on the weather dashboard, with coordinates for fetching. */
@Serializable
data class SavedCity(
    val name: String = "",
    val country: String = "",
    val admin1: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
) {
    /** Stable key for de-dupe and swipe identity (rounded coords avoid float noise). */
    val key: String get() = "$name|$country|${(latitude * 100).toInt()}|${(longitude * 100).toInt()}"
}

/**
 * Device-local store of saved weather cities (multiplatform-settings), mirroring
 * WorldClockStore. Stores coordinates so weather can be fetched without
 * re-geocoding each time. Insertion order preserved.
 */
object WeatherStore {

    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }
    private const val KEY = "weather_cities"

    fun all(): List<SavedCity> {
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<SavedCity>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(city: SavedCity) {
        val current = all()
        if (current.any { it.key == city.key }) return
        settings[KEY] = json.encodeToString(current + city)
    }

    fun remove(city: SavedCity) {
        settings[KEY] = json.encodeToString(all().filterNot { it.key == city.key })
    }
}