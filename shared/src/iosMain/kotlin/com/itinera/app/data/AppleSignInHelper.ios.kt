package com.itinera.app.data

import androidx.compose.runtime.Composable
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class AppleSignInHelper {
    actual suspend fun signIn(): AppleCredential? = suspendCoroutine { cont ->
        val provider = IosAppleSignIn.provider
        if (provider == null) {
            cont.resume(null)
        } else {
            provider { idToken, rawNonce ->
                if (idToken != null && rawNonce != null) {
                    cont.resume(AppleCredential(idToken, rawNonce))
                } else {
                    cont.resume(null)
                }
            }
        }
    }
}

@Composable
actual fun rememberAppleSignInHelper(): AppleSignInHelper = AppleSignInHelper()

/**
 * Swift installs the actual ASAuthorizationController flow here at startup (see
 * iOSApp.swift). The closure receives a callback it must invoke with
 * (identityToken, rawNonce), or (null, null) on cancel/failure.
 */
object IosAppleSignIn {
    var provider: ((onResult: (String?, String?) -> Unit) -> Unit)? = null
}
