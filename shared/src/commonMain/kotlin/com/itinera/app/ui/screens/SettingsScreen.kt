package com.itinera.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.UserProfile
import com.itinera.app.ui.components.TopBar

private val AccentRed = Color(0xFFE03131)

@Composable
fun SettingsScreen(
    profile: UserProfile,
    onEditProfile: () -> Unit,
    onAppearance: () -> Unit,
    onOpenLanguage: () -> Unit,
    onArchivedTrips: () -> Unit,
    onLogOut: () -> Unit,
    onDeleteAccount: () -> Unit,                    // ⬅ ADD
) {

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }


    val s = LocalStrings.current
    val primary = MaterialTheme.colorScheme.primary

    var showDeleteDialog by remember { mutableStateOf(false) }    // ⬅ ADD

    Column(Modifier.fillMaxSize()) {
        TopBar(s.settings)

        Column(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            // Profile card
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onEditProfile),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(profile, size = 48.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(profile.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(profile.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }

            // Settings group card
            Spacer(Modifier.height(20.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Column {
                    SettingsRow(Icons.Filled.DarkMode, s.appearance, primary, onClick = onAppearance)
                    ThinDivider()
                    SettingsRow(Icons.Filled.Translate, s.language, primary, onClick = onOpenLanguage)
                    ThinDivider()
                    SettingsRow(Icons.Filled.Archive, s.archivedTrips.toTitleCase(), primary, onClick = onArchivedTrips)
                    ThinDivider()
                    SettingsRow(Icons.Filled.Download, s.exportTrips.toTitleCase(), primary) {}
                    ThinDivider()
                    SettingsRow(Icons.Filled.Info, s.about, primary) {}
                }
            }

            // Log out — bright red rounded button
            Spacer(Modifier.height(120.dp))

            Button(
                onClick = onLogOut,
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 60.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentRed,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.logOut, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            // Delete account — subtle text button below log out
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { showDeleteDialog = true },              // ⬅ ADD: opens confirmation
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 80.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentRed,
                    contentColor = Color.White,
                    ),
            ) {
                Text(
                    s.deleteAccount,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }

            Spacer(Modifier.weight(1f))
        }
    }

    // Confirmation dialog — deletion is irreversible
    if (showDeleteDialog) {                                      // ⬅ ADD
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(s.deleteAccount) },
            text = { Text(s.deleteAccountConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteAccount()
                }) {
                    Text(s.delete, color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(s.cancel) }
            },
        )
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, iconTint: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    }
}

@Composable
private fun ThinDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
    )
}

@Composable
fun ProfileAvatar(profile: UserProfile, size: androidx.compose.ui.unit.Dp) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(modifier = Modifier.size(size), shape = CircleShape, color = primary.copy(alpha = 0.15f)) {
        Box(contentAlignment = Alignment.Center) {
            Text(profile.initials, color = primary, fontWeight = FontWeight.SemiBold)
        }
    }
}