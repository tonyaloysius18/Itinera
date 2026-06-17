package com.itinera.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock

@Serializable
data class RatesResponse(
    val amount: Double = 1.0,
    val base: String = "",
    val date: String = "",
    val rates: Map<String, Double> = emptyMap(),
)

@Serializable
data class TimeSeriesResponse(
    val amount: Double = 1.0,
    val base: String = "",
    val start_date: String = "",
    val end_date: String = "",
    val rates: Map<String, Map<String, Double>> = emptyMap(),  // date -> { symbol -> rate }
)

class CurrencyApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    /** Returns how much 1 unit of [from] is worth in [to], plus the rate date. */
    suspend fun fetchRate(from: String, to: String): Pair<Double, String> {
        if (from == to) return 1.0 to "today"
        val response: RatesResponse = client
            .get("https://api.frankfurter.dev/v1/latest") {
                parameter("base", from)
                parameter("symbols", to)
            }
            .body()
        val rate = response.rates[to] ?: error("Rate unavailable")
        return rate to response.date
    }

    /** Returns a list of (date, rate) points for the last [days] days, oldest first. */
    suspend fun fetchHistory(from: String, to: String, days: Int = 30): List<Pair<String, Double>> {
        if (from == to) return emptyList()
        // Frankfurter time-series: /v1/YYYY-MM-DD.. returns from that date up to today
        val response: TimeSeriesResponse = client
            .get("https://api.frankfurter.dev/v1/${startDate(days)}..") {
                parameter("base", from)
                parameter("symbols", to)
            }
            .body()
        return response.rates.entries
            .sortedBy { it.key }
            .mapNotNull { (date, perSymbol) -> perSymbol[to]?.let { date to it } }
    }

    private fun startDate(days: Int): String {
        val today = Clock.System.todayIn(TimeZone.UTC)
        val start = today.minus(DatePeriod(days = days))
        return start.toString()   // ISO format YYYY-MM-DD, which Frankfurter expects
    }
}