package com.itinera.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itinera.app.ReminderOffset
import com.itinera.app.legReminderFireTime
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Leg
import com.itinera.app.model.Trip
import com.itinera.app.model.label
import com.itinera.app.ui.components.TopBar
import com.itinera.app.ui.components.WheelPicker
import kotlin.time.Clock

internal data class NotificationPreview(
    val tripTitle: String,
    val leg: Leg,
    val fireAtMillis: Long,
)

internal fun nextNotificationPreview(
    trips: List<Trip>,
    offsetMinutes: Int,
    nowMillis: Long,
): NotificationPreview? = trips
    .asSequence()
    .flatMap { trip -> trip.legs.asSequence().map { leg -> trip to leg } }
    .filter { (_, leg) -> !leg.completed }
    .mapNotNull { (trip, leg) ->
        legReminderFireTime(leg, offsetMinutes, nowMillis)?.let { fireAt ->
            NotificationPreview(trip.title, leg, fireAt)
        }
    }
    .minByOrNull { it.fireAtMillis }

/**
 * Notifications settings: a master on/off plus the reminder lead-time choice.
 * `offsetMinutes` is the persisted UserProfile.reminderOffsetMinutes
 * (0 = off, else any positive number of minutes before departure).
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
fun NotificationsScreen(
    offsetMinutes: Int,
    hasPermission: Boolean,
    onChangeOffset: (Int) -> Unit,
    onRequestPermission: () -> Unit,
    onBack: () -> Unit,
    trips: List<Trip> = emptyList(),
) {
    val s = LocalStrings.current
    val enabled = offsetMinutes != ReminderOffset.OFF

    // Is the current offset one of the presets? If not, it's a custom value.
    val isCustom = enabled && offsetMinutes !in ReminderOffset.choices
    val nextReminder = nextNotificationPreview(
        trips = trips,
        offsetMinutes = offsetMinutes,
        nowMillis = Clock.System.now().toEpochMilliseconds(),
    )

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

            Text(
                s.notificationsTagline,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 44.dp, bottom = 20.dp),
            )

            // Master toggle card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            s.travelReminders,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            s.travelRemindersSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = enabled,
                        onCheckedChange = { on ->
                            if (on) {
                                onRequestPermission()
                                onChangeOffset(ReminderOffset.MIN_30)
                            } else {
                                onChangeOffset(ReminderOffset.OFF)
                            }
                        },
                    )
                }
            }

            // Offset choices (only meaningful when enabled)
            if (enabled) {
                SectionLabel(s.remindMe)

                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ReminderChoiceCard(
                            label = offsetLabel(ReminderOffset.MIN_30, s),
                            selected = offsetMinutes == ReminderOffset.MIN_30,
                            icon = Icons.Filled.Schedule,
                            onClick = { onChangeOffset(ReminderOffset.MIN_30) },
                            modifier = Modifier.weight(1f),
                        )
                        ReminderChoiceCard(
                            label = offsetLabel(ReminderOffset.HOUR_1, s),
                            selected = offsetMinutes == ReminderOffset.HOUR_1,
                            icon = Icons.Filled.Schedule,
                            onClick = { onChangeOffset(ReminderOffset.HOUR_1) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ReminderChoiceCard(
                            label = offsetLabel(ReminderOffset.DAY_1, s),
                            selected = offsetMinutes == ReminderOffset.DAY_1,
                            icon = Icons.Filled.Schedule,
                            onClick = { onChangeOffset(ReminderOffset.DAY_1) },
                            modifier = Modifier.weight(1f),
                        )
                        ReminderChoiceCard(
                            label = if (isCustom) offsetLabel(offsetMinutes, s) else s.custom,
                            selected = isCustom,
                            icon = Icons.Filled.Settings,
                            onClick = { showCustomDialog = true },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                SectionLabel(s.nextReminder)
                if (nextReminder != null) {
                    NextReminderCard(
                        preview = nextReminder,
                        offsetLabel = offsetLabel(offsetMinutes, s),
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                s.nothingUpcoming,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (!hasPermission) {
                    PermissionWarning(
                        message = s.allowNotificationsPrompt,
                        actionLabel = s.allowNotifications,
                        onRequestPermission = onRequestPermission,
                    )
                }
            }

            Spacer(Modifier.height(20.dp).navigationBarsPadding())
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

@Composable
private fun SectionLabel(label: String) {
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 10.dp),
    )
}

@Composable
private fun ReminderChoiceCard(
    label: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier
            .heightIn(min = 76.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        color = if (selected) primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (selected) Icons.Filled.Check else icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NextReminderCard(
    preview: NotificationPreview,
    offsetLabel: String,
) {
    val leg = preview.leg
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        leg.operator.ifBlank { preview.tripTitle },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (leg.operator.isNotBlank()) {
                        Text(
                            preview.tripTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Text(
                    offsetLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    leg.fromCity,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        transportIcon(leg.transport),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    leg.toCity,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "${leg.date.label()} · ${leg.timeLabel.ifBlank { "09:00" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PermissionWarning(
    message: String,
    actionLabel: String,
    onRequestPermission: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = onRequestPermission,
            ) {
                Text(actionLabel, fontWeight = FontWeight.SemiBold)
            }
        }
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
