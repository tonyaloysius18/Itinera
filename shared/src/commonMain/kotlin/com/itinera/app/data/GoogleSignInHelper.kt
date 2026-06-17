// GoogleSignInHelper.kt (commonMain)
package com.itinera.app.data

import androidx.compose.runtime.Composable

data class GoogleTokens(val idToken: String, val accessToken: String)

expect class GoogleSignInHelper {
    suspend fun signIn(): GoogleTokens?
}

@Composable
expect fun rememberGoogleSignInHelper(): GoogleSignInHelper