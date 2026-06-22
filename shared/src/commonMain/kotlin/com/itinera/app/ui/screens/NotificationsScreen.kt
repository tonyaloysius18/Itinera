package com.itinera.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.ReminderOffset
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar

/**
 * Notifications settings: a master on/off plus the reminder lead-time choice.
 * `offsetMinutes` is the persisted UserProfile.reminderOffsetMinutes
 * (0 = off, else 30 / 60 / 1440).
 */
@Composable
fun NotificationsScreen(
    offsetMinutes: Int,
    hasPermission: Boolean,
    onChangeOffset: (Int) -> Unit,
    onRequestPermission: () -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalStrings.current
    val enabled = offsetMinutes != ReminderOffset.OFF

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TopBar(s.notifications, onBack = onBack, modifier = Modifier.offset(x = (-15).dp))

            Spacer(Modifier.height(8.dp))

            // Master toggle card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(s.remindMe, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            s.notificationsSoonSubtitle, // reuse existing subtitle string
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { on ->
                            if (on) {
                                onRequestPermission()
                                onChangeOffset(ReminderOffset.HOUR_1) // default when turned on
                            } else {
                                onChangeOffset(ReminderOffset.OFF)
                            }
                        },
                    )
                }
            }

            // Offset choices (only meaningful when enabled)
            if (enabled) {
                Text(
                    s.remindMe,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 8.dp),
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    Column {
                        ReminderOffset.choices.forEachIndexed { index, choice ->
                            OffsetRow(
                                label = offsetLabel(choice),
                                selected = offsetMinutes == choice,
                                onClick = { onChangeOffset(choice) },
                            )
                            if (index < ReminderOffset.choices.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                )
                            }
                        }
                    }
                }

                if (!hasPermission) {
                    Text(
                        "Allow notifications in system settings to receive reminders.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp, top = 12.dp),
                    )
                }
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

// Simple human labels; swap for localized strings if you add keys.
private fun offsetLabel(minutes: Int): String = when (minutes) {
    ReminderOffset.MIN_15 -> "15 minutes before"
    ReminderOffset.MIN_30 -> "30 minutes before"
    ReminderOffset.HOUR_1 -> "1 hour before"
    ReminderOffset.DAY_1 -> "1 day before"
    else -> "$minutes minutes before"
}

@Composable
private fun OffsetRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) primary else MaterialTheme.colorScheme.onSurface,
        )
        RadioButton(selected = selected, onClick = onClick)
    }
}