package com.itinera.app.data

import com.itinera.app.model.Trip
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
/**
 * Reads and writes the user's trips in Firestore under users/{uid}/trips/{tripId}.
 * Mirrors ProfileService's API exactly: explicit serializer on read, .set() on write.
 */
class TripService {

    private val db = Firebase.firestore

    private fun tripsRef(uid: String) =
        db.collection("users").document(uid).collection("trips")

    /** Save (create or overwrite) a single trip. */
    suspend fun saveTrip(uid: String, trip: Trip) {
        tripsRef(uid).document(trip.id).set(trip)
    }

    /** Load all of the user's trips. */
    suspend fun loadTrips(uid: String): List<Trip> {
        val snapshot = tripsRef(uid).get()
        return snapshot.documents.map { doc ->
            doc.data(Trip.serializer())                 // ⬅ explicit serializer, like ProfileService
        }
    }
    /** Remove a trip. */
    suspend fun deleteTrip(uid: String, tripId: String) {
        tripsRef(uid).document(tripId).delete()
    }

    fun tripsFlow(uid: String): Flow<List<Trip>> =
        tripsRef(uid).snapshots.map { snapshot ->
            snapshot.documents.map { doc -> doc.data(Trip.serializer()) }
        }

}