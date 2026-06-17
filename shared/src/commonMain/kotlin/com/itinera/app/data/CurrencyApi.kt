package com.itinera.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ===== ExchangeRate-API (open.er-api.com): 160+ currencies, no key, latest rates only =====
@Serializable
data class ErApiResponse(
    val result: String = "",
    val base_code: String = "",
    val time_last_update_utc: String = "",
    val rates: Map<String, Double> = emptyMap(),
)

// ===== Frankfurter: ~30 major currencies, free historical time-series for the chart =====
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

    /**
     * Live conversion via ExchangeRate-API — covers all 160+ currencies (no key).
     * Returns how much 1 unit of [from] is worth in [to], plus the update date.
     */
    suspend fun fetchRate(from: String, to: String): Pair<Double, String> {
        if (from == to) return 1.0 to "today"
        val response: ErApiResponse = client
            .get("https://open.er-api.com/v6/latest/$from")
            .body()
        if (response.result != "success") error("Rate unavailable")
        val rate = response.rates[to] ?: error("Rate unavailable")
        // Trim the long UTC string to just the date portion if present
        val date = response.time_last_update_utc.takeIf { it.isNotBlank() }
            ?.split(" ")?.let { parts ->
                if (parts.size >= 4) "${parts[3]}-${monthNum(parts[2])}-${parts[1]}" else ""
            } ?: ""
        return rate to date
    }

    /**
     * 30-day history via Frankfurter — only works for the major currencies it supports.
     * Returns an empty list (so the chart just hides) for unsupported pairs.
     */
    suspend fun fetchHistory(from: String, to: String, days: Int = 30): List<Pair<String, Double>> {
        if (from == to) return emptyList()
        return try {
            val response: TimeSeriesResponse = client
                .get("https://api.frankfurter.dev/v1/${startDate(days)}..") {
                    parameter("base", from)
                    parameter("symbols", to)
                }
                .body()
            response.rates.entries
                .sortedBy { it.key }
                .mapNotNull { (date, perSymbol) -> perSymbol[to]?.let { date to it } }
        } catch (_: Exception) {
            emptyList()   // currency not supported by Frankfurter → no chart, no crash
        }
    }

    private fun startDate(days: Int): String {
        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        val start = today.minus(DatePeriod(days = days))
        return start.toString()
    }

    private fun monthNum(mon: String): String = when (mon) {
        "Jan" -> "01"; "Feb" -> "02"; "Mar" -> "03"; "Apr" -> "04"
        "May" -> "05"; "Jun" -> "06"; "Jul" -> "07"; "Aug" -> "08"
        "Sep" -> "09"; "Oct" -> "10"; "Nov" -> "11"; "Dec" -> "12"
        else -> "01"
    }
}