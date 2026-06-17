package com.itinera.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import com.itinera.app.resources.arizonia_regular
import com.itinera.app.resources.caudex_bold
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import org.jetbrains.compose.resources.painterResource
import com.itinera.app.resources.Res
import com.itinera.app.resources.itinera_logo
import com.itinera.app.resources.login_bg
import com.itinera.app.resources.ic_google
import com.itinera.app.resources.ic_apple
import com.itinera.app.getPlatform
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.launch
import com.itinera.app.data.AuthService
import com.itinera.app.data.rememberGoogleSignInHelper


/**
 * Login gate. The email/password button performs a real Firebase sign-in via
 * AuthService. Validation and failure messages surface through the app-level
 * pill via onMessage. Google and Apple buttons remain mocks for now.
 */

val isIos = getPlatform().name.startsWith("iOS", ignoreCase = true)

@Composable
fun LoginScreen(
    authService: AuthService,
    onAuthed: () -> Unit,
    onCreateAccount: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val s = LocalStrings.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val googleHelper = rememberGoogleSignInHelper()


    val displayFont = FontFamily(Font(Res.font.arizonia_regular))
    val taglineFont = FontFamily(Font(Res.font.caudex_bold))

    // Light colors for text/fields so they read on a darkened photo.
    val onImage = Color.White
    val onImageMuted = Color.White.copy(alpha = 0.75f)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = onImage,
        unfocusedTextColor = onImage,
        focusedBorderColor = onImage,
        unfocusedBorderColor = onImageMuted,
        focusedLabelColor = onImage,
        unfocusedLabelColor = onImageMuted,
        cursorColor = onImage,
    )
    val textFieldShape = RoundedCornerShape(12.dp)


    fun attemptLogin() {
        if (email.isBlank() || password.isBlank()) {
            onMessage(s.fillAllFields)            // ⬅ pill: empty fields
            return
        }
        loading = true
        scope.launch {
            try {
                authService.signIn(email, password)
                loading = false
                onAuthed()                        // success → navigate in
            } catch (e: Exception) {
                loading = false
                onMessage(s.invalidCredentials)   // ⬅ pill: generic auth failure
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Back layer: the background image, cropped to fill the screen.
        Image(
            painter = painterResource(Res.drawable.login_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        // Middle layer: dark gradient so the form stays legible.
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.60f))
                )
            )
        )

        // Top layer: the login content.
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                Spacer(Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Itinera",
                        fontFamily = displayFont,
                        style = MaterialTheme.typography.displayLargeEmphasized,
                        color = onImage,
                    )
                    Spacer(Modifier.width(12.dp))
                    Image(
                        painter = painterResource(Res.drawable.itinera_logo),
                        contentDescription = "Itinera logo",
                        colorFilter = ColorFilter.tint(onImage),
                        modifier = Modifier.height(90.dp)
                            .padding(top = 2.dp, end = 2.dp)
                    )
                }

                Text(
                    s.appTagline,
                    fontFamily = taglineFont,
                    style = MaterialTheme.typography.titleMedium,
                    color = onImageMuted,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.offset(y = (-20).dp),
                )
            }

            Spacer(Modifier.height(130.dp))
            EmailFieldWithSuggestions(
                email = email,
                onEmailChange = { email = it },
                label = { Text(s.email) },
                //singleLine = true,
                colors = fieldColors,
                //colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text(s.password) }, singleLine = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) s.hidePassword else s.showPassword,
                            tint = onImageMuted,
                        )
                    }
                },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Text(
                s.forgotPassword,
                style = MaterialTheme.typography.bodySmall,
                color = onImage,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clickable {
                        resetEmail = email          // prefill with whatever they typed
                        showResetDialog = true
                    },
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { attemptLogin() },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(s.logIn)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f), color = onImageMuted)
                Text(s.orDivider, modifier = Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.bodySmall, color = onImageMuted)
                HorizontalDivider(Modifier.weight(1f), color = onImageMuted)
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val tokens = googleHelper.signIn()
                        if (tokens != null) {
                            try {
                                authService.signInWithGoogle(tokens.idToken, tokens.accessToken)
                                onAuthed()
                            } catch (e: Exception) {
                                onMessage("Google failed: ${e.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )  {   // still mock
                Image(
                    painter = painterResource(Res.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(s.continueGoogle)
            }

            if (isIos) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onAuthed, modifier = Modifier.fillMaxWidth()) {  // still mock
                    Image(
                        painter = painterResource(Res.drawable.ic_apple),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(onImage),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(s.continueApple)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "${s.newHere} ${s.createAccount}",
                style = MaterialTheme.typography.bodyMedium,
                color = onImageMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCreateAccount() },
            )
        }
    }
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(s.forgotPassword) },
            text = {
                Column {
                    Text(s.resetPasswordPrompt)   // "Enter your email and we'll send a reset link."
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text(s.email) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (resetEmail.isBlank()) {
                        onMessage(s.fillAllFields)
                        return@TextButton
                    }
                    showResetDialog = false
                    scope.launch {
                        try {
                            authService.sendPasswordReset(resetEmail.trim())
                            onMessage(s.resetEmailSent)   // "Password reset email sent"
                        } catch (e: Exception) {
                            onMessage(s.resetEmailFailed) // "Couldn't send reset email"
                        }
                    }
                }) { Text(s.ok) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(s.cancel) }
            },
        )
    }
}

