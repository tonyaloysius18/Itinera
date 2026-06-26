package com.itinera.app.data

import com.itinera.app.model.UserProfile
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth

/**
 * Thin wrapper around Firebase Auth. Suspend functions throw on failure
 * (wrong password, email already in use, network error, etc.), so callers
 * wrap them in try/catch and surface a message to the user.
 */
class AuthService {

    // null when signed out; a FirebaseUser when signed in
    val currentUser: FirebaseUser?
        get() = Firebase.auth.currentUser

    val isSignedIn: Boolean
        get() = currentUser != null

    val currentUid: String?
        get() = Firebase.auth.currentUser?.uid

    suspend fun updatePassword(newPassword: String) {
        Firebase.auth.currentUser?.updatePassword(newPassword)
    }

    suspend fun deleteAccount() {
        Firebase.auth.currentUser?.delete()
    }


    /** Create a new account. Throws if the email is taken or the password is weak. */
    suspend fun signUp(email: String, password: String): FirebaseUser? {
        val result = Firebase.auth.createUserWithEmailAndPassword(email.trim(), password)
        return result.user
    }

    /** Sign in to an existing account. Throws if credentials are wrong. */
    suspend fun signIn(email: String, password: String): FirebaseUser? {
        val result = Firebase.auth.signInWithEmailAndPassword(email.trim(), password)
        return result.user
    }

    /** Sign out the current user. */
    suspend fun signOut() {
        Firebase.auth.signOut()
    }

    suspend fun sendPasswordReset(email: String) {
        Firebase.auth.sendPasswordResetEmail(email)
    }

    suspend fun signInWithGoogle(idToken: String, accessToken: String) {
        val credential = GoogleAuthProvider.credential(idToken, accessToken)
        Firebase.auth.signInWithCredential(credential)
    }
    fun currentUserProfile(): UserProfile? {
        val user = Firebase.auth.currentUser ?: return null
        val fullName = user.displayName ?: ""
        val parts = fullName.split(" ")
        return UserProfile(
            name = parts.firstOrNull() ?: "",
            surname = parts.drop(1).joinToString(" "),
            email = user.email ?: "",
            photoUrl = user.photoURL ?: "",          // ⬅ Google profile photo
        )
    }

    fun currentSignInMethod(): String {
        val providers = Firebase.auth.currentUser?.providerData?.map { it.providerId } ?: emptyList()
        return if (providers.any { it.contains("google") }) "google" else "password"
    }

}