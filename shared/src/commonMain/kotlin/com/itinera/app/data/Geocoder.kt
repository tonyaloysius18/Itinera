package com.itinera.app.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLQueryComponent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * City-name → coordinates via the free Open-Meteo geocoding API (no key, no auth).
 * https://geocoding-api.open-meteo.com/v1/search?name=Toulouse&count=1
 *
 * Used to stamp lat/lng onto legs once at save time, so the trip map opens
 * instantly and works offline afterwards.
 */

@Serializable
data class GeoPoint(val lat: Double, val lng: Double, val country: String = "")


@Serializable
private data class GeocoderResult(
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "",
)

@Serializable
private data class GeocoderResponse(val results: List<GeocoderResult> = emptyList())

private val geoJson = Json { ignoreUnknownKeys = true }

/** Returns the best-match coordinates for a city name, or null if not found / offline. */
suspend fun geocodeCity(client: HttpClient, cityName: String): GeoPoint? {
    val name = cityName.trim()
    if (name.isBlank()) return null
    return try {
        val q = name.encodeURLQueryComponent()
        val body = client
            .get("https://geocoding-api.open-meteo.com/v1/search?name=$q&count=1&language=en&format=json")
            .bodyAsText()
        val parsed = geoJson.decodeFromString(GeocoderResponse.serializer(), body)
        parsed.results.firstOrNull()
            ?.takeIf { it.latitude != 0.0 || it.longitude != 0.0 }
            ?.let { GeoPoint(it.latitude, it.longitude, it.country) }
    } catch (e: Exception) {
        println("ITINERA: GEOCODE FAILED for '$name' — ${e.message}")
        null
    }
}