package com.itinera.app.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ---- Geocoding response ----
@Serializable
private data class GeoResponse(val results: List<GeoEntry>? = null)

@Serializable
private data class GeoEntry(
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "",
    val admin1: String = "",   // region/state, helps disambiguate
)

/** A resolved place with coordinates. */
data class GeoPlace(
    val name: String,
    val country: String,
    val admin1: String,
    val latitude: Double,
    val longitude: Double,
)

// ---- Forecast response ----
@Serializable
private data class ForecastResponse(
    val current: CurrentBlock? = null,
    val daily: DailyBlock? = null,
)

@Serializable
private data class CurrentBlock(
    val temperature_2m: Double = 0.0,
    val weather_code: Int = 0,
)

@Serializable
private data class DailyBlock(
    val time: List<String> = emptyList(),
    val weather_code: List<Int> = emptyList(),
    val temperature_2m_max: List<Double> = emptyList(),
    val temperature_2m_min: List<Double> = emptyList(),
)

/** One day's forecast. */
data class DayForecast(
    val date: String,       // ISO "2026-06-26"
    val code: Int,
    val maxTemp: Double,
    val minTemp: Double,
)

/** Current conditions + a short daily forecast. */
data class WeatherResult(
    val currentTemp: Double,
    val currentCode: Int,
    val days: List<DayForecast>,
)

/**
 * Weather via Open-Meteo (free, no API key). Two calls: geocode a city name to
 * coordinates, then fetch current + daily forecast. JSON parsed with
 * kotlinx.serialization from the raw body (no Ktor content-negotiation needed).
 */
class WeatherService {

    private val client = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    /** Resolve a city name to coordinates. Returns null if not found / on error. */
    suspend fun geocode(city: String): GeoPlace? = geocodeMany(city).firstOrNull()

    /** Resolve a city name to up to [count] candidate places (for disambiguation). */
    suspend fun geocodeMany(city: String, count: Int = 6): List<GeoPlace> {
        if (city.isBlank()) return emptyList()
        return try {
            val q = city.trim().encodeURLParameter()
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$q&count=$count&language=en&format=json"
            val body = client.get(url).bodyAsText()
            val parsed = json.decodeFromString<GeoResponse>(body)
            (parsed.results ?: emptyList()).map {
                GeoPlace(
                    name = it.name,
                    country = it.country,
                    admin1 = it.admin1,
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
            }
        } catch (e: Exception) {
            println("ITINERA: GEOCODE FAILED — ${e.message}")
            emptyList()
        }
    }

    /** Fetch current + [days]-day daily forecast for coordinates. Null on error. */
    suspend fun forecast(latitude: Double, longitude: Double, days: Int = 5): WeatherResult? {
        return try {
            val url = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$latitude&longitude=$longitude" +
                    "&current=temperature_2m,weather_code" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                    "&timezone=auto&forecast_days=$days"
            val body = client.get(url).bodyAsText()
            val parsed = json.decodeFromString<ForecastResponse>(body)
            val cur = parsed.current ?: return null
            val d = parsed.daily ?: return null
            val list = d.time.indices.map { i ->
                DayForecast(
                    date = d.time.getOrElse(i) { "" },
                    code = d.weather_code.getOrElse(i) { 0 },
                    maxTemp = d.temperature_2m_max.getOrElse(i) { 0.0 },
                    minTemp = d.temperature_2m_min.getOrElse(i) { 0.0 },
                )
            }
            WeatherResult(
                currentTemp = cur.temperature_2m,
                currentCode = cur.weather_code,
                days = list,
            )
        } catch (e: Exception) {
            println("ITINERA: FORECAST FAILED — ${e.message}")
            null
        }
    }
}

/** WMO weather code → short label. */
fun weatherLabel(code: Int): String = when (code) {
    0 -> "Clear sky"
    1 -> "Mainly clear"
    2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Fog"
    51, 53, 55 -> "Drizzle"
    56, 57 -> "Freezing drizzle"
    61, 63, 65 -> "Rain"
    66, 67 -> "Freezing rain"
    71, 73, 75 -> "Snow"
    77 -> "Snow grains"
    80, 81, 82 -> "Rain showers"
    85, 86 -> "Snow showers"
    95 -> "Thunderstorm"
    96, 99 -> "Thunderstorm, hail"
    else -> "—"
}

/** WMO weather code → emoji glyph (works cross-platform without icon assets). */
fun weatherEmoji(code: Int): String = when (code) {
    0 -> "☀️"
    1, 2 -> "🌤️"
    3 -> "☁️"
    45, 48 -> "🌫️"
    51, 53, 55, 56, 57 -> "🌦️"
    61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️"
    71, 73, 75, 77, 85, 86 -> "❄️"
    95, 96, 99 -> "⛈️"
    else -> "🌡️"
}