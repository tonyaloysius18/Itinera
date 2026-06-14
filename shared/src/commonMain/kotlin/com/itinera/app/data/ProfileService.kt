package com.itinera.app.data

import com.itinera.app.model.UserProfile
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.serialization.builtins.serializer

/**
 * Reads and writes the user's profile document in Firestore at users/{uid}.
 * The document ID is the Firebase Auth UID, so each user owns exactly one
 * profile document (enforced by the security rule).
 */
class ProfileService {

    private val db = Firebase.firestore

    /** Save (create or overwrite) the profile for the given user id. */
    suspend fun saveProfile(uid: String, profile: UserProfile) {
        db.collection("users").document(uid).set(profile)
    }

    /** Load the profile for the given user id, or null if none exists yet. */
    suspend fun loadProfile(uid: String): UserProfile? {
        val snapshot = db.collection("users").document(uid).get()
        return if (snapshot.exists) snapshot.data(UserProfile.serializer()) else null
    }

    suspend fun deleteProfile(uid: String) {
        db.collection("users").document(uid).delete()
    }
}