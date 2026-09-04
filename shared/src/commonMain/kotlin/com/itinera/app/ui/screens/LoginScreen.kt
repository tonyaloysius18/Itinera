package com.itinera.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itinera.app.data.AuthService
import com.itinera.app.data.rememberGoogleSignInHelper
import com.itinera.app.data.rememberAppleSignInHelper
import com.itinera.app.getPlatform
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.resources.Res
import com.itinera.app.resources.arizonia_regular
import com.itinera.app.resources.caudex_bold
import com.itinera.app.resources.ic_apple
import com.itinera.app.resources.ic_google
import com.itinera.app.resources.itinera_logo
import com.itinera.app.resources.login_bg
import com.itinera.app.ui.components.PlaneLoader
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource


@Composable
fun LoginScreen(
    authService: AuthService,
    prefillEmail: String = "",
    onAuthed: () -> Unit,
    onCreateAccount: () -> Unit,
    onMessage: (String) -> Unit,
    onAppleSignIn: (suspend () -> Unit)? = null,
) {
    val s = LocalStrings.current

    val isIos = remember { getPlatform().name.startsWith("iOS", ignoreCase = true) }

    var email by remember { mutableStateOf(prefillEmail) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val googleHelper = rememberGoogleSignInHelper()
    val appleHelper = rememberAppleSignInHelper()

    val displayFont = FontFamily(Font(Res.font.arizonia_regular))
    val taglineFont = FontFamily(Font(Res.font.caudex_bold))

    val onPanel = Color.White
    val onPanelMuted = Color.White.copy(alpha = 0.62f)

    fun attemptLogin() {
        if (email.isBlank() || password.isBlank()) {
            onMessage(s.fillAllFields)
            return
        }
        loading = true
        scope.launch {
            try {
                authService.signIn(email.trim(), password)
                loading = false
                onAuthed()
            } catch (_: Exception) {
                loading = false
                onMessage(s.invalidCredentials)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.login_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.00f to Color.Black.copy(alpha = 0.15f),
                    0.30f to Color.Black.copy(alpha = 0.05f),
                    0.62f to Color.Black.copy(alpha = 0.40f),
                    1.00f to Color.Black.copy(alpha = 0.65f),
                )
            )
        )

        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Itinera",
                        fontFamily = displayFont,
                        style = MaterialTheme.typography.displayLarge,
                        color = onPanel,
                    )
                    Spacer(Modifier.width(12.dp))
                    Image(
                        painter = painterResource(Res.drawable.itinera_logo),
                        contentDescription = s.itineraLogo,
                        colorFilter = ColorFilter.tint(onPanel),
                        modifier = Modifier.height(90.dp).padding(top = 2.dp, end = 2.dp),
                    )
                }
                Text(
                    s.appTagline,
                    fontFamily = taglineFont,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.offset(y = (-20).dp),
                )
            }

            Spacer(Modifier.weight(1f))

            // ── The panel ────────────────────────────────────────────────────
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(Color(0xFF121418).copy(alpha = 0.45f))
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.08f)),
                ) {
                    Column(Modifier.padding(horizontal = 13.dp, vertical = 9.dp)) {
                        Text(
                            s.email,
                            style = MaterialTheme.typography.labelSmall,
                            color = onPanelMuted,
                        )
                        EmailFieldWithSuggestions(
                            email = email,
                            onEmailChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            textColor = onPanel,
                        )
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                s.password,
                                style = MaterialTheme.typography.labelSmall,
                                color = onPanelMuted,
                            )
                            BasicTextField(
                                value = password,
                                onValueChange = { password = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = onPanel),
                                cursorBrush = SolidColor(onPanel),
                                visualTransformation = if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Icon(
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) s.hidePassword else s.showPassword,
                            tint = onPanelMuted,
                            modifier = Modifier.size(20.dp).clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { passwordVisible = !passwordVisible },
                        )
                    }
                }

                Text(
                    s.forgotPassword,
                    style = MaterialTheme.typography.bodySmall,
                    color = onPanelMuted,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clickable {
                            resetEmail = email
                            showResetDialog = true
                        },
                )

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { attemptLogin() },
                    enabled = !loading,
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .width(220.dp)
                        .height(50.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    if (loading) PlaneLoader() else Text(s.logIn)
                }

                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCreateAccount() }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        s.newHere,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onPanelMuted,
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        s.createAccount,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                    Text(
                        s.orDivider,
                        modifier = Modifier.padding(horizontal = 10.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = onPanelMuted,
                    )
                    HorizontalDivider(Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProviderButton(
                        iconPainter = painterResource(Res.drawable.ic_google),
                        label = if (isIos) "Google" else s.continueGoogle,
                        tintIcon = false,
                        modifier = Modifier.weight(1f),
                    ) {
//                        scope.launch {
//                            try {
//                                val tokens = googleHelper.signIn()
//                                if (tokens != null) {
//                                    authService.signInWithGoogle(tokens.idToken, tokens.accessToken)
//                                    onAuthed()
//                                }
//                            } catch (e: Exception) {
//                                val msg = e.message.orEmpty()
//                                // A cancelled sheet isn't a failure worth a pill.
//                                if (!msg.contains("cancel", ignoreCase = true)) {
//
//                                    onMessage(s.googleSignInFailed)
//                                    println("ITINERA: GOOGLE SIGN-IN FAILED — $msg")
//                                }
//                            }
//                        }
                        scope.launch {
                            try {
                                val tokens = googleHelper.signIn()

                                if (tokens != null) {
                                    authService.signInWithGoogle(
                                        tokens.idToken,
                                        tokens.accessToken
                                    )

                                    onAuthed()
                                }
                            } catch (e: Exception) {
                                val msg = e.message.orEmpty()

                                if (!msg.contains("cancel", ignoreCase = true)) {
                                    onMessage(s.googleSignInFailed)

                                    println(
                                        "ITINERA: GOOGLE SIGN-IN FAILED — " +
                                                "${e::class.simpleName}: $msg"
                                    )

                                    e.printStackTrace()
                                }
                            }
                        }
                    }

                    if (isIos) {
                        ProviderButton(
                            iconPainter = painterResource(Res.drawable.ic_apple),
                            label = "Apple",
                            tintIcon = true,
                            modifier = Modifier.weight(1f),
                        ) {
                            scope.launch {
                                try {
                                    // Prefer an injected handler (tests / overrides); otherwise
                                    // run the native ASAuthorization flow via the helper.
                                    val handler = onAppleSignIn
                                    if (handler != null) {
                                        handler()
                                        onAuthed()
                                    } else {
                                        val cred = appleHelper.signIn()
                                        if (cred != null) {
                                            authService.signInWithApple(cred.idToken, cred.rawNonce)
                                            onAuthed()
                                        }
                                    }
                                } catch (e: Exception) {
                                    val msg = e.message.orEmpty()
                                    // A cancelled sheet isn't a failure worth a pill.
                                    if (!msg.contains("cancel", ignoreCase = true)) {
                                        onMessage(s.appleSignInFailed)
                                        println(
                                            "ITINERA: APPLE SIGN-IN FAILED — " +
                                                    "${e::class.simpleName}: $msg"
                                        )
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(s.forgotPassword) },
            shape = RoundedCornerShape(20.dp),
            text = {
                Column {
                    Text(s.resetPasswordPrompt)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text(s.email) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                            onMessage(s.resetEmailSent)
                        } catch (_: Exception) {
                            onMessage(s.resetEmailFailed)
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

@Composable
private fun ProviderButton(
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    tintIcon: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = iconPainter,
            contentDescription = null,
            colorFilter = if (tintIcon) ColorFilter.tint(Color.White) else null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(9.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = 1,
        )
    }
}