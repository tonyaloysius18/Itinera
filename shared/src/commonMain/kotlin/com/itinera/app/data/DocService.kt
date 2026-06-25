package com.itinera.app.data

import com.itinera.app.model.DocItem
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reads and writes documents under trips/{tripId}/documents/{docId}.
 * Each DocItem carries memberIds (denormalized from its parent trip) so a single
 * collection-group query can stream all documents across the trips a user belongs to.
 *
 * (Previously documents lived under users/{uid}/documents. Moved under trips for sharing.)
 */
class DocService {

    private val db = Firebase.firestore

    private fun docsRef(tripId: String) =
        db.collection("trips").document(tripId).collection("documents")

    /** Save (create or overwrite) a single document under its trip. */
    suspend fun saveDocument(doc: DocItem) {
        docsRef(doc.tripId).document(doc.id).set(doc)
    }

    /** Remove a document under its trip. */
    suspend fun deleteDocument(tripId: String, docId: String) {
        docsRef(tripId).document(docId).delete()
    }

    /** Live stream of all documents across trips this user is a member of. */
    fun documentsFlow(uid: String): Flow<List<DocItem>> =
        db.collectionGroup("documents")
            .where { "memberIds" contains uid }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data(DocItem.serializer()) }
            }

    // ── one-off migration helper: reads the OLD users/{uid}/documents location ──
    suspend fun loadLegacyDocuments(uid: String): List<DocItem> {
        val snapshot = db.collection("users").document(uid).collection("documents").get()
        return snapshot.documents.map { it.data(DocItem.serializer()) }
    }
}