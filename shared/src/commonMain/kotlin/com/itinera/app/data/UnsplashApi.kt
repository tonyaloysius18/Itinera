package com.itinera.app.data

import com.itinera.app.config.Secrets
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


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

    /**
     * Returns a photo URL for [query], or null if none found / on error. "landmark" gives the best photos for big
     * cities, but returns nothing for smaller ones (e.g. Grenoble), so fall back to broader searches.
     */
    suspend fun fetchImage(query: String): String? {
        for (term in listOf("$query landmark", "$query city", query)) {
            search(term)?.let { return it }
        }
        return null
    }

    private suspend fun search(term: String): String? {
        return try {
            val response: UnsplashSearchResponse = client
                .get("https://api.unsplash.com/search/photos") {
                    parameter("query", term)
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
