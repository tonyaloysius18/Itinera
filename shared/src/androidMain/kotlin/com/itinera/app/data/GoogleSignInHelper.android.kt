package com.itinera.app.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

private const val WEB_CLIENT_ID = "780952897576-711qksnhgfp41t0cln1od7gimi7vsphu.apps.googleusercontent.com"

actual class GoogleSignInHelper(private val context: Context) {
    actual suspend fun signIn(): GoogleSignInResult {
        val credentialManager = CredentialManager.create(context)

        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val cred = GoogleIdTokenCredential.createFrom(result.credential.data)
            GoogleSignInResult.Success(
                GoogleTokens(
                    idToken = cred.idToken,
                    accessToken = null
                )
            )
        } catch (e: GetCredentialCancellationException) {
            // The user dismissed the Credential Manager sheet — not a failure.
            GoogleSignInResult.Cancelled
        } catch (e: GetCredentialException) {
            Log.e("GoogleSignIn", "Credential Manager error: ${e.type} ${e.message}")
            GoogleSignInResult.Failed("${e.type}: ${e.message.orEmpty()}")
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Unexpected error: ${e.message}", e)
            GoogleSignInResult.Failed(e.message ?: e::class.simpleName.orEmpty())
        }
    }
}

@Composable
actual fun rememberGoogleSignInHelper(): GoogleSignInHelper {
    val context = LocalContext.current
    return GoogleSignInHelper(context)
}
