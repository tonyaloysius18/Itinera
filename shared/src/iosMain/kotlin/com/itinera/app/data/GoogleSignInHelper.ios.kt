package com.itinera.app.data

import androidx.compose.runtime.Composable
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class GoogleSignInHelper {
    actual suspend fun signIn(): GoogleTokens? = suspendCoroutine { cont ->
        val provider = IosGoogleSignIn.provider
        if (provider == null) {
            cont.resume(null)
        } else {
            provider { idToken, accessToken ->
                if (idToken != null && accessToken != null) {
                    cont.resume(GoogleTokens(idToken, accessToken))
                } else {
                    cont.resume(null)
                }
            }
        }
    }
}

@Composable
actual fun rememberGoogleSignInHelper(): GoogleSignInHelper = GoogleSignInHelper()

object IosGoogleSignIn {
    // now passes idToken AND accessToken
    var provider: ((onResult: (String?, String?) -> Unit) -> Unit)? = null
}