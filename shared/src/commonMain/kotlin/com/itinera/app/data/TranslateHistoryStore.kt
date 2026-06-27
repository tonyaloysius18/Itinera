package com.itinera.app.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** One saved translation (history entry; may also be favorited). */
@Serializable
data class TranslationEntry(
    val id: String = "",
    val sourceText: String = "",
    val resultText: String = "",
    val sourceLang: String = "",   // code, e.g. "en"
    val targetLang: String = "",   // code, e.g. "fr"
    val createdAt: Long = 0L,
    val favorite: Boolean = false,
)

/**
 * Device-local translation history + favorites (multiplatform-settings), mirroring
 * WeatherStore. Newest first; history is capped, favorites are kept regardless.
 */
object TranslateHistoryStore {

    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }
    private const val KEY = "translate_history"
    private const val MAX_HISTORY = 50   // non-favorite cap

    fun all(): List<TranslationEntry> {
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<TranslationEntry>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persist(list: List<TranslationEntry>) {
        settings[KEY] = json.encodeToString(list)
    }

    /** Add (or move-to-top) a translation. De-dupes on same text+langs. */
    fun add(entry: TranslationEntry) {
        if (entry.sourceText.isBlank() || entry.resultText.isBlank()) return
        val existing = all().filterNot {
            it.sourceText == entry.sourceText &&
                    it.sourceLang == entry.sourceLang &&
                    it.targetLang == entry.targetLang
        }
        // keep all favorites + newest non-favorites up to the cap
        val combined = listOf(entry) + existing
        val favorites = combined.filter { it.favorite }
        val nonFavorites = combined.filter { !it.favorite }.take(MAX_HISTORY)
        persist((favorites + nonFavorites).sortedByDescending { it.createdAt })
    }

    fun toggleFavorite(id: String) {
        persist(all().map { if (it.id == id) it.copy(favorite = !it.favorite) else it })
    }

    fun remove(id: String) {
        persist(all().filterNot { it.id == id })
    }

    fun clearNonFavorites() {
        persist(all().filter { it.favorite })
    }
}