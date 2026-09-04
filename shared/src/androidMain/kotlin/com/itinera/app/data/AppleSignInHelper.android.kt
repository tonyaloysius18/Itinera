package com.itinera.app.data

import androidx.compose.runtime.Composable

/**
 * Sign in with Apple is only offered on iOS (the button is iOS-only). This
 * Android actual exists solely to satisfy the expect/actual contract and always
 * returns null.
 */
actual class AppleSignInHelper {
    actual suspend fun signIn(): AppleCredential? = null
}

@Composable
actual fun rememberAppleSignInHelper(): AppleSignInHelper = AppleSignInHelper()
