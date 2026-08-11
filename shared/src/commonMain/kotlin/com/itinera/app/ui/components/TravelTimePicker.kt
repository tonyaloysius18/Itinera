package com.itinera.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Clock

private const val MinutesPerDay = 24 * 60
private const val MinuteStep = 1

private data class TimelinePalette(
    val start: Color,
    val middle: Color,
    val end: Color,
    val foreground: Color,
)

/**
 * A travel-oriented 24-hour picker that keeps the value returned to the app as a [LocalTime].
 * The timeline and the visible +/- controls provide equivalent drag and tap interactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelTimePickerSheet(
    title: String,
    initialTime: LocalTime,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
) {
    var dayMinutes by remember(initialTime) {
        mutableIntStateOf(initialTime.hour * 60 + initialTime.minute)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selectedTime = localTimeFrom(dayMinutes)
    val selectedLabel = selectedTime.toStoredTime()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 12.dp).size(width = 40.dp, height = 4.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            ) {}
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = selectedLabel,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                ),
                modifier = Modifier.semantics {
                    contentDescription = "Selected time $selectedLabel"
                },
            )

            Spacer(Modifier.height(12.dp))

            TimeGradientTimeline(
                dayMinutes = dayMinutes,
                selectedLabel = selectedLabel,
                onMinutesChanged = { dayMinutes = it },
            )

            Spacer(Modifier.height(12.dp))

            MinuteStepper(
                onSubtract = { dayMinutes = shiftMinutes(dayMinutes, -MinuteStep) },
                onAdd = { dayMinutes = shiftMinutes(dayMinutes, MinuteStep) },
            )

            Spacer(Modifier.height(12.dp))

            val now = remember {
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickTimeButton(
                    label = "Now",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        dayMinutes = now.hour * 60 + now.minute
                    },
                )
                QuickTimeButton(
                    label = "+30 min",
                    modifier = Modifier.weight(1f),
                    onClick = { dayMinutes = shiftMinutes(dayMinutes, 30) },
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onTimeSelected(selectedTime) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(confirmLabel)
            }
        }
    }
}

@Composable
private fun TimeGradientTimeline(
    dayMinutes: Int,
    selectedLabel: String,
    onMinutesChanged: (Int) -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val timelineShape = RoundedCornerShape(20.dp)
    val timelinePalette = timeBasedTimelinePalette(dayMinutes)
    val foreground = timelinePalette.foreground
    val timePeriod = timePeriodLabel(dayMinutes)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(timelineShape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        timelinePalette.start,
                        timelinePalette.middle,
                        timelinePalette.end,
                    )
                )
            )
            .border(1.dp, foreground.copy(alpha = 0.28f), timelineShape)
            .semantics {
                contentDescription = "$timePeriod sky, selected time $selectedLabel"
            },
    ) {
        TimeOfDayScene(
            dayMinutes = dayMinutes,
            palette = timelinePalette,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Surface(
                color = foreground.copy(alpha = 0.16f),
                contentColor = foreground,
                shape = RoundedCornerShape(9.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(
                    selectedLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }

            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxWidth().height(30.dp)) {
                    val centerY = size.height / 2f
                    drawLine(
                        color = foreground.copy(alpha = 0.72f),
                        start = androidx.compose.ui.geometry.Offset(0f, centerY),
                        end = androidx.compose.ui.geometry.Offset(size.width, centerY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                    for (index in 0..24) {
                        val x = size.width * index / 24f
                        val major = index % 6 == 0
                        val tickHeight = if (major) 18.dp.toPx() else 9.dp.toPx()
                        drawLine(
                            color = foreground.copy(alpha = if (major) 0.9f else 0.55f),
                            start = androidx.compose.ui.geometry.Offset(x, centerY - tickHeight / 2),
                            end = androidx.compose.ui.geometry.Offset(x, centerY + tickHeight / 2),
                            strokeWidth = if (major) 2.dp.toPx() else 1.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }

                Slider(
                    value = dayMinutes.toFloat(),
                    onValueChange = {
                        val snapped = (it / MinuteStep).roundToInt() * MinuteStep
                        onMinutesChanged(snapped.coerceIn(0, MinutesPerDay - MinuteStep))
                    },
                    valueRange = 0f..(MinutesPerDay - MinuteStep).toFloat(),
                    steps = MinutesPerDay - 2,
                    modifier = Modifier.fillMaxWidth().semantics {
                        contentDescription = "24-hour time timeline, selected $selectedLabel, $timePeriod"
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = primary,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent,
                    ),
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("00", "06", "12", "18", "24").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = foreground.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeOfDayScene(
    dayMinutes: Int,
    palette: TimelinePalette,
    modifier: Modifier = Modifier,
) {
    val minute = dayMinutes.coerceIn(0, MinutesPerDay - 1)
    val sunAlpha = daylightAlpha(minute)
    val moonAlpha = 1f - sunAlpha

    Canvas(modifier = modifier) {
        val starColor = Color.White.copy(alpha = 0.78f * moonAlpha)
        val stars = listOf(
            0.10f to 0.22f,
            0.20f to 0.36f,
            0.34f to 0.18f,
            0.70f to 0.20f,
            0.82f to 0.34f,
            0.91f to 0.16f,
        )
        stars.forEachIndexed { index, (x, y) ->
            drawCircle(
                color = starColor,
                radius = if (index % 3 == 0) 1.7.dp.toPx() else 1.1.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * x, size.height * y),
            )
        }

        val sunProgress = ((minute - 5 * 60) / (14f * 60f)).coerceIn(0f, 1f)
        val sunArc = 4f * sunProgress * (1f - sunProgress)
        val sunCenter = androidx.compose.ui.geometry.Offset(
            x = size.width * (0.12f + 0.76f * sunProgress),
            y = size.height * (0.69f - 0.42f * sunArc),
        )
        val sunRadius = 10.dp.toPx()
        repeat(8) { index ->
            val angle = index * kotlin.math.PI.toFloat() / 4f
            val inner = sunRadius * 1.45f
            val outer = sunRadius * 1.85f
            drawLine(
                color = Color(0xFFFFF3B0).copy(alpha = 0.72f * sunAlpha),
                start = androidx.compose.ui.geometry.Offset(
                    sunCenter.x + kotlin.math.cos(angle) * inner,
                    sunCenter.y + kotlin.math.sin(angle) * inner,
                ),
                end = androidx.compose.ui.geometry.Offset(
                    sunCenter.x + kotlin.math.cos(angle) * outer,
                    sunCenter.y + kotlin.math.sin(angle) * outer,
                ),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        drawCircle(
            color = Color(0xFFFFF1A8).copy(alpha = sunAlpha),
            radius = sunRadius,
            center = sunCenter,
        )

        val moonMinutes = if (minute >= 18 * 60) minute - 18 * 60 else minute + 6 * 60
        val moonProgress = (moonMinutes / (11f * 60f)).coerceIn(0f, 1f)
        val moonArc = 4f * moonProgress * (1f - moonProgress)
        val moonCenter = androidx.compose.ui.geometry.Offset(
            x = size.width * (0.12f + 0.76f * moonProgress),
            y = size.height * (0.69f - 0.42f * moonArc),
        )
        val moonRadius = 11.dp.toPx()
        drawCircle(
            color = Color(0xFFFFF5CC).copy(alpha = moonAlpha),
            radius = moonRadius,
            center = moonCenter,
        )
        drawCircle(
            color = palette.middle.copy(alpha = moonAlpha),
            radius = moonRadius * 0.88f,
            center = androidx.compose.ui.geometry.Offset(
                x = moonCenter.x + moonRadius * 0.48f,
                y = moonCenter.y - moonRadius * 0.18f,
            ),
        )
    }
}

@Composable
private fun timeBasedTimelinePalette(dayMinutes: Int): TimelinePalette {
    val night = TimelinePalette(
        start = Color(0xFF071426),
        middle = Color(0xFF102E5C),
        end = Color(0xFF1D467A),
        foreground = Color(0xFFF8FAFC),
    )
    val sunrise = TimelinePalette(
        start = Color(0xFFF59E0B),
        middle = Color(0xFFFFC83D),
        end = Color(0xFFFFE8A3),
        foreground = Color(0xFF3D2B00),
    )
    val daytime = TimelinePalette(
        start = Color(0xFFFBBF24),
        middle = Color(0xFFFDE047),
        end = Color(0xFFFFF3C4),
        foreground = Color(0xFF352600),
    )
    val dusk = TimelinePalette(
        start = Color(0xFFF59E0B),
        middle = Color(0xFFB453C6),
        end = Color(0xFF243A78),
        foreground = Color(0xFFF8FAFC),
    )
    val keyframes = listOf(
        0 to night,
        5 * 60 to night,
        7 * 60 to sunrise,
        12 * 60 to daytime,
        17 * 60 to dusk,
        19 * 60 to night,
        MinutesPerDay to night,
    )
    val minute = dayMinutes.coerceIn(0, MinutesPerDay - 1)
    val segmentIndex = keyframes
        .indexOfLast { (keyframeMinute, _) -> minute >= keyframeMinute }
        .coerceIn(0, keyframes.lastIndex - 1)
    val (startMinute, startPalette) = keyframes[segmentIndex]
    val (endMinute, endPalette) = keyframes[segmentIndex + 1]
    val fraction = (minute - startMinute).toFloat() / (endMinute - startMinute)

    return TimelinePalette(
        start = lerp(startPalette.start, endPalette.start, fraction),
        middle = lerp(startPalette.middle, endPalette.middle, fraction),
        end = lerp(startPalette.end, endPalette.end, fraction),
        foreground = lerp(startPalette.foreground, endPalette.foreground, fraction),
    )
}

private fun daylightAlpha(minute: Int): Float = when {
    minute < 5 * 60 -> 0f
    minute < 7 * 60 -> (minute - 5 * 60) / (2f * 60f)
    minute <= 17 * 60 -> 1f
    minute < 19 * 60 -> 1f - (minute - 17 * 60) / (2f * 60f)
    else -> 0f
}

private fun timePeriodLabel(dayMinutes: Int): String = when (dayMinutes) {
    in 5 * 60 until 8 * 60 -> "Sunrise"
    in 8 * 60 until 17 * 60 -> "Daytime"
    in 17 * 60 until 19 * 60 -> "Sunset"
    else -> "Night"
}

@Composable
private fun MinuteStepper(
    onSubtract: () -> Unit,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepButton(
            label = "Subtract 1 minute",
            icon = { Icon(Icons.Filled.Remove, contentDescription = null) },
            onClick = onSubtract,
        )
        Spacer(Modifier.width(16.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.height(48.dp),
        ) {
            Box(Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                Text(
                    "1-minute steps",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        StepButton(
            label = "Add 1 minute",
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            onClick = onAdd,
        )
    }
}

@Composable
private fun StepButton(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp).semantics { contentDescription = label },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        ),
    ) {
        Box(contentAlignment = Alignment.Center) { icon() }
    }
}

@Composable
private fun QuickTimeButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(label)
    }
}

private fun shiftMinutes(dayMinutes: Int, delta: Int): Int =
    (dayMinutes + delta).mod(MinutesPerDay)

private fun localTimeFrom(dayMinutes: Int): LocalTime =
    LocalTime(dayMinutes / 60, dayMinutes % 60)

fun parseTimeOrNoon(value: String): LocalTime {
    val parts = value.split(':')
    val hour = parts.getOrNull(0)?.toIntOrNull()
    val minute = parts.getOrNull(1)?.toIntOrNull()
    return if (hour != null && minute != null && hour in 0..23 && minute in 0..59) {
        LocalTime(hour, minute)
    } else {
        LocalTime(12, 0)
    }
}

fun LocalTime.toStoredTime(): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
