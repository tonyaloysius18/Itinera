package com.itinera.app.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Point-of-interest search, for the location field on AddPlaceScreen.
 *
 * NOT Open-Meteo: that geocoder only indexes populated places, so "Mercedes-Benz
 * Museum" returns nothing. Photon is OSM-backed like the maps already in use and
 * is built specifically for type-ahead search — Nominatim has the same data but
 * its usage policy discourages autocomplete.
 *
 * Free public endpoint, no key. If you ever outgrow it, Photon is open source
 * and self-hostable, so the API surface here wouldn't change.
 */
class PlaceSearchService(
    private val client: HttpClient = HttpClient(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * @param lat/[lon] optional bias toward the user's trip. Worth passing:
     * "museum" is a very different result set in Stuttgart versus globally.
     */
    suspend fun search(
        query: String,
        lat: Double? = null,
        lon: Double? = null,
        limit: Int = 6,
        lang: String = "en",
    ): List<PlaceResult> {
        val q = query.trim()
        if (q.length < 3) return emptyList()

        val url = buildString {
            append("https://photon.komoot.io/api/?q=")
            append(q.encodeURLParameter())
            append("&limit=").append(limit)
            append("&lang=").append(lang)
            if (lat != null && lon != null) {
                append("&lat=").append(lat).append("&lon=").append(lon)
            }
        }

        return try {
            val body = client.get(url).bodyAsText()
            json.decodeFromString<PhotonResponse>(body).features.mapNotNull { it.toPlaceResult() }
        } catch (e: Exception) {
            println("ITINERA: PLACE SEARCH FAILED — ${e.message}")
            emptyList()
        }
    }
}

/** What the UI needs: a name to show, an address to store, and coordinates. */
data class PlaceResult(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)

// ─── Photon GeoJSON ─────────────────────────────────────────────────────────

@Serializable
private data class PhotonResponse(val features: List<PhotonFeature> = emptyList())

@Serializable
private data class PhotonFeature(
    val properties: PhotonProperties = PhotonProperties(),
    val geometry: PhotonGeometry = PhotonGeometry(),
)

@Serializable
private data class PhotonProperties(
    val name: String? = null,
    val street: String? = null,
    @SerialName("housenumber") val houseNumber: String? = null,
    val postcode: String? = null,
    val city: String? = null,
    val district: String? = null,
    val state: String? = null,
    val country: String? = null,
)

@Serializable
private data class PhotonGeometry(
    /** GeoJSON order is [longitude, latitude] — not the other way round. */
    val coordinates: List<Double> = emptyList(),
)

private fun PhotonFeature.toPlaceResult(): PlaceResult? {
    val p = properties
    val name = p.name?.takeIf { it.isNotBlank() } ?: return null
    if (geometry.coordinates.size < 2) return null

    // "Mercedesstraße 100, 70372 Stuttgart, Germany"
    val streetLine = listOfNotNull(p.street, p.houseNumber)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .takeIf { it.isNotBlank() }
    val cityLine = listOfNotNull(p.postcode, p.city ?: p.district)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .takeIf { it.isNotBlank() }
    val address = listOfNotNull(streetLine, cityLine, p.country)
        .filter { it.isNotBlank() }
        .joinToString(", ")

    return PlaceResult(
        name = name,
        address = address,
        longitude = geometry.coordinates[0],
        latitude = geometry.coordinates[1],
    )
}