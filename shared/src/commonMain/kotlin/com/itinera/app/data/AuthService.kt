package com.itinera.app.data

import com.itinera.app.model.UserProfile
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
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

    suspend fun reauthenticate(currentPassword: String) {
        val user = Firebase.auth.currentUser ?: error("Not signed in")
        val email = user.email ?: error("No email on account")
        user.reauthenticate(EmailAuthProvider.credential(email, currentPassword))
    }

    /** False for Google/Apple sign-in — there's no password to change. */
    val hasPasswordProvider: Boolean
        get() = Firebase.auth.currentUser
            ?.providerData
            ?.any { it.providerId == "password" } == true

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

//    suspend fun signInWithGoogle(idToken: String, accessToken: String) {
//        val credential = GoogleAuthProvider.credential(idToken, accessToken)
//        Firebase.auth.signInWithCredential(credential)
//    }
    suspend fun signInWithGoogle(
        idToken: String,
        accessToken: String?
    ) {
        val credential = GoogleAuthProvider.credential(
            idToken = idToken,
            accessToken = accessToken
        )

        Firebase.auth.signInWithCredential(credential)
    }

    /**
     * Sign in with Apple. [idToken] is the identity token (JWT) returned by
     * ASAuthorization, and [rawNonce] is the un-hashed nonce that was SHA256'd
     * into the authorization request — Firebase re-hashes it to verify the token.
     */
    suspend fun signInWithApple(idToken: String, rawNonce: String) {
        val credential = OAuthProvider.credential(
            providerId = "apple.com",
            idToken = idToken,
            rawNonce = rawNonce,
        )
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
        return when {
            providers.any { it.contains("google") } -> "google"
            providers.any { it.contains("apple") } -> "apple"
            else -> "password"
        }
    }

}