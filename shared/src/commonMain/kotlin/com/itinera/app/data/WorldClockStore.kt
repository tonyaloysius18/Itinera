package com.itinera.app.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** A saved world-clock entry: the display label the user picked + its IANA zone. */
@Serializable
data class SavedZone(
    val label: String = "",     // e.g. "Chennai"
    val zoneId: String = "",    // e.g. "Asia/Kolkata"
)

/**
 * Device-local store of the world-clock entries the user has added. Stores a
 * (label, zoneId) pair so the row can display the city the user actually searched
 * for (e.g. "Chennai") while using the correct IANA zone ("Asia/Kolkata") for the
 * time. Persisted with multiplatform-settings; insertion order preserved.
 */
object WorldClockStore {

    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }
    private const val KEY = "world_clock_zones_v2"   // v2: now stores labels too

    fun all(): List<SavedZone> {
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<SavedZone>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(label: String, zoneId: String) {
        if (zoneId.isBlank()) return
        val current = all()
        // de-dupe on label+zone so "Chennai/Kolkata" and "Delhi/Kolkata" can coexist
        if (current.any { it.label == label && it.zoneId == zoneId }) return
        settings[KEY] = json.encodeToString(current + SavedZone(label, zoneId))
    }

    fun remove(entry: SavedZone) {
        settings[KEY] = json.encodeToString(
            all().filterNot { it.label == entry.label && it.zoneId == entry.zoneId }
        )
    }
}