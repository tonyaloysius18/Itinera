package com.itinera.app.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itinera.app.ReminderOffset
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.UserProfile
import com.itinera.app.ui.components.TopBar


private val ColorAccount = Color(0xFF007AFF)
private val ColorAppearance = Color(0xFF5856D6)
private val ColorLanguage = Color(0xFF34C759)
private val ColorNotifications = Color(0xFFFF3B30)
private val ColorWorldClock = Color(0xFF32ADE6)
private val ColorWeather = Color(0xFFFFCC00)
private val ColorTranslate = Color(0xFF5856D6)
private val ColorEmergency = Color(0xFFFF2D55) // Slightly different red
private val ColorCompass = Color(0xFF30B0C7)
private val ColorArchive = Color(0xFF8E8E93)
private val ColorExport = Color(0xFFFF9500)
private val ColorBackup = Color(0xFF5AC8FA)
private val ColorHelp = Color(0xFF5856D6)
private val ColorFeedback = Color(0xFF34C759)
private val ColorAbout = Color(0xFF8E8E93)

@Composable
fun SettingsScreen(
    profile: UserProfile,
    onEditProfile: () -> Unit,
    onAccount: () -> Unit,
    onAppearance: () -> Unit,
    onOpenLanguage: () -> Unit,
    onNotifications: () -> Unit,
    onWorldClock: () -> Unit,
    onEmergency: () -> Unit,
    onWeather: () -> Unit,
    onTranslate: () -> Unit,
    onCompass: () -> Unit,
    onArchivedTrips: () -> Unit,
    onExportTrips: () -> Unit,
    onBackupStatus: () -> Unit,
    onHelp: () -> Unit,
    onFeedback: () -> Unit,
    onAbout: () -> Unit,
    /**
     * ⬅ ADD — current values shown at the end of their rows. A settings row
     * that states its value saves a tap and makes the screen informative rather
     * than a menu. All optional, so existing call sites still compile; blank
     * means the row renders exactly as before.
     */
    appearanceValue: String = "",
    languageValue: String = "",
    notificationsValue: String = "",
    appVersion: String = "Itinera 1.0.0",
) {
    val s = LocalStrings.current
    val scrollState = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }

    val resolvedNotificationsValue = notificationsValue.ifBlank {
        when (profile.reminderOffsetMinutes) {
            ReminderOffset.OFF -> s.off
            else -> "${profile.reminderOffsetMinutes} min"
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(s.settings)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onEditProfile),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileAvatar(profile, size = 48.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            profile.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            profile.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    )
                }
            }

            // ⬅ ADD — the groups were already named in comments; they just never
            // reached the screen.
            SectionLabel(s.preferences)
            SettingsCard {
                SettingsRow(Icons.Filled.ManageAccounts, s.account, ColorAccount, onClick = onAccount)
                ThinDivider()
                SettingsRow(Icons.Filled.DarkMode, s.appearance, ColorAppearance, value = appearanceValue, onClick = onAppearance)
                ThinDivider()
                SettingsRow(Icons.Filled.Language, s.language, ColorLanguage, value = languageValue, onClick = onOpenLanguage)
                ThinDivider()
                SettingsRow(Icons.Filled.Notifications, s.notifications, ColorNotifications, value = resolvedNotificationsValue, onClick = onNotifications)
            }

            SectionLabel(s.travelTools)
            SettingsCard {
                SettingsRow(Icons.Filled.Public, s.worldClock, ColorWorldClock, onClick = onWorldClock)
                ThinDivider()
                SettingsRow(Icons.Filled.Cloud, s.weather, ColorWeather, onClick = onWeather)
                ThinDivider()
                SettingsRow(Icons.Filled.Translate, s.translate, ColorTranslate, onClick = onTranslate)
                ThinDivider()
                SettingsRow(Icons.Filled.MedicalServices, s.emergency, ColorEmergency, onClick = onEmergency)
                ThinDivider()
                SettingsRow(Icons.Filled.Explore, s.compass, ColorCompass, onClick = onCompass)
            }

            SectionLabel(s.tripsAndData)
            SettingsCard {
                // ⬅ CHANGED — was s.archivedTrips.toTitleCase(). Fix the casing in
                // Strings.kt rather than transforming it here.
                SettingsRow(Icons.Filled.Archive, s.archivedTrips, ColorArchive, onClick = onArchivedTrips)
                ThinDivider()
                SettingsRow(Icons.Filled.Download, s.exportTrips, ColorExport, onClick = onExportTrips)
                ThinDivider()
                SettingsRow(Icons.Filled.Backup, s.backupStatus, ColorBackup, onClick = onBackupStatus)
            }

            SectionLabel(s.support)
            SettingsCard {
                SettingsRow(Icons.Filled.HelpOutline, s.help, ColorHelp, onClick = onHelp)
                ThinDivider()
                SettingsRow(Icons.Filled.Feedback, s.sendFeedback, ColorFeedback, onClick = onFeedback)
                ThinDivider()
                SettingsRow(Icons.Filled.Info, s.about, ColorAbout, onClick = onAbout)
            }

            if (appVersion.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                Text(
                    appVersion,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    textAlign = TextAlign.Center,
                )
            }

            // Clearance for the floating bottom pill nav bar.
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            Spacer(Modifier.height(110.dp))
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        modifier = Modifier.padding(start = 6.dp, top = 20.dp, bottom = 7.dp),
    )
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
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    value: String = "",
    onClick: () -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            // ⬅ CHANGED — was 16.dp vertical. With the 30.dp icon container the
            // row is still comfortably over the 48.dp touch target, and 14 rows
            // at 4.dp less each is most of a screenful of height back.
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ⬅ CHANGED — was a bare 22.dp icon tinted primary on every row. Fourteen
        // identical blue icons read as a stripe down the edge; neutral icons let
        // the labels lead.
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(Modifier.width(13.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (value.isNotBlank()) {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = onSurface.copy(alpha = 0.45f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(6.dp))
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ThinDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 57.dp),   // ⬅ CHANGED — aligns to the new label x
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
    )
}

@Composable
fun ProfileAvatar(profile: UserProfile, size: Dp) {
    val s = LocalStrings.current
    val primary = MaterialTheme.colorScheme.primary

    // Photo if we have one — bytes take priority over URL for instant preview.
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
