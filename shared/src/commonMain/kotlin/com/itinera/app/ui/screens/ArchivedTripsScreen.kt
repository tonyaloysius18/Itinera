package com.itinera.app.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Trip
import com.itinera.app.ui.components.TopBar

@Composable
fun ArchivedTripsScreen(
    trips: List<Trip>,
    onBack: () -> Unit,
    onUnarchive: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    val s = LocalStrings.current
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }

    Column(Modifier.fillMaxSize()) {
        TopBar(s.archivedTrips.toTitleCase(), onBack = onBack)

        if (trips.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📥", style = MaterialTheme.typography.displayMedium)
//                    Icon(
//                        Icons.Filled.Archive,
//                        contentDescription = null,
//                        modifier = Modifier.size(48.dp),
//                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
//                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        s.noArchivedTrips,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                items(trips, key = { it.id }) { trip ->
                    Column {
                        TripCardContent(
                            trip = trip,
                            countriesWord = s.countries,
                            legsWord = s.legs,
                            legWordSingular = s.leg,        // ⬅ ADD
                            doneWord = s.done,
                            noDatesWord = s.noDatesYet,
                            onClick = {},
                            isPinned = trip.pinned,
                        )
                        // action row beneath the card
                        Row(
                            Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { onUnarchive(trip.id) }) {
                                Icon(Icons.Filled.Unarchive, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s.unarchive)
                            }
                            Spacer(Modifier.width(4.dp))
                            TextButton(onClick = { pendingDeleteId = trip.id }) {
                                Icon(Icons.Filled.Delete, null, tint = Color(0xFFE03131), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s.delete, color = Color(0xFFE03131))
                            }
                        }
                    }
                }
            }
        }
    }

    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(s.deleteTripQ) },
            text = { Text(s.cantBeUndone) },
            confirmButton = {
                TextButton(onClick = { onDelete(pendingDeleteId!!); pendingDeleteId = null }) {
                    Text(s.delete, color = Color(0xFFE03131))
                }
            },
            dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text(s.cancel) } },
        )
    }
}
