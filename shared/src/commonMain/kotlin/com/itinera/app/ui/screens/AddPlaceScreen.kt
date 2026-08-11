package com.itinera.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itinera.app.data.PlaceResult
import com.itinera.app.data.PlaceSearchService
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Activity
import com.itinera.app.model.label
import com.itinera.app.ui.components.InlineTimeSlot
import com.itinera.app.ui.components.PickerRow
import com.itinera.app.ui.components.PlainTextInput
import com.itinera.app.ui.components.SectionLabel
import com.itinera.app.ui.components.TravelTimePickerSheet
import com.itinera.app.ui.components.parseTimeOrNoon
import com.itinera.app.ui.components.toStoredTime
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    existing: Activity? = null,
    onClose: () -> Unit,
    /**
     * ⬅ CHANGED — gained endTime and note.
     *
     * `note` exists on Activity and nothing was writing to it. `location` was
     * labelled "Remarks" in this form while TripDetailScreen renders it as the
     * place's location, so remarks typed here appeared on the itinerary as a
     * location. Both now do what their names say.
     */
    onSave: (
        date: LocalDate,
        title: String,
        time: String,
        endTime: String,
        location: String,
        note: String,
    ) -> Unit,
    /**
     * The trip's days, offered as chips. Optional — with an empty list the date
     * row falls back to the calendar dialog alone.
     *
     * Worth passing: the unbounded DatePicker lets you file a place on a date
     * outside the trip entirely, and nothing catches it.
     */
    tripDates: List<LocalDate> = emptyList(),
    /**
     * Optional coordinates to bias POI search toward. Worth passing the current
     * leg's destination: "museum" is a very different result set in Stuttgart
     * than globally.
     */
    biasLat: Double? = null,
    biasLon: Double? = null,
) {
    val s = LocalStrings.current
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var location by remember { mutableStateOf(existing?.location ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var time by remember { mutableStateOf(existing?.time ?: "") }
    var endTime by remember { mutableStateOf(existing?.endTime ?: "") }
    var date by remember { mutableStateOf(existing?.date) }

    // ⬅ ADD — POI lookup for the location field.
    val placeSearch = remember { PlaceSearchService() }
    var placeResults by remember { mutableStateOf<List<PlaceResult>>(emptyList()) }
    var searchingPlaces by remember { mutableStateOf(false) }
    // Set when a suggestion is tapped, cleared when the user edits again — so
    // results don't reappear over the answer they just chose.
    var locationPicked by remember { mutableStateOf(existing != null) }

    LaunchedEffect(location, locationPicked) {
        if (locationPicked || location.trim().length < 3) {
            placeResults = emptyList(); searchingPlaces = false
            return@LaunchedEffect
        }
        delay(350)   // cancelled and relaunched per keystroke
        searchingPlaces = true
        placeResults = placeSearch.search(
            query = location,
            lat = biasLat,
            lon = biasLon,
        )
        searchingPlaces = false
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // ⬅ CHANGED — cased once on save. Per keystroke it fought anyone typing
    // "Musée d'Orsay" or a lowercase brand name.
    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() }
        }

    val blocker = when {
        title.isBlank() -> s.needPlaceName
        date == null -> s.needDate
        else -> null
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = s.close) }
                Text(
                    if (existing == null) s.newPlace else s.editPlace,
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Column(
                Modifier.weight(1f).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            ) {
                // ⬅ CHANGED — the name is what the screen is for, so it gets its
                // own card and a larger type size rather than being the first of
                // four identical outlined boxes.
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Text(
                            s.place,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                        Spacer(Modifier.height(2.dp))
                        PlainTextInput(
                            value = title,
                            onValueChange = { title = it.toTitleCase() },
                            placeholder = "",
                            textStyle = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }

                SectionLabel(s.whenLabel)

                // ⬅ ADD — one tap instead of three, and it makes filing a place
                // outside the trip impossible.
                if (tripDates.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        tripDates.forEach { d ->
                            val active = d == date
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                else Color.Transparent,
                                border = BorderStroke(
                                    if (active) 1.dp else 0.5.dp,
                                    if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                ),
                                modifier = Modifier.clickable { date = d },
                            ) {
                                Text(
                                    dayChipLabel(d),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (active) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                )
                            }
                        }
                        // Escape hatch for a place that genuinely sits outside the
                        // trip's days.
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Transparent,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.clickable { showDatePicker = true },
                        ) {
                            Row(
                                Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.CalendarMonth,
                                    contentDescription = s.otherDate,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Column {
                        if (tripDates.isEmpty()) {
                            PickerRow(
                                icon = Icons.Filled.CalendarMonth,
                                value = date?.label().orEmpty(),
                                placeholder = s.date,
                                onClick = { showDatePicker = true },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 46.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                            )
                        }
                        Row(Modifier.height(IntrinsicSize.Min)) {
                            InlineTimeSlot(
                                label = s.startsLabel,
                                value = time,
                                leadingIcon = Icons.Filled.Schedule,
                                modifier = Modifier.weight(1f),
                            ) { showTimePicker = true }
                            VerticalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                            )
                            // ⬅ ADD — legs have start and end; places had one time.
                            // "Museum, 09:00–11:30" says more than "09:00".
                            InlineTimeSlot(
                                label = s.endsLabel,
                                value = endTime,
                                emptyText = s.optionalLabel,
                                modifier = Modifier.weight(1f),
                            ) { showEndTimePicker = true }
                        }
                    }
                }

                SectionLabel(s.detailsLabel)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(14.dp))
                            // ⬅ CHANGED — was labelled "Remarks" while
                            // TripDetailScreen renders it as the location.
                            PlainTextInput(
                                value = location,
                                onValueChange = { location = it.toTitleCase(); locationPicked = false },
                                placeholder = s.locationLabel,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            when {
                                searchingPlaces -> CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(15.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                )
                                location.isBlank() -> Text(
                                    s.optionalLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                )
                            }
                        }

                        // ⬅ ADD — POI suggestions. Tapping one fills the address
                        // and, if the name field is still empty, the name too.
                        placeResults.forEach { result ->
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 46.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                            )
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        location = result.address.ifBlank { result.name }
                                        if (title.isBlank()) title = result.name
                                        locationPicked = true
                                        placeResults = emptyList()
                                    }
                                    .padding(start = 46.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        result.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (result.address.isNotBlank()) {
                                        Text(
                                            result.address,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 46.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        )
                        // ⬅ ADD — Activity.note existed and nothing wrote to it.
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                Icons.Filled.Notes,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                modifier = Modifier.size(18.dp).padding(top = 2.dp),
                            )
                            Spacer(Modifier.width(14.dp))
                            PlainTextInput(
                                value = note,
                                onValueChange = { note = it.toTitleCase() },
                                placeholder = s.notesLabel,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                singleLine = false,
                                modifier = Modifier.weight(1f),
                            )
                            if (note.isBlank()) {
                                Text(
                                    s.optionalLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(150.dp))
            }
        }

        // ⬅ CHANGED — was full width at the bottom edge with a dimmed disabled
        // state and no indication of what was missing.
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.4f to MaterialTheme.colorScheme.background,
                    )
                )
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (blocker != null) {
                Text(
                    blocker,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Button(
                onClick = {
                    date?.let {
                        onSave(
                            it,
                            title.trim().toTitleCase(),
                            time,
                            endTime,
                            location.trim().toTitleCase(),
                            note.trim(),
                        )
                    }
                },
                enabled = blocker == null,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(26.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 0.dp),
                modifier = Modifier.height(52.dp),
            ) { Text(s.savePlace) }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                    }
                    showDatePicker = false
                }) { Text(s.ok) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(s.cancel) } },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        TravelTimePickerSheet(
            title = s.startTime,
            initialTime = parseTimeOrNoon(time),
            confirmLabel = s.ok,
            onDismiss = { showTimePicker = false },
            onTimeSelected = {
                time = it.toStoredTime()
                showTimePicker = false
            },
        )
    }

    if (showEndTimePicker) {
        TravelTimePickerSheet(
            title = s.endTime,
            initialTime = parseTimeOrNoon(endTime),
            confirmLabel = s.ok,
            onDismiss = { showEndTimePicker = false },
            onTimeSelected = {
                endTime = it.toStoredTime()
                showEndTimePicker = false
            },
        )
    }
}

/** "Sat 11" — short enough that a week of chips fits without scrolling far. */
private fun dayChipLabel(date: LocalDate): String {
    val dow = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "$dow ${date.dayOfMonth}"
}
