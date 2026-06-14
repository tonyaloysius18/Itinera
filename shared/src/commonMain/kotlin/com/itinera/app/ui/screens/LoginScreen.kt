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
import androidx.compose.runtime.rememberCoroutineScope          // ⬅ ADD
import kotlinx.coroutines.launch                                 // ⬅ ADD
import com.itinera.app.data.AuthService                          // ⬅ ADD


/**
 * Login gate. The email/password button now performs a REAL Firebase sign-in
 * via AuthService. Google and Apple buttons remain mocks (they need separate
 * OAuth / native flows) and still call onAuthed() for now.
 */

val isIos = getPlatform().name.startsWith("iOS", ignoreCase = true)

@Composable
fun LoginScreen(
    authService: AuthService,                 // ⬅ ADD: passed in from App.kt
    onAuthed: () -> Unit,
    onCreateAccount: () -> Unit,
) {
    val s = LocalStrings.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }     // ⬅ ADD: inline error text
    var loading by remember { mutableStateOf(false) }           // ⬅ ADD: disables button during call
    val scope = rememberCoroutineScope()                        // ⬅ ADD

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

    // Performs the real sign-in. Suspends, so it runs in a coroutine.
    fun attemptLogin() {                                         // ⬅ ADD
        if (email.isBlank() || password.isBlank()) {
            error = s.fillAllFields
            return
        }
        error = null
        loading = true
        scope.launch {
            try {
                authService.signIn(email, password)
                loading = false
                onAuthed()                                      // success → navigate in
            } catch (e: Exception) {
                loading = false
                error = s.loginFailed                           // wrong credentials / network / etc.
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
            OutlinedTextField(
                value = email, onValueChange = { email = it; error = null },
                label = { Text(s.email) }, singleLine = true,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it; error = null },
                label = { Text(s.password) }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Text(
                s.forgotPassword,
                style = MaterialTheme.typography.bodySmall,
                color = onImage,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )

            // Inline error message (shown on failure)
            if (error != null) {                                 // ⬅ ADD
                Spacer(Modifier.height(8.dp))
                Text(
                    error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { attemptLogin() },                    // ⬅ CHANGED: real sign-in
                enabled = !loading,                              // ⬅ disable while working
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (loading) {                                   // ⬅ spinner while working
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
            OutlinedButton(onClick = onAuthed, modifier = Modifier.fillMaxWidth()) {   // still mock
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
}