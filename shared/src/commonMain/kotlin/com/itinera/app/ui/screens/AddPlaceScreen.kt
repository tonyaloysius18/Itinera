package com.itinera.app.ui.screens

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
import com.itinera.app.model.Activity
import com.itinera.app.model.label
import dev.darkokoa.datetimewheelpicker.WheelTimePicker
import dev.darkokoa.datetimewheelpicker.core.format.TimeFormat
import dev.darkokoa.datetimewheelpicker.core.format.timeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    existing: Activity? = null,
    onClose: () -> Unit,
    onSave: (date: LocalDate, title: String, time: String, location: String) -> Unit,
) {
    val s = LocalStrings.current
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var location by remember { mutableStateOf(existing?.location ?:"") }
    var time by remember { mutableStateOf(existing?.time ?: "") }
    var date by remember { mutableStateOf(existing?.date) }      // ⬅ replaces `var day`

    var showDatePicker by remember { mutableStateOf(false) }       // ⬅ ADD
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()                // ⬅ ADD
    val textFieldShape = RoundedCornerShape(12.dp)

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            Text(if (existing == null) s.newPlace else s.editPlace, style = MaterialTheme.typography.titleLarge)        }

        Column(
            Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedTextField(
                title, { title = it.replaceFirstChar{ if (it.isLowerCase()) it.uppercase() else it.toString() } },
                label = { Text(s.place) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            OutlinedTextField(
                location, { location = it.replaceFirstChar{ if (it.isLowerCase()) it.uppercase() else it.toString() } },
                label = { Text(s.landmark) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

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
                    WheelTimePicker(timeFormatter = timeFormatter(timeFormat = TimeFormat.HOUR_24)) { snapped ->
                        picked = snapped
                    }
                },
            )
        }

        Box(Modifier.padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 60.dp))
        {Button(
            onClick = { if (title.isNotBlank() && date != null) onSave(date!!, title.trim(), time, location.trim()) },
            enabled = title.isNotBlank() && date != null,
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp).padding(bottom = 16.dp),
        ) { Text(s.savePlace) }
        }
    }
}



//Button(
//onClick = { showAddChooser = true },
//
//) {
//    Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
//    Spacer(Modifier.width(6.dp))
//    Text("Add", style = MaterialTheme.typography.bodyMedium)
//}
// Shared Day dropdown — used by both AddPlaceScreen and AddLegScreen (same package)
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DayPickerField(
//    day: Int,
//    onDayChange: (Int) -> Unit,
//    modifier: Modifier = Modifier,
//    maxDays: Int = 14,
//) {
//    var expanded by remember { mutableStateOf(false) }
//    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
//        OutlinedTextField(
//            value = "Day $day",
//            onValueChange = {},
//            readOnly = true,
//            label = { Text("Day") },
//            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//            modifier = Modifier.menuAnchor().fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp),
//        )
//        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//            (1..maxDays).forEach { d ->
//                DropdownMenuItem(text = { Text("Day $d") }, onClick = { onDayChange(d); expanded = false })
//            }
//        }
//    }
//}