package com.itinera.app.data

import com.itinera.app.model.Trip
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Fetches a coarse weather signal per destination for a trip, via Open-Meteo (no key):
 *  - trip within ~14 days → real daily forecast for the leg coordinates.
 *  - further out → seasonal climate normals (typical weather for that place/season)
 *    approximated from last year's archive for the same calendar window.
 *
 * One call per distinct geocoded destination city; failures are skipped silently
 * (packing still gets the non-weather rules).
 */

@Serializable
private data class PackingDailyBlock(
    val time: List<String> = emptyList(),
    val temperature_2m_max: List<Double?> = emptyList(),
    val temperature_2m_min: List<Double?> = emptyList(),
    val precipitation_sum: List<Double?> = emptyList(),
)
@Serializable
private data class OpenMeteoDaily(val daily: PackingDailyBlock? = null)

private val pkJson = Json { ignoreUnknownKeys = true }

private fun twoDigit(value: Int): String = value.toString().padStart(2, '0')

suspend fun fetchTripWeather(client: HttpClient, trip: Trip): List<DestinationWeather> {
    val dates = trip.legs.map { it.date }.sorted()
    if (dates.isEmpty()) return emptyList()
    val start = dates.first()
    val end = dates.last()
    val near = tripStartsInDays(trip) in 0..14

    // distinct destinations with coordinates (endpoints + stops)
    data class Dest(val city: String, val lat: Double, val lng: Double)
    val dests = buildList {
        trip.legs.forEach { leg ->
            if (leg.toLat != 0.0 || leg.toLng != 0.0) add(Dest(leg.toCity, leg.toLat, leg.toLng))
            leg.stops.forEach { if (it.lat != 0.0 || it.lng != 0.0) add(Dest(it.city, it.lat, it.lng)) }
        }
    }.distinctBy { it.city.trim().lowercase() }

    return dests.mapNotNull { d ->
        try {
            val url = if (near) {
                "https://api.open-meteo.com/v1/forecast?latitude=${d.lat}&longitude=${d.lng}" +
                        "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum" +
                        "&start_date=$start&end_date=$end&timezone=auto"
            } else {
                // seasonal proxy: same calendar window, previous year, from the archive
                val y = start.year - 1
                val sMd = "${twoDigit(start.monthNumber)}-${twoDigit(start.dayOfMonth)}"
                val eMd = "${twoDigit(end.monthNumber)}-${twoDigit(end.dayOfMonth)}"
                "https://archive-api.open-meteo.com/v1/archive?latitude=${d.lat}&longitude=${d.lng}" +
                        "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum" +
                        "&start_date=$y-$sMd&end_date=$y-$eMd&timezone=auto"
            }
            val body = client.get(url).bodyAsText()
            val daily = pkJson.decodeFromString(OpenMeteoDaily.serializer(), body).daily ?: return@mapNotNull null
            val maxT = daily.temperature_2m_max.filterNotNull().maxOrNull()
            val minT = daily.temperature_2m_min.filterNotNull().minOrNull()
            val rain = daily.precipitation_sum.filterNotNull().any { it >= 3.0 }  // ≥3mm on any day
            DestinationWeather(d.city, maxT, minT, rain, isForecast = near)
        } catch (e: Exception) {
            null
        }
    }
}
