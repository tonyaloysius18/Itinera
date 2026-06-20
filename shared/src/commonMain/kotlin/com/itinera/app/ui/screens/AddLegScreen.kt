package com.itinera.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Leg
import com.itinera.app.model.TransportType
import com.itinera.app.ui.components.countries          // ⬅ reuse existing country list
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import dev.darkokoa.datetimewheelpicker.WheelTimePicker
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import dev.darkokoa.datetimewheelpicker.core.format.timeFormatter
import kotlinx.datetime.LocalTime
import com.itinera.app.model.label
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLegScreen(
    existing: Leg? = null,
    onClose: () -> Unit,
    onSave: (Leg) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val s = LocalStrings.current
    var from by remember { mutableStateOf(existing?.fromCity ?: "") }
    var to by remember { mutableStateOf(existing?.toCity ?: "") }
    var date by remember { mutableStateOf(existing?.date) }
    var startTime by remember { mutableStateOf(existing?.timeLabel ?: "") }
    var endTime by remember { mutableStateOf(existing?.endTimeLabel ?: "") }
    var operator by remember { mutableStateOf(existing?.operator ?: "") }
    var country by remember { mutableStateOf(existing?.country ?: "") }            // ⬅ ADD
    var transport by remember { mutableStateOf(existing?.transport ?: TransportType.TRAIN) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }                    // ⬅ ADD
    val datePickerState = rememberDatePickerState()

    val textFieldShape = RoundedCornerShape(12.dp)

    val operatorLabel = when (transport) {
        TransportType.FLIGHT -> s.flightOperator
        TransportType.TRAIN -> s.trainOperator
        TransportType.BUS -> s.busOperator
        TransportType.FERRY -> s.ferryOperator
        TransportType.CAR -> s.operatorGeneric
    }

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = s.close) }
            Text(if (existing == null) s.newLeg else s.editLeg, style = MaterialTheme.typography.titleLarge)
        }

        Column(
            Modifier.weight(1f).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedTextField(from, { from = it.toTitleCase() }, label = { Text(s.from) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)
            OutlinedTextField(to, { to = it.toTitleCase() }, label = { Text(s.to) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)

            // Destination country (optional) — counts toward the trip's country total
            OutlinedTextField(
                value = country,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text(s.country) },
                trailingIcon = { Icon(Icons.Filled.Public, contentDescription = null) },
                shape = textFieldShape,
                modifier = Modifier.fillMaxWidth().clickable { showCountryPicker = true },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                ),
            )

            Text(s.transport, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(TransportType.FLIGHT, TransportType.TRAIN, TransportType.BUS, TransportType.FERRY).forEach { t ->
                    val selected = transport == t
                    Box(
                        Modifier.weight(1f).height(44.dp)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp),
                            )
                            .clickable { transport = t },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            transportIcon(t), contentDescription = t.name,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                }
            }

            // Date (full width)
            OutlinedTextField(
                value = date?.label() ?: "",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text(s.date) },
                trailingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                shape = textFieldShape,
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                ),
            )

            // Start time | End time
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(s.startTime) },
                    trailingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                    shape = textFieldShape,
                    modifier = Modifier.weight(1f).clickable { showStartTimePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    ),
                )

                OutlinedTextField(
                    value = endTime,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(s.endTime) },
                    trailingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                    shape = textFieldShape,
                    modifier = Modifier.weight(1f).clickable { showEndTimePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    ),
                )
            }

            // Operator (full width, label changes with transport)
            OutlinedTextField(
                value = operator,
                onValueChange = { operator = it.toTitleCase() },
                label = { Text(operatorLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
        }

        // Calendar dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            date = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.UTC).date
                        }
                        showDatePicker = false
                    }) { Text(s.ok) }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(s.cancel) } },
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Start time picker
        if (showStartTimePicker) {
            var picked by remember { mutableStateOf(LocalTime(12, 0)) }
            AlertDialog(
                onDismissRequest = { showStartTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val h = picked.hour.toString().padStart(2, '0')
                        val m = picked.minute.toString().padStart(2, '0')
                        startTime = "$h:$m"
                        showStartTimePicker = false
                    }) { Text(s.ok) }
                },
                dismissButton = { TextButton(onClick = { showStartTimePicker = false }) { Text(s.cancel) } },
                text = {
                    WheelTimePicker(timeFormatter = timeFormatter(timeFormat = TimeFormat.HOUR_24)) { snappedTime ->
                        picked = snappedTime
                    }
                },
            )
        }

        // End time picker
        if (showEndTimePicker) {
            var picked by remember { mutableStateOf(LocalTime(12, 0)) }
            AlertDialog(
                onDismissRequest = { showEndTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val h = picked.hour.toString().padStart(2, '0')
                        val m = picked.minute.toString().padStart(2, '0')
                        endTime = "$h:$m"
                        showEndTimePicker = false
                    }) { Text(s.ok) }
                },
                dismissButton = { TextButton(onClick = { showEndTimePicker = false }) { Text(s.cancel) } },
                text = {
                    WheelTimePicker(timeFormatter = timeFormatter(timeFormat = TimeFormat.HOUR_24)) { snappedTime ->
                        picked = snappedTime
                    }
                },
            )
        }

        // Country picker (searchable, reuses the existing `countries` list)
        if (showCountryPicker) {

            fun String.toTitleCase(): String =
                split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { c ->
                        if (c.isLowerCase()) c.titlecase() else c.toString()
                    }
                }

            var search by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCountryPicker = false },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { showCountryPicker = false }) { Text(s.cancel) } },
                title = { Text(s.country) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it.toTitleCase() },
                            label = { Text(s.search) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        val filtered = countries.filter { it.name.contains(search, ignoreCase = true) }
                        LazyColumn(Modifier.heightIn(max = 320.dp)) {
                            items(filtered) { c ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .clickable { country = c.name; showCountryPicker = false }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(c.flag, fontSize = 20.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Text(c.name, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                },
            )
        }

        // Save
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    onSave(
                        Leg(
                            id = existing?.id ?: "leg_${kotlin.random.Random.nextLong()}",
                            completed = existing?.completed ?: false,
                            addedToCalendar = existing?.addedToCalendar ?: false,
                            fromCity = from.ifBlank { "—" },
                            toCity = to.ifBlank { "—" },
                            transport = transport,
                            date = date!!,
                            timeLabel = startTime.ifBlank { "" },
                            endTimeLabel = endTime.ifBlank { "" },
                            operator = operator.trim(),
                            country = country.trim(),                    // ⬅ ADD
                            bookingRef = null,
                        )
                    )
                },
                enabled = from.isNotBlank() && to.isNotBlank() && date != null,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 80.dp).padding(bottom = 16.dp),
            ) { Text(s.saveLeg) }
        }
    }
}