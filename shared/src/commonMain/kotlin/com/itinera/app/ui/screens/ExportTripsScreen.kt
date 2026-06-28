package com.itinera.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.data.TripExporter
import com.itinera.app.data.buildItineraryPdf
import com.itinera.app.data.rememberFileSharer
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Activity
import com.itinera.app.model.Expense
import com.itinera.app.model.Trip
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.EmptyState
import com.itinera.app.ui.components.TopBar

@Composable
fun ExportTripsScreen(
    trips: List<Trip>,
    activitiesForTrip: (String) -> List<Activity>,
    expensesForTrip: (String) -> List<Expense>,
    onBack: () -> Unit,
) {
    val s = LocalStrings.current
    val sharer = rememberFileSharer()

    // selection state — default: only the FIRST trip is selected
    val selected = remember {
        mutableStateMapOf<String, Boolean>().apply {
            trips.forEachIndexed { index, t -> put(t.id, index == 0) }
        }
    }
    val selectedTrips = trips.filter { selected[it.id] == true }
    val anySelected = selectedTrips.isNotEmpty()
    val allSelected = trips.isNotEmpty() && trips.all { selected[it.id] == true }

    var shareMenuOpen by remember { mutableStateOf(false) }

    fun bundles() = selectedTrips.map { trip ->
        TripExporter.TripBundle(
            trip = trip,
            activities = activitiesForTrip(trip.id),
            expenses = expensesForTrip(trip.id),
        )
    }

    fun safeName(ext: String): String {
        val base = if (selectedTrips.size == 1)
            selectedTrips.first().title.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').ifBlank { "trip" }
        else "itinera_trips"
        return "$base.$ext"
    }

    fun shareText() {
        val text = TripExporter.toText(bundles())
        sharer.share(text.encodeToByteArray(), safeName("txt"), "text/plain")
    }

    fun sharePdf() {
        val text = TripExporter.toText(bundles())
        val title = if (selectedTrips.size == 1) selectedTrips.first().title else "Itinera — Trips"
        val pdf = buildItineraryPdf(title, text.split("\n"))
        sharer.share(pdf, safeName("pdf"), "application/pdf")
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = s.exportTrips,
            onBack = onBack,
            trailing = {
                if (trips.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // select-all / deselect-all
                        IconButton(onClick = { trips.forEach { selected[it.id] = !allSelected } }) {
                            Icon(
                                if (allSelected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                contentDescription = if (allSelected) s.deselectAll else s.selectAll,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        // share — opens a small dropdown (text / pdf)
                        Box {
                            IconButton(
                                onClick = { if (anySelected) shareMenuOpen = true },
                                enabled = anySelected,
                            ) {
                                Icon(
                                    Icons.Filled.IosShare,
                                    contentDescription = s.share,
                                    tint = if (anySelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                )
                            }
                            DropdownMenu(
                                expanded = shareMenuOpen,
                                onDismissRequest = { shareMenuOpen = false },
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                DropdownMenuItem(
                                    text = { Text(s.shareAsText) },
                                    leadingIcon = { Icon(Icons.Filled.Description, null, modifier = Modifier.size(18.dp)) },
                                    onClick = { shareMenuOpen = false; shareText() },
                                )
                                DropdownMenuItem(
                                    text = { Text(s.shareAsPdf) },
                                    leadingIcon = { Icon(Icons.Filled.PictureAsPdf, null, modifier = Modifier.size(18.dp)) },
                                    onClick = { shareMenuOpen = false; sharePdf() },
                                )
                            }
                        }
                    }
                }
            },
        )

        if (trips.isEmpty()) {

            Column(
                Modifier.fillMaxSize().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("📝", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(12.dp))
                Text(
                    s.noTripsToExport,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    s.noTripsToExportSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }
//            EmptyState(
//                icon = Icons.Filled.Description,
//                title = s.noTripsToExport,
//                subtitle = s.noTripsToExportSubtitle,
//                modifier = Modifier.weight(1f),
//            )
            return@Column
        }

        // Selectable trip cards
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                s.selectTripsToExport,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                modifier = Modifier.padding(vertical = 8.dp),
            )

            trips.forEach { trip ->
                val checked = selected[trip.id] == true
                val expCount = expensesForTrip(trip.id).size

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShape)
                        .clickable { selected[trip.id] = !checked },
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 1.dp,
                    border = BorderStroke(
                        1.dp,
                        if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    ),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RoundCheck(checked = checked)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(trip.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "${trip.legs.size} ${s.legs} · $expCount ${if (expCount == 1) s.expenseSingular else s.expensePlural}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun RoundCheck(checked: Boolean) {
    val border = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    Box(
        Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (checked) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(2.dp, border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
        }
    }
}