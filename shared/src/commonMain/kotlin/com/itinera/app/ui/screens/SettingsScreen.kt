package com.itinera.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.UserProfile
import com.itinera.app.ui.components.TopBar

@Composable
fun SettingsScreen(
    profile: UserProfile,
    onEditProfile: () -> Unit,
    onAccount: () -> Unit,
    onAppearance: () -> Unit,
    onOpenLanguage: () -> Unit,
    onNotifications: () -> Unit,
    onArchivedTrips: () -> Unit,
    onExportTrips: () -> Unit,
    onBackupStatus: () -> Unit,
    onHelp: () -> Unit,
    onAbout: () -> Unit,
) {
    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }

    val s = LocalStrings.current
    val primary = MaterialTheme.colorScheme.primary

    Column(Modifier.fillMaxSize()) {
        TopBar(s.settings)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
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

            // Group 1 — account & display
            Spacer(Modifier.height(20.dp))
            SettingsCard {
                SettingsRow(Icons.Filled.ManageAccounts, s.account, primary, onClick = onAccount)
                ThinDivider()
                SettingsRow(Icons.Filled.DarkMode, s.appearance, primary, onClick = onAppearance)
                ThinDivider()
                SettingsRow(Icons.Filled.Translate, s.language, primary, onClick = onOpenLanguage)
                ThinDivider()
                SettingsRow(Icons.Filled.Notifications, s.notifications, primary, onClick = onNotifications)
            }

            // Group 2 — trips & data
            Spacer(Modifier.height(16.dp))
            SettingsCard {
                SettingsRow(Icons.Filled.Archive, s.archivedTrips.toTitleCase(), primary, onClick = onArchivedTrips)
                ThinDivider()
                SettingsRow(Icons.Filled.Download, s.exportTrips.toTitleCase(), primary, onClick = onExportTrips)
                ThinDivider()
                SettingsRow(Icons.Filled.Backup, s.backupStatus, primary, onClick = onBackupStatus)
            }

            // Group 3 — support
            Spacer(Modifier.height(16.dp))
            SettingsCard {
                SettingsRow(Icons.Filled.HelpOutline, s.help, primary, onClick = onHelp)
                ThinDivider()
                SettingsRow(Icons.Filled.Info, s.about, primary, onClick = onAbout)
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(content = content)
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
    val s = LocalStrings.current
    val primary = MaterialTheme.colorScheme.primary

    // Show photo if we have one (bytes take priority over URL for instant preview)
    val imageModel = profile.photoBytes ?: profile.photoUrl.takeIf { it.isNotBlank() }

    Surface(modifier = Modifier.size(size), shape = CircleShape, color = primary.copy(alpha = 0.15f)) {
        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = s.profilePhoto,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape),
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(profile.initials, color = primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}