package com.itinera.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.itinera.app.data.SavedZone
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.TopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime

/** "Europe/Paris" -> "Paris", "America/New_York" -> "New York". */
private fun friendlyZone(id: String): String =
    id.substringAfterLast('/').replace('_', ' ')

private fun pad2(n: Int): String = if (n < 10) "0$n" else "$n"

@OptIn(kotlin.time.ExperimentalTime::class)
private fun nowInstant(): Instant = Clock.System.now()

private fun keyOf(z: SavedZone) = z.label + "|" + z.zoneId

@Composable
fun WorldClockScreen(
    zones: List<SavedZone>,
    onAddZone: (String, String) -> Unit,   // (label, zoneId)
    onRemoveZone: (SavedZone) -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalStrings.current
    var showPicker by remember { mutableStateOf(false) }
    // which card is currently swiped open (only one at a time)
    var openKey by remember { mutableStateOf<String?>(null) }

    // live tick — update every second so minutes roll over naturally
    var now by remember { mutableStateOf(nowInstant()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = nowInstant()
            delay(1000)
        }
    }

    val homeTz = TimeZone.currentSystemDefault()

    Column(Modifier.fillMaxSize()) {
        TopBar(s.worldClock, onBack = onBack)

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Home / local zone, pinned at top — not swipeable (not removable)
            item {
                ClockRow(
                    label = friendlyZone(homeTz.id),
                    sublabel = s.localLabel,
                    zoneId = homeTz.id,
                    now = now,
                )
            }

            if (zones.isEmpty()) {
                item {
                    Text(
                        s.addCityForTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else {
                items(zones, key = { keyOf(it) }) { entry ->
                    val k = keyOf(entry)
                    SwipeableClockCard(
                        entry = entry,
                        now = now,
                        isOpen = openKey == k,
                        onOpenChange = { open -> openKey = if (open) k else null },
                        onDelete = { onRemoveZone(entry); openKey = null },
                        modifier = Modifier.animateItem(),
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { showPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(s.addTimeZone)
                }
            }
        }
    }

    if (showPicker) {
        ZonePickerDialog(
            onPick = { label, zoneId -> onAddZone(label, zoneId); showPicker = false },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun SwipeableClockCard(
    entry: SavedZone,
    now: Instant,
    isOpen: Boolean,
    onOpenChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val density = LocalDensity.current
    val actionWidth = 80.dp
    val gap = 15.dp
    val panelWidth = actionWidth + gap
    val panelPx = with(density) { panelWidth.toPx() }
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    // exit animation: 1f normally, animates to 0f to collapse the card before removal
    val exitScale = remember { Animatable(1f) }

    val progress = ((-offsetX.value - with(density) { gap.toPx() }) /
            (panelPx - with(density) { gap.toPx() })).coerceIn(0f, 1f)

    LaunchedEffect(isOpen) {
        if (!isOpen && offsetX.value != 0f) offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
    }

    // Smoothly slide the card off to the left and collapse its height, then remove.
    fun animateOutAndDelete() {
        scope.launch {
            val targetX = -with(density) { (panelWidth + 400.dp).toPx() }
            launch { offsetX.animateTo(targetX, tween(durationMillis = 260)) }
            exitScale.animateTo(0f, tween(durationMillis = 260))
            onDelete()
        }
    }

    Box(
        modifier
            .fillMaxWidth()
            .graphicsLayer {
                // collapse vertically as it exits (scaleY) while keeping it pinned to top
                scaleY = exitScale.value
                alpha = exitScale.value
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
            },
    ) {
        // Behind: the delete action
        Row(
            Modifier.matchParentSize().clip(CardShape),
            horizontalArrangement = Arrangement.End,
        ) {
            Column(
                Modifier
                    .width(panelWidth)
                    .fillMaxHeight()
                    .padding(start = gap),
            ) {
                ActionButton(
                    Icons.Filled.Delete, s.delete, Color(0xFFB23B3B), progress,
                    Modifier.weight(1f),
                ) { animateOutAndDelete() }
            }
        }

        // Front: the clock card, draggable horizontally
        Box(
            Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(keyOf(entry)) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newValue = (offsetX.value + dragAmount).coerceIn(-panelPx, 0f)
                                offsetX.snapTo(newValue)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value < -panelPx / 2) {
                                    offsetX.animateTo(-panelPx, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
                                    onOpenChange(true)
                                } else {
                                    offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
                                    onOpenChange(false)
                                }
                            }
                        },
                    )
                },
        ) {
            ClockRow(
                label = entry.label,
                sublabel = friendlyZone(entry.zoneId),
                zoneId = entry.zoneId,
                now = now,
                onTap = {
                    // tapping an open card closes it
                    if (offsetX.value != 0f) {
                        scope.launch { offsetX.animateTo(0f, tween(250)); onOpenChange(false) }
                    }
                },
            )
        }
    }
}

@Composable
private fun ClockRow(
    label: String,
    sublabel: String,
    zoneId: String,
    now: Instant,
    onTap: (() -> Unit)? = null,
) {
    val tz = remember(zoneId) { runCatching { TimeZone.of(zoneId) }.getOrNull() }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onTap != null) Modifier.clickable(onClick = onTap) else Modifier),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }
            if (tz != null) {
                val ldt = now.toLocalDateTime(tz)
                val offsetSecs = tz.offsetAt(now).totalSeconds
                val offH = offsetSecs / 3600
                val offM = (kotlin.math.abs(offsetSecs) % 3600) / 60
                val offLabel = buildString {
                    append(if (offH >= 0) "+" else "-")
                    append(kotlin.math.abs(offH))
                    if (offM != 0) append(":").append(pad2(offM))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${pad2(ldt.hour)}:${pad2(ldt.minute)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "${ldt.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} · UTC$offLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    bg: Color,
    progress: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(52.dp)
                .graphicsLayer {
                    scaleX = progress
                    scaleY = progress
                    alpha = progress
                }
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun ZonePickerDialog(
    onPick: (String, String) -> Unit,   // (label, zoneId)
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    var query by remember { mutableStateOf("") }

    data class ZoneResult(val label: String, val zoneId: String)

    val ianaResults = remember {
        TimeZone.availableZoneIds.sorted().map { ZoneResult(friendlyZone(it), it) }
    }
    val cityResults = remember {
        cityZones.map { (city, zone) -> ZoneResult(city, zone) }
    }

    val filtered = remember(query) {
        if (query.isBlank()) {
            cityResults.sortedBy { it.label }
        } else {
            val q = query.trim()
            val cityHits = cityResults.filter { it.label.contains(q, ignoreCase = true) }
            val ianaHits = ianaResults.filter {
                it.label.contains(q, ignoreCase = true) || it.zoneId.replace('_', ' ').contains(q, ignoreCase = true)
            }
            (cityHits + ianaHits).distinctBy { it.label + "|" + it.zoneId }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.close) } },
        title = { Text(s.addTimeZone) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(s.searchAnyCity) },
                    singleLine = true,
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(filtered, key = { it.label + "|" + it.zoneId }) { r ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onPick(r.label, r.zoneId) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(r.label, style = MaterialTheme.typography.bodyLarge)
                                if (!r.zoneId.endsWith("/" + r.label.replace(' ', '_'))) {
                                    Text(
                                        r.zoneId.replace('_', ' '),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    )
}