package com.itinera.app.data

import androidx.compose.runtime.Composable

/**
 * Sign in with Apple is only offered on iOS (the button is iOS-only). This
 * Android actual exists solely to satisfy the expect/actual contract; it is
 * never reached from the UI, so it reports a failure rather than pretending
 * the user cancelled.
 */
actual class AppleSignInHelper {
    actual suspend fun signIn(): AppleSignInResult =
        AppleSignInResult.Failed("Sign in with Apple is not available on Android")
}

@Composable
actual fun rememberAppleSignInHelper(): AppleSignInHelper = AppleSignInHelper()
