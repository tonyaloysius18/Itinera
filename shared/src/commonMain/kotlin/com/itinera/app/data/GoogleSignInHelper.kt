// GoogleSignInHelper.kt (commonMain)
package com.itinera.app.data

import androidx.compose.runtime.Composable

data class GoogleTokens(
    val idToken: String,
    val accessToken: String?
)

/**
 * Outcome of the native Google flow. Cancelling and failing are deliberately
 * distinct: a cancel is the user's choice and stays silent, while a failure has
 * to surface, or the button just looks dead.
 */
sealed interface GoogleSignInResult {
    data class Success(val tokens: GoogleTokens) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data class Failed(val reason: String) : GoogleSignInResult
}

expect class GoogleSignInHelper {
    suspend fun signIn(): GoogleSignInResult
}

@Composable
expect fun rememberGoogleSignInHelper(): GoogleSignInHelper
