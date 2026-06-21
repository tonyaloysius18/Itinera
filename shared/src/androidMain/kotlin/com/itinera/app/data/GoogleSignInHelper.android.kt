package com.itinera.app.data

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

import android.util.Log
import androidx.credentials.exceptions.GetCredentialException

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption

private const val WEB_CLIENT_ID = "780952897576-711qksnhgfp41t0cln1od7gimi7vsphu.apps.googleusercontent.com"

actual class GoogleSignInHelper(private val context: Context) {
    actual suspend fun signIn(): GoogleTokens? {
        val credentialManager = CredentialManager.create(context)
        
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val cred = GoogleIdTokenCredential.createFrom(result.credential.data)
            GoogleTokens(idToken = cred.idToken, accessToken = cred.idToken)
        } catch (e: GetCredentialException) {
            Log.e("GoogleSignIn", "Credential Manager error: ${e.type} ${e.message}")
            // Re-throw so the UI can catch it and show a meaningful message or ignore cancellations
            throw e
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Unexpected error: ${e.message}", e)
            throw e
        }
    }
}

@Composable
actual fun rememberGoogleSignInHelper(): GoogleSignInHelper {
    val context = LocalContext.current
    return GoogleSignInHelper(context)
}