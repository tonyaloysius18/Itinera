package com.itinera.app.data

import com.itinera.app.config.Secrets
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One turn of the conversation sent to Nera. [role] is "user" or "assistant". */
@Serializable
data class NeraTurn(val role: String, val content: String)

@Serializable
data class NeraActivity(
    val title: String,
    val time: String = "",
    val endTime: String = "",
    val location: String = "",
    val note: String = "",
    val lat: Double = 0.0,          // from a real place search; 0.0 when unknown
    val lng: Double = 0.0,
    val rating: Double = 0.0,       // 0.0 when unknown
    val ratingCount: Int = 0,
    val price: String = "",         // "free", "$", "$$", "$$$", "$$$$" or blank
)

@Serializable
data class NeraDay(
    val date: String,               // ISO "2026-10-01"
    val theme: String = "",
    val activities: List<NeraActivity> = emptyList(),
)

/** A draft itinerary proposed by Nera. Nothing is saved until the traveller approves it. */
@Serializable
data class NeraItinerary(
    val title: String,
    val destination: String = "",
    val countries: List<String> = emptyList(),   // English country names, main destination first
    val startDate: String,
    val travellers: List<String> = emptyList(),   // first names, excluding the current user
    val days: List<NeraDay> = emptyList(),
)

/** Nera's answer: either a plain message (type "say") or a draft (type "itinerary"). */
@Serializable
data class NeraReply(
    val type: String,
    val message: String = "",
    val quickReplies: List<String> = emptyList(),
    val itinerary: NeraItinerary? = null,
)

@Serializable
private data class NeraRequest(val messages: List<NeraTurn>, val currentItinerary: NeraItinerary? = null)

@Serializable
private data class NeraError(val error: String = "")

/** Why a Nera request failed. The UI maps each to a localized message. */
enum class NeraFailure { NOT_CONFIGURED, SIGN_IN, NETWORK, BAD_REPLY, QUOTA, GENERIC }

class NeraException(val failure: NeraFailure) : Exception(failure.name)

/**
 * Talks to the `nera` Cloud Function, which holds the Anthropic key, checks the
 * caller's Firebase ID token and enforces a daily quota. The app never sees the key.
 * Endpoint comes from `neraEndpoint` in local.properties (see generateSecrets).
 */
class NeraService {

    private val client = HttpClient {
        // Drafting a multi-day itinerary (with weather/place lookups) can take a while; the default 10s would cut it off.
        install(HttpTimeout) {
            requestTimeoutMillis = 170_000
            socketTimeoutMillis = 170_000
            connectTimeoutMillis = 15_000
        }
    }
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    val isConfigured: Boolean get() = Secrets.NERA_ENDPOINT.startsWith("https://")

    /** Throws [NeraException] on any failure. */
    suspend fun send(messages: List<NeraTurn>, currentItinerary: NeraItinerary?): NeraReply {
        if (!isConfigured) throw NeraException(NeraFailure.NOT_CONFIGURED)
        val token = Firebase.auth.currentUser?.getIdToken(false)
            ?: throw NeraException(NeraFailure.SIGN_IN)
        val response = try {
            client.post(Secrets.NERA_ENDPOINT) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(NeraRequest.serializer(), NeraRequest(messages, currentItinerary)))
            }
        } catch (e: Exception) {
            throw NeraException(NeraFailure.NETWORK)
        }
        val body = response.bodyAsText()
        if (response.status == HttpStatusCode.OK) {
            return try {
                json.decodeFromString(NeraReply.serializer(), body)
            } catch (e: Exception) {
                throw NeraException(NeraFailure.BAD_REPLY)
            }
        }
        val code = runCatching { json.decodeFromString(NeraError.serializer(), body).error }.getOrDefault("")
        throw NeraException(if (response.status.value == 429 || code == "quota") NeraFailure.QUOTA else NeraFailure.GENERIC)
    }
}
