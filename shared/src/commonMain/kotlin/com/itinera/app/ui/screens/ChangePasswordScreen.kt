package com.itinera.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.PlaneLoader
import com.itinera.app.ui.components.TopBar
import kotlinx.coroutines.launch

/*
 * ─── New string keys (EN + FR) ──────────────────────────────────────────────
 *   currentPassword     "Current password"        / "Mot de passe actuel"
 *   confirmPassword     "Confirm new password"    / "Confirmer le mot de passe"
 *   passwordsDontMatch  "Passwords don't match"   / "Les mots de passe ne correspondent pas"
 *   wrongCurrentPassword "That password isn't right" / "Mot de passe incorrect"
 *   couldntChangePassword "Couldn't change your password. Please try again."
 *                       / "Impossible de changer le mot de passe. Réessayez."
 *   passwordChanged     "Password changed"        / "Mot de passe modifié"
 *   reqLength           "At least 8 characters"   / "Au moins 8 caractères"
 *   reqNumber           "A number"                / "Un chiffre"
 *   reqLetter           "A letter"                / "Une lettre"
 *   strengthWeak        "Weak"                    / "Faible"
 *   strengthFair        "Fair"                    / "Moyen"
 *   strengthStrong      "Strong"                  / "Fort"
 *   noPasswordAccount   "You signed in with Google, so there's no password to change."
 *                       / "Vous vous êtes connecté avec Google : aucun mot de passe à changer."
 *
 * Reused: changePassword, newPassword, showPassword, hidePassword, save, back.
 * `passwordTooShort` is superseded by the requirement checklist.
 */

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    /** Called after a successful change — pop back and show a confirmation. */
    onChanged: () -> Unit,
    /**
     * Verifies the current password. Must throw on a wrong password.
     *
     * Firebase requires a recent login before updatePassword, so this isn't
     * only a safety check — it's what makes the update reliable. See the
     * AuthService note at the bottom of this file.
     */
    reauthenticate: suspend (currentPassword: String) -> Unit,
    updatePassword: suspend (newPassword: String) -> Unit,
    /**
     * False for Google/Apple sign-in accounts, which have no password to
     * change. Showing the form to them would be a dead end.
     */
    hasPasswordAccount: Boolean = true,
) {
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()

    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNext by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    // Live requirements — visible while typing rather than as a rejection after
    // the fact, which is what "Password too short" on submit amounts to.
    val hasLength = next.length >= 8
    val hasNumber = next.any { it.isDigit() }
    val hasLetter = next.any { it.isLetter() }
    val hasSpecial = next.any { !it.isLetterOrDigit() }
    val metCount = listOf(hasLength, hasNumber, hasLetter, hasSpecial).count { it }
    val matches = confirm.isNotEmpty() && next == confirm

    val blocker: String? = when {
        current.isBlank() -> s.currentPassword
        metCount < 4 -> null            // the checklist is already saying it
        confirm.isBlank() -> s.confirmPassword
        !matches -> s.passwordsDontMatch
        else -> null
    }
    val canSave = current.isNotBlank() && metCount == 4 && matches && !loading

    fun attempt() {
        error = null
        loading = true
        scope.launch {
            try {
                // Reauthenticate first. A wrong current password fails here,
                // before anything is changed.
                reauthenticate(current)
            } catch (e: Exception) {
                loading = false
                error = s.wrongCurrentPassword
                println("ITINERA: REAUTH FAILED — ${e.message}")
                return@launch
            }
            try {
                updatePassword(next)
                loading = false
                onChanged()
            } catch (e: Exception) {
                loading = false
                // Raw Firebase text isn't for users; the detail goes to the log.
                error = s.couldntChangePassword
                println("ITINERA: PASSWORD UPDATE FAILED — ${e.message}")
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(s.changePassword, onBack = onBack)

            if (!hasPasswordAccount) {
                // ⬅ A Google account has no password. Offering the form would be
                // a dead end ending in an opaque Firebase error.
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        s.noPasswordAccount,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                    )
                }
                return@Column
            }

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(12.dp))

                PasswordCard {
                    PasswordField(
                        label = s.currentPassword,
                        value = current,
                        visible = showCurrent,
                        onToggleVisible = { showCurrent = !showCurrent },
                        onValueChange = { current = it; error = null },
                    )
                }

                Spacer(Modifier.height(20.dp))

                PasswordCard {
                    PasswordField(
                        label = s.newPassword,
                        value = next,
                        visible = showNext,
                        onToggleVisible = { showNext = !showNext },
                        onValueChange = { next = it; error = null },
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                    )
                    PasswordField(
                        label = s.confirmPassword,
                        value = confirm,
                        visible = showNext,          // same toggle: they're one decision
                        onToggleVisible = { showNext = !showNext },
                        onValueChange = { confirm = it; error = null },
                        trailingCheck = matches,
                    )
                }

                if (next.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    StrengthMeter(metCount = metCount)
                    Spacer(Modifier.height(12.dp))
                    Requirement(s.reqLength, hasLength)
                    Requirement(s.reqNumber, hasNumber)
                    Requirement(s.reqLetter, hasLetter)
                    Requirement(s.specialCharacter, hasSpecial)
                }

                if (error != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(Modifier.height(140.dp))
            }
        }

        if (hasPasswordAccount) {
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp, bottom = 44.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (blocker != null && !canSave) {
                    Text(
                        blocker,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                Button(
                    onClick = { attempt() },
                    enabled = canSave,
                    shape = RoundedCornerShape(26.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 0.dp),
                    modifier = Modifier.height(52.dp),
                ) {
                    if (loading) PlaneLoader() else Text(s.save)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PasswordCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(content = content)
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    onValueChange: (String) -> Unit,
    trailingCheck: Boolean = false,
) {
    val s = LocalStrings.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = onSurface.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(1.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = if (visible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // A green tick on the confirm field says "these match" without a
        // sentence of error text underneath.
        if (trailingCheck) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = StrengthStrong,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
        }

        Icon(
            if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
            contentDescription = if (visible) s.hidePassword else s.showPassword,
            tint = onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .size(20.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onToggleVisible() },
        )
    }
}

private val StrengthWeak = Color(0xFFD8703C)
private val StrengthFair = Color(0xFFE0A93C)
private val StrengthStrong = Color(0xFF1D9E75)

/** Four segments, one per requirement met — no arbitrary scoring. */
@Composable
private fun StrengthMeter(metCount: Int) {
    val s = LocalStrings.current
    val color = when (metCount) {
        4 -> StrengthStrong
        3 -> StrengthFair
        2 -> StrengthFair
        else -> StrengthWeak
    }
    val label = when (metCount) {
        4 -> s.strengthStrong
        3 -> s.strengthFair
        2 -> s.strengthFair
        else -> s.strengthWeak
    }
    Column {
        Row(
            Modifier.fillMaxWidth().height(5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(4) { index ->
                val filled = index < metCount
                val alpha by animateFloatAsState(
                    targetValue = if (filled) 1f else 0.12f,
                    animationSpec = tween(200),
                )
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (filled) color.copy(alpha = alpha)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        ),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun Requirement(label: String, met: Boolean) {
    Row(
        Modifier.padding(vertical = 3.dp).animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    if (met) StrengthStrong
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (met) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp),
                )
            }
        }
        Spacer(Modifier.width(9.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = if (met) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        )
    }
}

/*
 * ─── AuthService additions needed ───────────────────────────────────────────
 *
 * `updatePassword` already exists. Add reauthentication and a way to tell
 * whether the account has a password at all:
 *
 *     suspend fun reauthenticate(currentPassword: String) {
 *         val user = Firebase.auth.currentUser ?: error("Not signed in")
 *         val email = user.email ?: error("No email on account")
 *         user.reauthenticate(EmailAuthProvider.credential(email, currentPassword))
 *     }
 *
 *     /** False for Google/Apple sign-in — there's no password to change. */
 *     val hasPasswordProvider: Boolean
 *         get() = Firebase.auth.currentUser
 *             ?.providerData
 *             ?.any { it.providerId == "password" } == true
 *
 * imports: dev.gitlive.firebase.auth.EmailAuthProvider
 *
 * Then in App.kt:
 *
 *     Screen.ChangePassword -> ChangePasswordScreen(
 *         onBack = { navigator.back() },
 *         onChanged = { navigator.back() },   // plus a confirmation pill
 *         reauthenticate = { repository.authService.reauthenticate(it) },
 *         updatePassword = { repository.authService.updatePassword(it) },
 *         hasPasswordAccount = repository.authService.hasPasswordProvider,
 *     )
 */