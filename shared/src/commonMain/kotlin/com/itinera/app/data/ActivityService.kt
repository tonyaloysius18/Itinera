package com.itinera.app.data

import com.itinera.app.model.Activity
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reads and writes activities (places) under trips/{tripId}/activities/{actId}.
 * Each Activity carries memberIds (denormalized from its parent trip) so a single
 * collection-group query streams all activities across the trips a user belongs to.
 *
 * (Activities were previously in-memory only; now persisted + shared like docs/expenses.)
 */
class ActivityService {

    private val db = Firebase.firestore

    private fun activitiesRef(tripId: String) =
        db.collection("trips").document(tripId).collection("activities")

    /** Save (create or overwrite) a single activity under its trip. */
    suspend fun saveActivity(activity: Activity) {
        activitiesRef(activity.tripId).document(activity.id).set(activity)
    }

    /** Remove an activity under its trip. */
    suspend fun deleteActivity(tripId: String, activityId: String) {
        activitiesRef(tripId).document(activityId).delete()
    }

    /** Live stream of all activities across trips this user is a member of. */
    fun activitiesFlow(uid: String): Flow<List<Activity>> =
        db.collectionGroup("activities")
            .where { "memberIds" contains uid }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data(Activity.serializer()) }
            }
}