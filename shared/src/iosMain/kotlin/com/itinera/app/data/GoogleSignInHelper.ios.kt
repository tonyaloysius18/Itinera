package com.itinera.app.data

import androidx.compose.runtime.Composable
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class GoogleSignInHelper {
    actual suspend fun signIn(): GoogleSignInResult = suspendCoroutine { cont ->
        val provider = IosGoogleSignIn.provider
        if (provider == null) {
            cont.resume(GoogleSignInResult.Failed("Google sign-in bridge is not installed"))
        } else {
            provider { idToken, accessToken, error ->
                val result = when {
                    idToken != null -> GoogleSignInResult.Success(
                        GoogleTokens(idToken, accessToken)
                    )
                    error != null -> GoogleSignInResult.Failed(error)
                    else -> GoogleSignInResult.Cancelled
                }
                cont.resume(result)
            }
        }
    }
}

@Composable
actual fun rememberGoogleSignInHelper(): GoogleSignInHelper = GoogleSignInHelper()

/**
 * Swift installs the actual GIDSignIn flow here at startup (see iOSApp.swift).
 * The closure receives a callback it must invoke with exactly one of these
 * shapes:
 *   - (idToken, accessToken, null) on success
 *   - (null, null, null)           when the user cancelled the sheet
 *   - (null, null, reason)         on failure
 */
object IosGoogleSignIn {
    var provider: ((onResult: (String?, String?, String?) -> Unit) -> Unit)? = null
}
