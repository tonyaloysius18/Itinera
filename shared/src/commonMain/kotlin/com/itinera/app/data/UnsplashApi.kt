package com.itinera.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.encodeURLParameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.itinera.app.config.Secrets


@Serializable
data class UnsplashSearchResponse(val results: List<UnsplashPhoto> = emptyList())

@Serializable
data class UnsplashPhoto(val urls: UnsplashUrls)

@Serializable
data class UnsplashUrls(val regular: String = "", @SerialName("small") val small: String = "")

class UnsplashApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    /** Returns a photo URL for [query], or null if none found / on error. */
    suspend fun fetchImage(query: String): String? {
        return try {
            val response: UnsplashSearchResponse = client
                .get("https://api.unsplash.com/search/photos") {
                    parameter("query", "$query landmark")
                    parameter("per_page", "1")
                    parameter("orientation", "landscape")
                    header("Authorization", "Client-ID ${Secrets.UNSPLASH_ACCESS_KEY}")
                }
                .body()
            response.results.firstOrNull()?.urls?.regular
        } catch (e: Exception) {
            null
        }
    }
}