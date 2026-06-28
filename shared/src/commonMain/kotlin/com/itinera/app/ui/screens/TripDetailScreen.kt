package com.itinera.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Activity
import com.itinera.app.model.Trip
import com.itinera.app.model.label
import com.itinera.app.ui.components.Progress
import com.itinera.app.ui.components.TopBar
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ConfirmationNumber
import com.itinera.app.model.DocItem


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripDetailScreen(
    trip: Trip,
    activities: List<Activity>,
    documents: List<DocItem> = emptyList(),
    onBack: () -> Unit,
    onTravellers: () -> Unit,
    onDocuments: () -> Unit,
    onOpenDoc: (String) -> Unit = {},
    onAddLeg: () -> Unit,
    onAddPlace: () -> Unit,
    onChecklist: () -> Unit,
    onToggleLeg: (String) -> Unit,
    onEditLeg: (String) -> Unit,
    onEditActivity: (String) -> Unit,
    onDeleteLeg: (String) -> Unit,
    onToggleActivity: (String) -> Unit,
    onDeleteActivity: (String) -> Unit,
    canEdit: Boolean = true,
    onMembers: () -> Unit,
) {
    val s = LocalStrings.current
    val done = trip.legs.count { it.completed }

    var pendingDeleteLegId by remember { mutableStateOf<String?>(null) }
    var pendingDeleteActivityId by remember { mutableStateOf<String?>(null) }

    var showAddChooser by remember { mutableStateOf(false) }

    // all distinct dates in the trip, earliest first → these define the day numbers
    val allDates = (trip.legs.map { it.date } + activities.map { it.date })
        .distinct()
        .sorted()

    // group each list by date
    val legsByDate = trip.legs.groupBy { it.date }
    val actsByDate = activities.groupBy { it.date }

    // the "next up" leg = first incomplete one, in date+time order
    val nextLegId = trip.legs.sortedWith(compareBy({ it.date }, { it.timeLabel }))
        .firstOrNull { !it.completed }?.id

    Column(Modifier.fillMaxSize()) {
        TopBar(
            trip.title,
            onBack = onBack,
            trailing = {
                Row {
                    IconButton(onClick = onTravellers) {
                        Icon(Icons.Filled.People, contentDescription = s.travellers, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDocuments) {
                        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = s.documents, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onMembers) {
                        Icon(Icons.Filled.Groups, contentDescription = s.members, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
        )
        Spacer(Modifier.height(8.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                "$done ${s.legsTravelled}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(6.dp))
            Progress(if (trip.legs.isEmpty()) 0f else done.toFloat() / trip.legs.size)
        }
        Spacer(Modifier.height(14.dp))

        // ⬅ Box wraps the scrollable list + the floating buttons
        Box(Modifier.weight(1f).fillMaxWidth()) {

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                if (allDates.isEmpty()) {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 300.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Filled.Flight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            modifier = Modifier.size(56.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            s.noLegsYet,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            s.noLegsSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                    }
                } else {
                    allDates.forEachIndexed { index, date ->
                        val dayNumber = index + 1
                        Text(
                            "${s.day} $dayNumber · ${date.label()}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        )

                        // ---- Legs for this day ----
                        legsByDate[date].orEmpty().forEach { leg ->
                            val isNext = leg.id == nextLegId
                            var showMenu by remember { mutableStateOf(false) }

                            Box {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { if (canEdit) onToggleLeg(leg.id) },
                                            onLongClick = { if (canEdit) showMenu = true },
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    if (leg.completed) {
                                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF1D9E75), modifier = Modifier.size(20.dp))
                                    } else {
                                        Icon(
                                            Icons.Outlined.Circle, null,
                                            tint = if (isNext) Color(0xFFBA7517) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        val operatorSuffix = if (leg.operator.isNotBlank()) " (${leg.operator})" else ""
                                        Text(
                                            "${leg.fromCity} → ${leg.toCity}$operatorSuffix",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isNext) FontWeight.Medium else FontWeight.Normal,
                                            textDecoration = if (leg.completed) TextDecoration.LineThrough else null,
                                            color = if (leg.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(transportIcon(leg.transport), null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            Spacer(Modifier.width(6.dp))
                                            val tail = if (isNext) {
                                                if (leg.timeLabel.isNotBlank()) "${leg.timeLabel} · ${s.nextUp}" else s.nextUp
                                            } else {
                                                leg.timeLabel
                                            }
                                            Text(
                                                tail,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isNext) Color(0xFFBA7517) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            )
                                        }
                                    }
                                }

                                // ---- Ticket / document shortcut for this leg ----
                                val legDocs = documents.filter { it.legId == leg.id }
                                if (legDocs.isNotEmpty()) {
                                    var showLegDocs by remember(leg.id) { mutableStateOf(false) }
                                    IconButton(
                                        onClick = {
                                            if (legDocs.size == 1) onOpenDoc(legDocs.first().id)
                                            else showLegDocs = true
                                        },
                                        modifier = Modifier.size(34.dp).align(Alignment.CenterEnd),
                                    ) {
                                        Icon(
                                            Icons.Filled.ConfirmationNumber,
                                            contentDescription = s.viewTicket,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    if (showLegDocs) {
                                        AlertDialog(
                                            onDismissRequest = { showLegDocs = false },
                                            title = { Text("${leg.fromCity} → ${leg.toCity}") },
                                            shape = RoundedCornerShape(16.dp),
                                            text = {
                                                Column {
                                                    legDocs.forEach { d ->
                                                        Row(
                                                            Modifier.fillMaxWidth()
                                                                .clickable { showLegDocs = false; onOpenDoc(d.id) }
                                                                .padding(vertical = 12.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                        ) {
                                                            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                                            Spacer(Modifier.width(10.dp))
                                                            Text(d.title, style = MaterialTheme.typography.bodyLarge)
                                                        }
                                                    }
                                                }
                                            },
                                            confirmButton = {},
                                            dismissButton = { TextButton(onClick = { showLegDocs = false }) { Text(s.close) } },
                                        )
                                    }
                                }
                            }

                            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp))) {
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    offset = DpOffset(x = 280.dp, y = 0.dp),
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(s.edit) },
                                        leadingIcon = { Icon(Icons.Filled.Edit, null) },
                                        onClick = { showMenu = false; onEditLeg(leg.id) },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(s.delete, color = Color(0xFFE03131)) },
                                        leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFE03131)) },
                                        onClick = { showMenu = false; pendingDeleteLegId = leg.id },
                                    )
                                }
                            }
                        }

                        // ---- Places for this day ----
                        actsByDate[date].orEmpty().forEach { act ->
                            var showMenu by remember { mutableStateOf(false) }

                            Box {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { if (canEdit) onToggleActivity(act.id) },
                                            onLongClick = { if (canEdit) showMenu = true },
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    if (act.completed) {
                                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF1D9E75), modifier = Modifier.size(20.dp))
                                    } else {
                                        Icon(Icons.Filled.Place, null, tint = Color(0xFF378ADD), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            act.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            textDecoration = if (act.completed) TextDecoration.LineThrough else null,
                                            color = if (act.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                        )
                                        val tail = listOf(act.time, act.location).filter { it.isNotBlank() }.joinToString(" · ")
                                        if (tail.isNotBlank()) {
                                            Spacer(Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    tail,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                )
                                            }
                                        }
                                    }
                                }

                                MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp))) {
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        offset = DpOffset(x = 280.dp, y = 0.dp),
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(s.edit) },
                                            leadingIcon = { Icon(Icons.Filled.Edit, null) },
                                            onClick = { showMenu = false; onEditActivity(act.id) },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(s.delete, color = Color(0xFFE03131)) },
                                            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFE03131)) },
                                            onClick = { showMenu = false; pendingDeleteActivityId = act.id },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // ⬅ clearance so the last rows scroll above the floating buttons
                Spacer(Modifier.height(96.dp))
            }

            // ⬅ floating buttons pinned to the bottom of the Box
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = onChecklist,
                    modifier = Modifier.padding(bottom = 60.dp).height(50.dp),
                    contentPadding = PaddingValues(horizontal = 25.dp, vertical = 8.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                ) {
                    Text(s.beforeYouGo, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
                if (canEdit) {                                          // ⬅ wrap starts
                    Spacer(Modifier.width(30.dp))
                    Button(
                        onClick = { showAddChooser = true },
                        modifier = Modifier.padding(bottom = 60.dp).height(50.dp),
                        contentPadding = PaddingValues(horizontal = 25.dp, vertical = 8.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(s.add, style = MaterialTheme.typography.bodyMedium)
                    }
                }                                                       // ⬅ wrap ends
            }
        }
    }

    if (showAddChooser) {
        AlertDialog(
            onDismissRequest = { showAddChooser = false },
            confirmButton = {},
            title = { Text(s.addToItinerary) },
            shape = RoundedCornerShape(16.dp),
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text(s.travel) },
                        supportingContent = { Text(s.aLegBetween) },
                        leadingContent = { Icon(Icons.Filled.Flight, null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { showAddChooser = false; onAddLeg() },
                    )
                    ListItem(
                        headlineContent = { Text(s.place) },
                        supportingContent = { Text(s.somewhereToVisit) },
                        leadingContent = { Icon(Icons.Filled.Place, null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { showAddChooser = false; onAddPlace() },
                    )
                }
            },
        )
    }

    if (pendingDeleteLegId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteLegId = null },
            title = { Text(s.deleteLegQ) },
            text = { Text(s.cantBeUndone) },
            confirmButton = {
                TextButton(onClick = { onDeleteLeg(pendingDeleteLegId!!); pendingDeleteLegId = null }) {
                    Text(s.delete, color = Color(0xFFE03131))
                }
            },
            dismissButton = { TextButton(onClick = { pendingDeleteLegId = null }) { Text(s.cancel) } },
        )
    }

    if (pendingDeleteActivityId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteActivityId = null },
            title = { Text(s.deletePlaceQ) },
            text = { Text(s.cantBeUndone) },
            confirmButton = {
                TextButton(onClick = { onDeleteActivity(pendingDeleteActivityId!!); pendingDeleteActivityId = null }) {
                    Text(s.delete, color = Color(0xFFE03131))
                }
            },
            dismissButton = { TextButton(onClick = { pendingDeleteActivityId = null }) { Text(s.cancel) } },
        )
    }
}
