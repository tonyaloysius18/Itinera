package com.itinera.app.data

import com.itinera.app.model.Trip
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reads and writes shared trips in Firestore under the top-level trips/{tripId}.
 * A trip is visible to any user whose uid is in its `memberIds` array; their role
 * lives in the `members` map (uid -> "owner"|"editor"|"viewer").
 *
 * (Previously trips lived under users/{uid}/trips. Moved top-level for sharing.)
 */
class TripService {

    private val db = Firebase.firestore

    private fun tripsRef() = db.collection("trips")

    /** Save (create or overwrite) a single trip at its top-level path. */
    suspend fun saveTrip(trip: Trip) {
        tripsRef().document(trip.id).set(trip)
    }

    /** Load all trips this user is a member of. */
    suspend fun loadTrips(uid: String): List<Trip> {
        val snapshot = tripsRef()
            .where { "memberIds" contains uid }
            .get()
        return snapshot.documents.map { doc ->
            doc.data(Trip.serializer())
        }
    }

    /** Remove a trip entirely (owner only — enforced by security rules). */
    suspend fun deleteTrip(tripId: String) {
        tripsRef().document(tripId).delete()
    }

    // ── one-off migration helper: reads the OLD users/{uid}/trips location ──
    suspend fun loadLegacyTrips(uid: String): List<Trip> {
        val snapshot = db.collection("users").document(uid).collection("trips").get()
        return snapshot.documents.map { it.data(Trip.serializer()) }
    }

    /** Live stream of trips this user is a member of — emits on every remote change. */
    fun tripsFlow(uid: String): Flow<List<Trip>> =
        tripsRef()
            .where { "memberIds" contains uid }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc -> doc.data(Trip.serializer()) }
            }
}