package com.itinera.app.data

import com.itinera.app.model.SharedItinerary
import com.itinera.app.model.SharedLinkRecord
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.serialization.Serializable

/** Where a shared itinerary's preview page lives (Firebase Hosting serves one page for every path under s). */
const val SHARE_BASE_URL = "https://itinera-ae020.web.app/s/"

/**
 * Publishes and revokes shareable itineraries.
 *
 *  - `sharedItineraries/{id}`: the sanitized public copy. Readable by id, never listable (see firestore.rules).
 *  - `users/{uid}/sharedLinks/{id}`: the owner's private record. Its existence is what authorizes writing/deleting
 *    the public doc, so the public doc itself never has to carry an owner id.
 */
class SharedItineraryService {

    private val db = Firebase.firestore

    private fun publicRef() = db.collection("sharedItineraries")
    private fun mineRef(uid: String) = db.collection("users").document(uid).collection("sharedLinks")

    /** A fresh, unguessable id (Firestore's own random document id). */
    fun newId(): String = publicRef().document.id

    /** Order matters: the owner record must exist first, because the public doc's create rule checks for it. */
    suspend fun publish(uid: String, tripId: String, item: SharedItinerary, nowMillis: Long) {
        mineRef(uid).document(item.id).set(SharedLinkRecord(id = item.id, tripId = tripId, title = item.title, createdAt = nowMillis))
        publicRef().document(item.id).set(item)
    }

    suspend fun load(id: String): SharedItinerary? {
        val snap = publicRef().document(id).get()
        return if (snap.exists) snap.data(SharedItinerary.serializer()) else null
    }

    /** Public doc first (the rule needs the owner record to still be there), then the owner record. */
    suspend fun unpublish(uid: String, id: String) {
        publicRef().document(id).delete()
        mineRef(uid).document(id).delete()
    }

    /** The community feed: approved, listed itineraries, most-copied or newest first. One page (no cursor yet). */
    suspend fun feed(popular: Boolean, limit: Int = 50): List<SharedItinerary> =
        publicRef().where { "feed" equalTo true }
            .orderBy(if (popular) "copyCount" else "approvedAt", Direction.DESCENDING)
            .limit(limit)
            .get().documents.mapNotNull { runCatching { it.data(SharedItinerary.serializer()) }.getOrNull() }

    /**
     * Counts one copy per traveller. The marker doc and the +1 go in one batch, and the security rules only accept the
     * increment when that marker is created in the same batch, so repeated taps by one person can't inflate the count.
     * Quietly does nothing if this person already counted.
     */
    suspend fun recordCopy(uid: String, id: String, nowMillis: Long) {
        val doc = publicRef().document(id)
        val marker = doc.collection("copies").document(uid)
        if (marker.get().exists) return
        val batch = db.batch()
        batch.set(marker, CopyMarker(at = nowMillis))
        batch.update(doc, "copyCount" to FieldValue.increment(1))
        batch.commit()
    }

    suspend fun linkFor(uid: String, tripId: String): SharedLinkRecord? =
        mineRef(uid).where { "tripId" equalTo tripId }.get().documents.firstOrNull()?.data(SharedLinkRecord.serializer())
}

@Serializable
private data class CopyMarker(val at: Long = 0L)
