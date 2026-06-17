package com.itinera.app.data

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

private const val WEB_CLIENT_ID = "780952897576-711qksnhgfp41t0cln1od7gimi7vsphu.apps.googleusercontent.com"

actual class GoogleSignInHelper(private val context: Context) {
    actual suspend fun signIn(): GoogleTokens? {        // ⬅ CHANGED return type
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val cred = GoogleIdTokenCredential.createFrom(result.credential.data)
            GoogleTokens(idToken = cred.idToken, accessToken = cred.idToken)   // ⬅ CHANGED
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
actual fun rememberGoogleSignInHelper(): GoogleSignInHelper {
    val context = LocalContext.current
    return GoogleSignInHelper(context)
}