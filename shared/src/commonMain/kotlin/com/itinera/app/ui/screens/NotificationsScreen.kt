package com.itinera.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.ReminderOffset
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar
import com.itinera.app.ui.components.WheelPicker

/**
 * Notifications settings: a master on/off plus the reminder lead-time choice.
 * `offsetMinutes` is the persisted UserProfile.reminderOffsetMinutes
 * (0 = off, else any positive number of minutes before departure).
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

    // Is the current offset one of the presets? If not, it's a custom value.
    val isCustom = enabled && offsetMinutes !in ReminderOffset.choices

    var showCustomDialog by remember { mutableStateOf(false) }

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
                        ReminderOffset.choices.forEach { choice ->
                            OffsetRow(
                                label = offsetLabel(choice, s),
                                selected = offsetMinutes == choice,
                                onClick = { onChangeOffset(choice) },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            )
                        }
                        // Custom… row — shows the custom value when active, else a prompt
                        OffsetRow(
                            label = if (isCustom) offsetLabel(offsetMinutes, s) else s.custom,
                            selected = isCustom,
                            onClick = { showCustomDialog = true },
                        )
                    }
                }

                if (!hasPermission) {
                    Text(
                        s.allowNotificationsPrompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp, top = 12.dp),
                    )
                }
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }

    if (showCustomDialog) {
        CustomOffsetDialog(
            initialMinutes = if (isCustom) offsetMinutes else ReminderOffset.HOUR_1,
            onDismiss = { showCustomDialog = false },
            onConfirm = { total ->
                onChangeOffset(total)
                showCustomDialog = false
            },
        )
    }
}

// ---- Custom offset dialog: two wheels (hours, minutes) ----
@Composable
private fun CustomOffsetDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val s = LocalStrings.current
    val hourValues = remember { (0..48).toList() }
    val minuteValues = remember { (0..55 step 5).toList() }

    // Seed wheels from the initial offset (round minutes to nearest 5).
    var hours by remember { mutableStateOf((initialMinutes / 60).coerceIn(0, 48)) }
    var minutes by remember {
        val m = initialMinutes % 60
        mutableStateOf((m / 5 * 5).coerceIn(0, 55))
    }

    val total = hours * 60 + minutes
    val valid = total > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { if (valid) onConfirm(total) }, enabled = valid) {
                Text(s.set)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
        title = { Text(s.customReminder) },
        text = {
            Column {
                Text(
                    s.remindBeforeDeparture,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        WheelPicker(
                            values = hourValues,
                            selectedIndex = hourValues.indexOf(hours).coerceAtLeast(0),
                            onSelectedIndexChange = { hours = hourValues[it] },
                            modifier = Modifier.width(70.dp),
                            label = { it.toString() },
                        )
                        Text(s.hours, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(Modifier.width(24.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        WheelPicker(
                            values = minuteValues,
                            selectedIndex = minuteValues.indexOf(minutes).coerceAtLeast(0),
                            onSelectedIndexChange = { minutes = minuteValues[it] },
                            modifier = Modifier.width(70.dp),
                            label = { it.toString().padStart(2, '0') },
                        )
                        Text(s.minutes, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (valid) offsetLabel(total, s) else s.pickAtLeast5,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

// Human label for any offset in minutes.
private fun offsetLabel(minutes: Int, s: com.itinera.app.i18n.Strings): String {
    if (minutes <= 0) return "Off"
    val h = minutes / 60
    val m = minutes % 60
    val parts = mutableListOf<String>()
    if (h > 0) parts.add(if (h == 1) "1 ${s.hourSingular}" else "$h ${s.hoursPlural}")
    if (m > 0) parts.add(if (m == 1) "1 ${s.minuteSingular}" else "$m ${s.minutesPlural}")
    return parts.joinToString(" ") + " ${s.before}"
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