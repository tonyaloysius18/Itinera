package com.itinera.app.data

import com.itinera.app.model.TripTemplate
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

/**
 * Reads curated trip templates from the top-level `tripTemplates` collection.
 * Read-only from the app: templates are authored/updated out of band (see
 * tools/seed/) rather than written here.
 */
class TripTemplateService {

    private val db = Firebase.firestore

    private fun templatesRef() = db.collection("tripTemplates")

    /** All published templates, ordered for Discover browsing. */
    suspend fun loadTemplates(): List<TripTemplate> {
        val snapshot = templatesRef().get()
        println("ITINERA: Loaded ${snapshot.documents.size} template documents from Firestore")
        return snapshot.documents.mapNotNull { doc ->
            try {
                val t = doc.data(TripTemplate.serializer())
                t
            } catch (e: Exception) {
                println("ITINERA: Failed to parse template doc ${doc.id}: ${e.message}")
                null
            }
        }.filter { it.published }.sortedBy { it.sortOrder }
    }
}
