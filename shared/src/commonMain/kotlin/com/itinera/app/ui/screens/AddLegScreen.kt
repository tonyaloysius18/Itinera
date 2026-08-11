package com.itinera.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Leg
import com.itinera.app.model.LegStop
import com.itinera.app.model.TransportType
import com.itinera.app.model.Traveller
import com.itinera.app.model.label
import com.itinera.app.ui.components.InlineTimeSlot
import com.itinera.app.ui.components.PickerRow
import com.itinera.app.ui.components.PlainTextInput
import com.itinera.app.ui.components.SectionLabel
import com.itinera.app.ui.components.TravelTimePickerSheet
import com.itinera.app.ui.components.countries
import com.itinera.app.ui.components.parseTimeOrNoon
import com.itinera.app.ui.components.toStoredTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLegScreen(
    existing: Leg? = null,
    travellers: List<Traveller> = emptyList(),
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
    var selectedTravellerIds by remember { mutableStateOf(existing?.travellerIds?.toSet() ?: travellers.map { it.id }.toSet()) }
    val stops = remember {
        mutableStateListOf<LegStop>().apply { existing?.stops?.forEach { add(it) } }
    }
    var stopTimePicker by remember { mutableStateOf<Pair<Int, Boolean>?>(null) }  // index to isArrival

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }                    // ⬅ ADD
    var showTravellerPicker by remember { mutableStateOf(false) }
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

    Box(Modifier.fillMaxSize()) {
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
            ) {
                // ⬅ CHANGED — transport moved to the top and gained labels. It sets
                // the operator field's label further down, so choosing it last was
                // backwards; and the bus and train glyphs are hard to tell apart at
                // icon size.
                Text(
                    s.transport,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 2.dp, bottom = 10.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        TransportType.FLIGHT to s.flightLabel,
                        TransportType.TRAIN to s.trainLabel,
                        TransportType.BUS to s.busLabel,
                        TransportType.FERRY to s.ferryLabel,
                    ).forEach { (t, label) ->
                        val selected = transport == t
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (selected) 1.dp else 0.5.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .clickable { transport = t }
                                .padding(vertical = 9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                transportIcon(t),
                                contentDescription = null,
                                tint = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // ⬅ CHANGED — From and To were two unrelated outlined boxes. A leg
                // is a route; the connector and the swap button say so.
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        // The swap button gets its own Box around From/To only —
                        // against the whole card it would centre between To and
                        // the date row.
                        Box {
                            Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                RouteField(
                                    label = s.from,
                                    value = from,
                                    onValueChange = { from = it.toTitleCase() },
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 52.dp, end = 64.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                )
                                RouteField(
                                    label = s.to,
                                    value = to,
                                    onValueChange = { to = it.toTitleCase() },
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 14.dp)
                                    .size(34.dp)
                                    .clickable { val t = from; from = to; to = t },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.SwapVert,
                                        contentDescription = s.swapDirection,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        // ⬅ CHANGED — When moved up into the route card. Date and
                        // times belong with the route they describe, and the stops
                        // no longer separate them from it.
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        )
                        PickerRow(
                            icon = Icons.Filled.CalendarMonth,
                            value = date?.label().orEmpty(),
                            placeholder = s.date,
                            onClick = { showDatePicker = true },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        )
                        Row(Modifier.height(IntrinsicSize.Min)) {
                            InlineTimeSlot(
                                label = s.startTime,
                                value = startTime,
                                leadingIcon = Icons.Filled.Schedule,
                                modifier = Modifier.weight(1f),
                            ) { showStartTimePicker = true }
                            VerticalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                            )
                            InlineTimeSlot(
                                label = s.endTime,
                                value = endTime,
                                modifier = Modifier.weight(1f),
                            ) { showEndTimePicker = true }
                        }
                    }
                }

                // Layovers — only rendered when there are any, so the common case
                // (a direct leg) isn't three empty fields longer.
                stops.forEachIndexed { index, stop ->
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    s.stopPlaceholder.replace("%d", (index + 1).toString()),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.weight(1f),
                                )
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = s.removeStop,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { stops.removeAt(index) },
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            PlainTextInput(
                                value = stop.city,
                                onValueChange = {
                                    // city changed -> reset geo so it re-geocodes
                                    stops[index] = stop.copy(city = it, lat = 0.0, lng = 0.0, country = "")
                                },
                                placeholder = s.city,
                                textStyle = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(Modifier.height(10.dp))
                            Row {
                                InlineTimeSlot(
                                    label = s.arrival,
                                    value = stop.arrivalTime,
                                    modifier = Modifier.weight(1f),
                                ) { stopTimePicker = index to true }
                                InlineTimeSlot(
                                    label = s.departure,
                                    value = stop.departureTime,
                                    modifier = Modifier.weight(1f),
                                ) { stopTimePicker = index to false }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                // ⬅ CHANGED — outlined pill, matching the chips on the language
                // and weather screens.
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    modifier = Modifier.clickable { stops.add(LegStop()) },
                ) {
                    Row(
                        Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            s.addStop,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        )
                    }
                }

                // ── DETAILS ───────────────────────────────────────────────────
                SectionLabel(s.detailsLabel)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Column {
                        // Operator is free text, so it stays an input — but styled
                        // as a row so it sits with the pickers.
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(16.dp))
                            Box(Modifier.weight(1f)) {
                                PlainTextInput(
                                    value = operator,
                                    onValueChange = { operator = it.toTitleCase() },
                                    placeholder = operatorLabel,
                                    textStyle = MaterialTheme.typography.titleMedium,
                                )
                            }
                            if (operator.isBlank()) {
                                Text(
                                    s.optionalLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        )
                        PickerRow(
                            icon = Icons.Filled.Public,
                            value = country,
                            placeholder = s.country,
                            trailingHint = if (country.isBlank()) s.optionalLabel else "",
                            onClick = { showCountryPicker = true },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        )
                        PickerRow(
                            icon = Icons.Filled.People,
                            value = if (selectedTravellerIds.isEmpty()) ""
                            else "${selectedTravellerIds.size} ${s.travellersCount}",
                            placeholder = s.selectTravellers,
                            onClick = { showTravellerPicker = true },
                        )
                    }
                }

                Spacer(Modifier.height(120.dp))
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
                TravelTimePickerSheet(
                    title = s.departure,
                    initialTime = parseTimeOrNoon(startTime),
                    confirmLabel = s.ok,
                    onDismiss = { showStartTimePicker = false },
                    onTimeSelected = {
                        startTime = it.toStoredTime()
                        showStartTimePicker = false
                    },
                )
            }

            // End time picker
            if (showEndTimePicker) {
                TravelTimePickerSheet(
                    title = s.arrival,
                    initialTime = parseTimeOrNoon(endTime),
                    confirmLabel = s.ok,
                    onDismiss = { showEndTimePicker = false },
                    onTimeSelected = {
                        endTime = it.toStoredTime()
                        showEndTimePicker = false
                    },
                )
            }

            // Stop arrival/departure time picker
            stopTimePicker?.let { (index, isArrival) ->
                val value = if (isArrival) stops[index].arrivalTime else stops[index].departureTime
                TravelTimePickerSheet(
                    title = if (isArrival) s.arrival else s.departure,
                    initialTime = parseTimeOrNoon(value),
                    confirmLabel = s.ok,
                    onDismiss = { stopTimePicker = null },
                    onTimeSelected = { selected ->
                        val time = selected.toStoredTime()
                        stops[index] = if (isArrival) stops[index].copy(arrivalTime = time)
                        else stops[index].copy(departureTime = time)
                        stopTimePicker = null
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
                                onValueChange = { search = it },   // ⬅ CHANGED — was toTitleCase per keystroke
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

            if (showTravellerPicker) {
                val allSelected = selectedTravellerIds.size == travellers.size
                AlertDialog(
                    onDismissRequest = { showTravellerPicker = false },
                    confirmButton = {
                        TextButton(onClick = { showTravellerPicker = false }) { Text(s.ok) }
                    },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(s.selectTravellers)
                            IconButton(onClick = {
                                selectedTravellerIds = if (allSelected) emptySet() else travellers.map { it.id }.toSet()
                            }) {
                                Icon(
                                    Icons.Filled.SelectAll,
                                    contentDescription = s.selectAll
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    text = {
                        LazyColumn(Modifier.heightIn(max = 300.dp)) {
                            items(travellers) { t ->
                                val isSelected = t.id in selectedTravellerIds
                                Row(
                                    Modifier.fillMaxWidth()
                                        .clickable {
                                            selectedTravellerIds = if (isSelected) selectedTravellerIds - t.id else selectedTravellerIds + t.id
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            selectedTravellerIds = if (it) selectedTravellerIds + t.id else selectedTravellerIds - t.id
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("${t.firstName} ${t.surname}".trim())
                                }
                            }
                        }
                    }
                )
            }

        } // End of Column

        // Save
        // ⬅ CHANGED — was a dimmed button with no indication of what was missing.
        val blocker = when {
            from.isBlank() || to.isBlank() -> s.needFromAndTo
            date == null -> s.needDate
            else -> null
        }
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.4f to MaterialTheme.colorScheme.background,
                    )
                )
                .padding(horizontal = 16.dp)
                // ⬅ CHANGED — lifted off the bottom edge so it reads as floating.
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
                    onSave(
                        Leg(
                            id = existing?.id ?: "leg_${kotlin.random.Random.nextLong()}",
                            completed = existing?.completed ?: false,
                            addedToCalendar = existing?.addedToCalendar ?: false,
                            // ⬅ CHANGED — cased once here. Running toTitleCase on
                            // every keystroke fought anyone typing "kempten (allgau)".
                            fromCity = from.trim().toTitleCase().ifBlank { "—" },
                            toCity = to.trim().toTitleCase().ifBlank { "—" },
                            transport = transport,
                            date = date!!,
                            timeLabel = startTime.ifBlank { "" },
                            endTimeLabel = endTime.ifBlank { "" },
                            operator = operator.trim().toTitleCase(),
                            country = country.trim(),
                            bookingRef = null,
                            travellerIds = selectedTravellerIds.toList(),
                            // keep coords when the city didn't change (edit shouldn't wipe them)
                            fromLat = if (existing?.fromCity == from) existing.fromLat else 0.0,
                            fromLng = if (existing?.fromCity == from) existing.fromLng else 0.0,
                            toLat = if (existing?.toCity == to) existing.toLat else 0.0,
                            toLng = if (existing?.toCity == to) existing.toLng else 0.0,
                            stops = stops.mapNotNull { st ->
                                st.city.trim().ifBlank { null }?.let { st.copy(city = it.toTitleCase()) }
                            },
                        )
                    )
                },
                enabled = blocker == null,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(26.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                // ⬅ CHANGED — was fillMaxWidth().height(50.dp). Wraps its label with
                // generous padding instead, so it reads as a floating action rather
                // than a bar.
                contentPadding = PaddingValues(horizontal = 56.dp, vertical = 0.dp),
                modifier = Modifier.height(52.dp),
            ) { Text(s.saveLeg) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Form building blocks
// ─────────────────────────────────────────────────────────────────────────────

/** One end of the route, with its marker on the connector line. */
@Composable
private fun RouteField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(20.dp), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Filled.Place,
                contentDescription = null,
                tint = onSurface.copy(alpha = 0.55f),
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f).padding(end = 44.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = onSurface.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(1.dp))
            PlainTextInput(
                value = value,
                onValueChange = onValueChange,
                placeholder = "",
                textStyle = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
