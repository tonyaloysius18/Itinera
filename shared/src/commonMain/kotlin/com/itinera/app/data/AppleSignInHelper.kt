// AppleSignInHelper.kt (commonMain)
package com.itinera.app.data

import androidx.compose.runtime.Composable

/**
 * Result of a native "Sign in with Apple" flow: the identity token (a JWT) and
 * the *raw* nonce that was hashed into the request. Firebase needs both to build
 * an Apple OAuth credential.
 */
data class AppleCredential(
    val idToken: String,
    val rawNonce: String,
)

expect class AppleSignInHelper {
    suspend fun signIn(): AppleCredential?
}

@Composable
expect fun rememberAppleSignInHelper(): AppleSignInHelper
