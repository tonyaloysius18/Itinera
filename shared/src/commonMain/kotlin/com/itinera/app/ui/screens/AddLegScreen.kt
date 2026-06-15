package com.itinera.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Leg
import com.itinera.app.model.TransportType
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
    var time by remember { mutableStateOf(existing?.timeLabel ?: "") }
    var transport by remember { mutableStateOf(existing?.transport ?: TransportType.TRAIN) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val textFieldShape = RoundedCornerShape(12.dp)

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
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            Text(if (existing == null) s.newLeg else "Edit leg", style = MaterialTheme.typography.titleLarge)   // ⬅ title switches
        }

        Column(
            Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedTextField(from, { from = it.toTitleCase() }, label = { Text(s.from) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)
            OutlinedTextField(to, { to = it.toTitleCase() }, label = { Text(s.to) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)

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

            // ⬅ Day dropdown

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = date?.label() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(s.date) },
                    trailingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                    shape = textFieldShape,
                    modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    ),
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(s.time) },
                    trailingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                    shape = textFieldShape,
                    modifier = Modifier.weight(1f).clickable { showTimePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    ),
                )
            }
        }

        // Calendar dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            date = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.UTC).date      // ⬅ store the LocalDate itself
                        }
                        showDatePicker = false
                    }) { Text(s.ok) }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(s.cancel) } },
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Time picker dialog
        if (showTimePicker) {
            var picked by remember { mutableStateOf(LocalTime(12, 0)) }
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val h = picked.hour.toString().padStart(2, '0')
                        val m = picked.minute.toString().padStart(2, '0')
                        time = "$h:$m"
                        showTimePicker = false
                    }) { Text(s.ok) }
                },
                dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(s.cancel) } },
                text = {
                    WheelTimePicker(timeFormatter = timeFormatter(timeFormat = TimeFormat.HOUR_24)) { snappedTime ->
                        picked = snappedTime
                    }
                },
            )
        }

        // ⬅ Save (+ Delete beside it when editing)
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
                            fromCity = from.ifBlank { "—" },
                            toCity = to.ifBlank { "—" },
                            transport = transport,
                            date = date!!,                       // ⬅ real LocalDate (button enabled only when set)
                            timeLabel = time.ifBlank { "" },
                            bookingRef = null,
                        )
                    )
                },

                enabled = from.isNotBlank() && to.isNotBlank() && date != null,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp).padding(bottom = 16.dp),
            ) { Text(s.saveLeg) }
        }
    }
}