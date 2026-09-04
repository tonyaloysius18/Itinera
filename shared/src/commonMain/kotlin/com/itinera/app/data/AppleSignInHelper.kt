// AppleSignInHelper.kt (commonMain)
package com.itinera.app.data

import androidx.compose.runtime.Composable

/**
 * Result of a native "Sign in with Apple" flow: the identity token (a JWT) and
 * the *raw* nonce that was hashed into the request. Firebase needs both to build
 * an Apple OAuth credential.
 *
 * [fullName] and [email] come from the ASAuthorization credential rather than
 * the token. Apple returns the name **only on the very first authorization** for
 * a given Apple ID + app pair, and never puts it in the JWT — so if it isn't
 * captured here it is lost for good, and the profile ends up nameless.
 */
data class AppleCredential(
    val idToken: String,
    val rawNonce: String,
    val fullName: String? = null,
    val email: String? = null,
)

/**
 * Outcome of the native flow. Cancelling the sheet and failing outright are
 * deliberately distinct: a cancel is the user's choice and stays silent, while
 * a failure has to surface, or the button just looks dead.
 */
sealed interface AppleSignInResult {
    data class Success(val credential: AppleCredential) : AppleSignInResult
    data object Cancelled : AppleSignInResult
    data class Failed(val reason: String) : AppleSignInResult
}

expect class AppleSignInHelper {
    suspend fun signIn(): AppleSignInResult
}

@Composable
expect fun rememberAppleSignInHelper(): AppleSignInHelper
