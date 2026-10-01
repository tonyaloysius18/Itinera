package com.itinera.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.TripTemplate
import com.itinera.app.ui.components.TopBar
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
        }
    }
}

/** Numbered day badge + "Day N · theme" — same recipe the real itinerary uses. */
@Composable
private fun DayHeader(dayNumber: Int, theme: String) {
    val s = LocalStrings.current
    Row(
        Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("$dayNumber", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "${s.day} $dayNumber" + if (theme.isNotBlank()) " · $theme" else "",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** 24dp timeline marker — same visual as a real trip's itinerary rows. */
@Composable
private fun TimelineMarker() {
    Box(
        Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            modifier = Modifier.size(14.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripTemplateDetailScreen(
    template: TripTemplate,
    onBack: () -> Unit,
    onUseTemplate: (LocalDate) -> Unit,
) {
    val s = LocalStrings.current
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val accent = templateAccent(template)
    val primaryType = template.destinationTypes.firstOrNull()
    val daysWord = s.templateDurationDaysN.replace("%d", template.durationDays.toString())

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(template.title, onBack = onBack)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp),
            ) {
                item {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(200.dp).clip(RoundedCornerShape(20.dp))) {
                        if (template.coverImageUrl.isNotBlank()) {
                            AsyncImage(
                                model = template.coverImageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize(),
                            )
                            Box(
                                Modifier.matchParentSize().background(
                                    Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.45f))
                                )
                            )
                        } else {
                            Box(Modifier.fillMaxSize().background(accent.copy(alpha = 0.15f)))
                            Icon(
                                primaryType?.icon() ?: Icons.Filled.Map,
                                contentDescription = null,
                                tint = accent.copy(alpha = 0.5f),
                                modifier = Modifier.align(Alignment.Center).size(56.dp),
                            )
                        }
                        if (primaryType != null) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                            ) {
                                Text(
                                    primaryType.label(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }
                }

                item {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${template.country} · ${template.continent.label()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCell(daysWord, s.durationLabel, Modifier.weight(1f))
                            StatCell(template.budgetTier.label(), s.filterBudget, Modifier.weight(1f))
                            StatCell(template.paceTag.label(), s.filterPace, Modifier.weight(1f))
                        }

                        if (template.shortDescription.isNotBlank()) {
                            Spacer(Modifier.height(16.dp))
                            Text(template.shortDescription, style = MaterialTheme.typography.bodyMedium)
                        }

                        if (template.destinationTypes.size > 1 || template.tags.isNotEmpty()) {
                            Spacer(Modifier.height(14.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(template.destinationTypes) { type -> MetaPill(type.label(), type.icon()) }
                                items(template.tags) { tag -> MetaPill(tag) }
                            }
                        }
                    }
                }

                item {
                    Text(
                        s.itinerary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                items(template.days, key = { it.dayNumber }) { day ->
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        DayHeader(day.dayNumber, day.theme)
                        day.activities.forEach { activity ->
                            val tail = listOf(activity.time, activity.location).filter { it.isNotBlank() }.joinToString(" · ")
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 9.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                TimelineMarker()
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(activity.title, style = MaterialTheme.typography.bodyLarge)
                                    if (tail.isNotBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            tail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showDatePicker = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth(0.8f)
                .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
            elevation = null,
        ) {
            Text(s.useThisTemplate, fontWeight = FontWeight.Medium)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                        onUseTemplate(date)
                    }
                    showDatePicker = false
                }) { Text(s.ok) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(s.cancel) } },
        ) {
            Column {
                Text(
                    s.chooseStartDate,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                )
                DatePicker(state = datePickerState)
            }
        }
    }
}
