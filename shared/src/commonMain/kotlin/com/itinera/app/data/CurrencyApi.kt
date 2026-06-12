package com.itinera.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RatesResponse(
    val amount: Double = 1.0,
    val base: String = "",
    val date: String = "",
    val rates: Map<String, Double> = emptyMap(),
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
}