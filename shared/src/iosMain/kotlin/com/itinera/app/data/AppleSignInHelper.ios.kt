package com.itinera.app.data

import androidx.compose.runtime.Composable
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class AppleSignInHelper {
    actual suspend fun signIn(): AppleSignInResult = suspendCoroutine { cont ->
        val provider = IosAppleSignIn.provider
        if (provider == null) {
            cont.resume(AppleSignInResult.Failed("Apple sign-in bridge is not installed"))
        } else {
            provider { idToken, rawNonce, error ->
                val result = when {
                    idToken != null && rawNonce != null ->
                        AppleSignInResult.Success(AppleCredential(idToken, rawNonce))
                    error != null -> AppleSignInResult.Failed(error)
                    else -> AppleSignInResult.Cancelled
                }
                cont.resume(result)
            }
        }
    }
}

@Composable
actual fun rememberAppleSignInHelper(): AppleSignInHelper = AppleSignInHelper()

/**
 * Swift installs the actual ASAuthorizationController flow here at startup (see
 * iOSApp.swift). The closure receives a callback it must invoke with exactly one
 * of these shapes:
 *   - (identityToken, rawNonce, null) on success
 *   - (null, null, null)              when the user cancelled the sheet
 *   - (null, null, reason)            on failure
 */
object IosAppleSignIn {
    var provider: ((onResult: (String?, String?, String?) -> Unit) -> Unit)? = null
}
