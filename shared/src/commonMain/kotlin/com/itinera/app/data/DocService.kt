package com.itinera.app.data

import com.itinera.app.model.DocItem
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

/**
 * Reads and writes the user's documents in Firestore under
 * users/{uid}/documents/{docId}. Each document carries a tripId field, so the
 * repository can filter them per trip in memory (same shape as the flat list).
 *
 * Mirrors ProfileService/TripService: explicit serializer on read, .set() on write.
 */
class DocService {

    private val db = Firebase.firestore

    private fun docsRef(uid: String) =
        db.collection("users").document(uid).collection("documents")

    /** Load all of the user's documents (across all trips). */
    suspend fun loadDocuments(uid: String): List<DocItem> {
        val snapshot = docsRef(uid).get()
        return snapshot.documents.map { doc ->
            doc.data(DocItem.serializer())
        }
    }

    /** Save (create or overwrite) a single document record. */
    suspend fun saveDocument(uid: String, doc: DocItem) {
        docsRef(uid).document(doc.id).set(doc)
    }

    /** Remove a document record. */
    suspend fun deleteDocument(uid: String, docId: String) {
        docsRef(uid).document(docId).delete()
    }
}