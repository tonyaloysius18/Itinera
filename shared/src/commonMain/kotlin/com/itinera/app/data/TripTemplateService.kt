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
        val snapshot = templatesRef()
            .where { "published" equalTo true }
            .get()
        return snapshot.documents
            .map { it.data(TripTemplate.serializer()) }
            .sortedBy { it.sortOrder }
    }
}
