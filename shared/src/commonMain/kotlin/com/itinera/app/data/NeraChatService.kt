package com.itinera.app.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

/**
 * Persists the Nera conversation for a trip under trips/{tripId}/neraMessages, so leaving
 * and reopening Nera for that trip restores the exchange instead of starting from scratch.
 * One document per message (same shape as ExpenseService/DocService); `seq` orders them.
 */
class NeraChatService {

    private val db = Firebase.firestore

    private fun ref(tripId: String) =
        db.collection("trips").document(tripId).collection("neraMessages")

    /** The stored conversation for a trip, oldest first. Empty if none yet or on failure. */
    suspend fun loadHistory(tripId: String): List<StoredNeraMessage> {
        val snapshot = ref(tripId).get()
        return snapshot.documents.map { it.data(StoredNeraMessage.serializer()) }.sortedBy { it.seq }
    }

    /** Save (create or overwrite) one turn. */
    suspend fun appendMessage(tripId: String, message: StoredNeraMessage) {
        ref(tripId).document(message.id).set(message)
    }

    /** Save several turns at once (used when a new trip's pre-approval chat is persisted retroactively). */
    suspend fun appendMessages(tripId: String, messages: List<StoredNeraMessage>) {
        for (m in messages) appendMessage(tripId, m)
    }

    /** Marks a drafted itinerary as already applied to the trip, so its buttons don't reappear on reload. */
    suspend fun markApproved(tripId: String, messageId: String) {
        ref(tripId).document(messageId).update("approved" to true)
    }
}
