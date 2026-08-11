package com.itinera.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.data.SavedZone
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.TopBar
import com.itinera.app.ui.components.reorderableItem
import com.itinera.app.ui.components.rememberReorderableLazyListState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.Clock
import kotlin.time.Instant

/** "Europe/Paris" -> "Paris", "America/New_York" -> "New York". */
private fun friendlyZone(id: String): String =
    id.substringAfterLast('/').replace('_', ' ')

private fun pad2(n: Int): String = if (n < 10) "0$n" else "$n"

@OptIn(kotlin.time.ExperimentalTime::class)
private fun nowInstant(): Instant = Clock.System.now()

private fun keyOf(z: SavedZone) = z.label + "|" + z.zoneId
private const val HomeClockKey = "__local_world_clock__"

@Composable
fun WorldClockScreen(
    zones: List<SavedZone>,
    onAddZone: (String, String) -> Unit,   // (label, zoneId)
    onRemoveZone: (SavedZone) -> Unit,
    onReorderZones: (List<SavedZone>) -> Unit = {},
    onBack: () -> Unit,
    /**
     * Destinations from the user's trips, offered as shortcuts in the picker.
     * Optional, so existing call sites still compile.
     */
    tripCities: List<String> = emptyList(),
) {
    val s = LocalStrings.current
    var showPicker by remember { mutableStateOf(false) }
    // which card is currently swiped open (only one at a time)
    var openKey by remember { mutableStateOf<String?>(null) }
    var orderedZones by remember(zones) { mutableStateOf(zones) }
    val clockListState = rememberLazyListState()

    fun moveZone(draggedKey: String, targetKey: String): List<SavedZone>? {
        val fromIndex = orderedZones.indexOfFirst { keyOf(it) == draggedKey }
        val toIndex = orderedZones.indexOfFirst { keyOf(it) == targetKey }
        if (fromIndex < 0 || toIndex < 0 || fromIndex == toIndex) return null
        return orderedZones.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    fun moveZoneBy(key: String, delta: Int) {
        val fromIndex = orderedZones.indexOfFirst { keyOf(it) == key }
        val toIndex = fromIndex + delta
        if (fromIndex !in orderedZones.indices || toIndex !in orderedZones.indices) return
        val next = orderedZones.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        orderedZones = next
        onReorderZones(next)
    }

    val reorderState = rememberReorderableLazyListState(
        listState = clockListState,
        canMoveOver = { it != HomeClockKey },
        onMove = { draggedKey, targetKey ->
            moveZone(draggedKey, targetKey)?.let {
                orderedZones = it
                openKey = null
            }
        },
        onDrop = { onReorderZones(orderedZones) },
    )

    // live tick — update every second so minutes roll over naturally
    var now by remember { mutableStateOf(nowInstant()) }
    // ⬅ CHANGED — was delay(1000). Only minutes are displayed, so ticking every
    // second recomposed every row 3,600 times an hour to change the display 60.
    // Sleeping to the next minute boundary also lands the rollover on time
    // rather than up to a second late.
    LaunchedEffect(Unit) {
        while (true) {
            now = nowInstant()
            delay(60_000 - (now.toEpochMilliseconds() % 60_000))
        }
    }

    val homeTz = TimeZone.currentSystemDefault()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(s.worldClock, onBack = onBack)

            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                state = clockListState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Home / local zone, pinned at top — not swipeable (not removable)
                item(key = HomeClockKey) {
                    // ⬅ CHANGED — was an identical ClockRow. Home is the reference
                    // every other row is measured against.
                    HomeClockCard(zoneId = homeTz.id, now = now)
                }

                if (orderedZones.isEmpty()) {
                    item {
                        Column(
                            // ⬅ CHANGED — was padding(top = 200.dp), a magic number
                            // that lands differently on every screen size.
                            Modifier.fillMaxWidth().padding(top = 64.dp, start = 32.dp, end = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // ⬅ CHANGED — was Text("🕐"), which doesn't tint with the theme.
                            Box(
                                Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                s.noCitiesYet,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                s.addCityForTime,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            )
                        }
                    }
                } else {
                    items(orderedZones, key = { keyOf(it) }) { entry ->
                        val k = keyOf(entry)
                        val index = orderedZones.indexOfFirst { keyOf(it) == k }
                        SwipeableClockCard(
                            entry = entry,
                            now = now,
                            homeTz = homeTz,     // ⬅ ADD
                            isOpen = openKey == k,
                            onOpenChange = { open -> openKey = if (open) k else null },
                            onDelete = { onRemoveZone(entry); openKey = null },
                            modifier = Modifier
                                .animateItem()
                                .reorderableItem(
                                    state = reorderState,
                                    key = k,
                                    itemLabel = entry.label,
                                    canMoveUp = index > 0,
                                    canMoveDown = index in 0 until orderedZones.lastIndex,
                                    onMoveUp = { moveZoneBy(k, -1) },
                                    onMoveDown = { moveZoneBy(k, 1) },
                                ),
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showPicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd).offset(x = (-25).dp, y = (-120).dp)
                .padding(end = 20.dp, bottom = 24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
        ) {
            Icon(Icons.Filled.Add, contentDescription = s.addTimeZone)
        }
    }

    if (showPicker) {
        ZonePickerDialog(
            tripCities = tripCities,
            onPick = { label, zoneId -> onAddZone(label, zoneId); showPicker = false },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun SwipeableClockCard(
    entry: SavedZone,
    now: Instant,
    homeTz: TimeZone,     // ⬅ ADD
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
    val exitOffsetX = remember { Animatable(0f) }

    val progress = ((-offsetX.value - with(density) { gap.toPx() }) /
            (panelPx - with(density) { gap.toPx() })).coerceIn(0f, 1f)

    LaunchedEffect(isOpen) {
        if (!isOpen && offsetX.value != 0f) offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
    }

    // Slide the whole card off to the left, then remove it (matches Weather).
    fun animateOutThenDelete() {
        scope.launch {
            val slide = with(density) { (panelWidth + 600.dp).toPx() }
            exitOffsetX.animateTo(-slide, tween(durationMillis = 300))
            onDelete()
        }
    }

    Box(
        modifier
            .fillMaxWidth()
            .offset { IntOffset(exitOffsetX.value.roundToInt(), 0) },
    ) {
        // Behind: the delete action — only present while swiped, so no flash on collapse
        if (offsetX.value != 0f) {
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
                    ) { animateOutThenDelete() }
                }
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
                zoneId = entry.zoneId,
                now = now,
                homeTz = homeTz,     // ⬅ ADD
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

/** Warm for day, cool for night — answers "can I call them?" without reading the clock. */
private val DayTint = Color(0xFFE0A93C)
private val NightTint = Color(0xFF9B92DD)

/**
 * Signed offset in minutes between a zone and home. Positive = ahead.
 * Minutes, not hours: Chennai is +5:30 and Kathmandu +5:45.
 */
private fun offsetMinutesFromHome(tz: TimeZone, home: TimeZone, now: Instant): Int =
    (tz.offsetAt(now).totalSeconds - home.offsetAt(now).totalSeconds) / 60

@Composable
private fun relativeToHome(minutes: Int): String {
    val s = LocalStrings.current
    if (minutes == 0) return s.sameTimeAsYou
    val abs = kotlin.math.abs(minutes)
    val h = abs / 60
    val m = abs % 60
    val amount = when {
        h > 0 && m > 0 -> "${h}h${pad2(m)}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
    return if (minutes > 0) s.hoursAhead.replace("%s", amount)
    else s.hoursBehind.replace("%s", amount)
}

@Composable
private fun dayOfWeekShort(dow: kotlinx.datetime.DayOfWeek): String {
    val s = LocalStrings.current
    return when (dow) {
        kotlinx.datetime.DayOfWeek.MONDAY -> s.monShort
        kotlinx.datetime.DayOfWeek.TUESDAY -> s.tueShort
        kotlinx.datetime.DayOfWeek.WEDNESDAY -> s.wedShort
        kotlinx.datetime.DayOfWeek.THURSDAY -> s.thuShort
        kotlinx.datetime.DayOfWeek.FRIDAY -> s.friShort
        kotlinx.datetime.DayOfWeek.SATURDAY -> s.satShort
        kotlinx.datetime.DayOfWeek.SUNDAY -> s.sunShort
    }
}

private fun utcOffsetLabel(tz: TimeZone, now: Instant): String {
    val secs = tz.offsetAt(now).totalSeconds
    val h = secs / 3600
    val m = (kotlin.math.abs(secs) % 3600) / 60
    return buildString {
        append("UTC")
        append(if (h >= 0) "+" else "-")
        append(kotlin.math.abs(h))
        if (m != 0) append(":").append(pad2(m))
    }
}

/**
 * The local zone, as a hero. It's the reference every other row is measured
 * against, so it shouldn't look identical to them — which it did, apart from
 * the word "Local".
 */
@Composable
private fun HomeClockCard(zoneId: String, now: Instant) {
    val s = LocalStrings.current
    val tz = remember(zoneId) { runCatching { TimeZone.of(zoneId) }.getOrNull() } ?: return
    val ldt = now.toLocalDateTime(tz)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "${friendlyZone(zoneId)} · ${s.localLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${pad2(ldt.hour)}:${pad2(ldt.minute)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "${dayOfWeekShort(ldt.dayOfWeek)} ${ldt.dayOfMonth} · ${utcOffsetLabel(tz, now)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }

            Spacer(Modifier.width(16.dp))
            LiveAnalogClock(
                zoneId = zoneId,
                modifier = Modifier.size(76.dp),
            )
        }
    }
}

@Composable
private fun LiveAnalogClock(
    zoneId: String,
    modifier: Modifier = Modifier,
) {
    val tz = remember(zoneId) { runCatching { TimeZone.of(zoneId) }.getOrNull() } ?: return
    var currentInstant by remember(zoneId) { mutableStateOf(nowInstant()) }

    LaunchedEffect(zoneId) {
        while (true) {
            currentInstant = nowInstant()
            delay(1_000 - (currentInstant.toEpochMilliseconds() % 1_000))
        }
    }

    val time = currentInstant.toLocalDateTime(tz)
    val faceColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    val hourHandColor = MaterialTheme.colorScheme.onSurface
    val minuteHandColor = MaterialTheme.colorScheme.primary
    val secondHandColor = MaterialTheme.colorScheme.tertiary
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier.semantics {
            contentDescription =
                "${pad2(time.hour)}:${pad2(time.minute)}:${pad2(time.second)}"
        },
    ) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        drawCircle(color = faceColor, radius = radius)
        drawCircle(
            color = borderColor,
            radius = radius - 0.75.dp.toPx(),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
        )

        repeat(12) { index ->
            val angle = index * (2f * PI.toFloat() / 12f) - PI.toFloat() / 2f
            val isCardinal = index % 3 == 0
            val outerRadius = radius * 0.84f
            val innerRadius = radius * if (isCardinal) 0.69f else 0.75f
            drawLine(
                color = tickColor.copy(alpha = if (isCardinal) 0.8f else 0.45f),
                start = androidx.compose.ui.geometry.Offset(
                    center.x + cos(angle) * innerRadius,
                    center.y + sin(angle) * innerRadius,
                ),
                end = androidx.compose.ui.geometry.Offset(
                    center.x + cos(angle) * outerRadius,
                    center.y + sin(angle) * outerRadius,
                ),
                strokeWidth = if (isCardinal) 2.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        val secondProgress = (time.second + time.nanosecond / 1_000_000_000f) / 60f
        val minuteProgress = (time.minute + secondProgress) / 60f
        val hourProgress = ((time.hour % 12) + minuteProgress) / 12f

        fun handEnd(progress: Float, length: Float): androidx.compose.ui.geometry.Offset {
            val angle = progress * 2f * PI.toFloat() - PI.toFloat() / 2f
            return androidx.compose.ui.geometry.Offset(
                center.x + cos(angle) * length,
                center.y + sin(angle) * length,
            )
        }

        drawLine(
            color = hourHandColor,
            start = center,
            end = handEnd(hourProgress, radius * 0.45f),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = minuteHandColor,
            start = center,
            end = handEnd(minuteProgress, radius * 0.66f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = secondHandColor,
            start = center,
            end = handEnd(secondProgress, radius * 0.72f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(color = secondHandColor, radius = 2.5.dp.toPx(), center = center)
    }
}

@Composable
private fun ClockRow(
    label: String,
    zoneId: String,
    now: Instant,
    homeTz: TimeZone,
    onTap: (() -> Unit)? = null,
) {
    val s = LocalStrings.current
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
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (tz == null) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                return@Row
            }

            val ldt = now.toLocalDateTime(tz)
            val homeLdt = now.toLocalDateTime(homeTz)
            val isDay = ldt.hour in 6..19
            val diffMinutes = offsetMinutesFromHome(tz, homeTz, now)

            // ⬅ ADD — day/night marker. 21:32 and 18:02 used to look identical.
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background((if (isDay) DayTint else NightTint).copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isDay) Icons.Filled.WbSunny else Icons.Filled.NightlightRound,
                    contentDescription = null,
                    tint = if (isDay) DayTint else NightTint,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(1.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // ⬅ CHANGED — was the raw zone id ("Kolkata"). The offset from
                    // home is the number people were computing in their heads.
                    Text(
                        relativeToHome(diffMinutes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                    // ⬅ ADD — "Sun" on every row said nothing about the rollover.
                    val dayShift = ldt.date.toEpochDays() - homeLdt.date.toEpochDays()
                    if (dayShift != 0L) {
                        Text(
                            " · " + if (dayShift > 0) s.tomorrow else s.yesterday,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (dayShift > 0) DayTint else NightTint,
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${pad2(ldt.hour)}:${pad2(ldt.minute)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    "${dayOfWeekShort(ldt.dayOfWeek)} · ${utcOffsetLabel(tz, now)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }

            Spacer(Modifier.width(6.dp))
            Text(
                "::",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(20.dp),
            )
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

/**
 * Zone picker. Already filtered live — this is the same pill-field styling as
 * the weather and documents search, plus the UTC offset on each result so you
 * can tell "Munich" apart from "Munich, North Dakota" before adding it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ZonePickerDialog(
    tripCities: List<String>,
    onPick: (String, String) -> Unit,   // (label, zoneId)
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    var query by remember { mutableStateOf("") }

    data class ZoneResult(val label: String, val zoneId: String)

    val ianaResults = remember {
        TimeZone.availableZoneIds.sorted().map { ZoneResult(friendlyZone(it), it) }
    }
    val cityResults = remember {
        cityZones.map { (city, zone) -> ZoneResult(city, zone) }
    }

    val filtered = remember(query) {
        val q = query.trim()
        // ⬅ CHANGED — was cityResults.sortedBy { it.label } when blank, which
        // dumped the whole alphabetical city list on you before you'd typed
        // anything. Abu Dhabi is not a useful default.
        if (q.isBlank()) {
            emptyList()
        } else {
            val cityHits = cityResults.filter { it.label.contains(q, ignoreCase = true) }
            val ianaHits = ianaResults.filter {
                it.label.contains(q, ignoreCase = true) ||
                        it.zoneId.replace('_', ' ').contains(q, ignoreCase = true)
            }
            (cityHits + ianaHits).distinctBy { it.label + "|" + it.zoneId }
        }
    }

    // One timestamp for the whole dialog — offsets don't shift while it's open.
    val now = remember { nowInstant() }

    val focusRequester = remember { FocusRequester() }
    // The dialog exists to be typed into, so open the keyboard with it.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.close) } },
        title = { Text(s.addTimeZone) },
        shape = RoundedCornerShape(20.dp),
        text = {
            Column {
                // ⬅ CHANGED — was an OutlinedTextField. Pill field, matching the
                // weather and documents search.
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = onSurface.copy(alpha = 0.06f),
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Search, null,
                            tint = onSurface.copy(alpha = 0.45f),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    s.searchAnyCity,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = onSurface.copy(alpha = 0.4f),
                                )
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = { query = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = onSurface),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            )
                        }
                        if (query.isNotEmpty()) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = s.clearLabel,
                                tint = onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp).clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { query = "" },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ⬅ ADD — destinations from the user's trips. Fills the box rather
                // than adding directly, so the same filter resolves the zone.
                if (tripCities.isNotEmpty() && query.isBlank()) {
                    Text(
                        s.fromYourTrips,
                        style = MaterialTheme.typography.labelMedium,
                        color = onSurface.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        tripCities.forEach { name ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Transparent,
                                border = BorderStroke(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                ),
                                modifier = Modifier.clickable { query = name },
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Filled.Add, null,
                                        modifier = Modifier.size(13.dp),
                                        tint = onSurface.copy(alpha = 0.6f),
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(name, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // ⬅ CHANGED — was firing on a blank query too, so the dialog
                // opened saying "no matches" before you'd typed.
                if (query.isNotBlank() && filtered.isEmpty()) {
                    Text(
                        s.noCityMatches,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 10.dp),
                    )
                }
//                else if (query.isBlank() && tripCities.isEmpty()) {
//                    // Without trip chips the dialog would otherwise be empty.
//                    Text(
//                        s.searchAnyCity,
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = onSurface.copy(alpha = 0.45f),
//                        modifier = Modifier.padding(vertical = 16.dp),
//                    )
//                }

                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(filtered, key = { it.label + "|" + it.zoneId }) { r ->
                        val tz = remember(r.zoneId) { runCatching { TimeZone.of(r.zoneId) }.getOrNull() }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onPick(r.label, r.zoneId) }
                                .padding(vertical = 11.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Place,
                                contentDescription = null,
                                tint = onSurface.copy(alpha = 0.45f),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    r.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                // ⬅ CHANGED — the zone id was only shown when it
                                // didn't match the label, so most rows had nothing to
                                // tell them apart. The offset always distinguishes.
                                val second = listOfNotNull(
                                    tz?.let { utcOffsetLabel(it, now) },
                                    r.zoneId.substringBeforeLast('/').replace('_', ' ')
                                        .takeIf { it.isNotBlank() && it != r.zoneId },
                                ).joinToString(" · ")
                                if (second.isNotBlank()) {
                                    Text(
                                        second,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = onSurface.copy(alpha = 0.5f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    }
                }
            }
        },
    )
}
