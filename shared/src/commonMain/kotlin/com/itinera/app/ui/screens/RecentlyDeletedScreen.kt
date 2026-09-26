package com.itinera.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Trip
import com.itinera.app.ui.components.ActionPill
import com.itinera.app.ui.components.TopBar
import com.itinera.app.ui.theme.itinera

/**
 * Trips the owner deleted in the last 30 days. Each can be restored, or erased for good right away;
 * anything older is erased automatically (see TripRepository.purgeExpiredTrips).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentlyDeletedScreen(
    trips: List<Trip>,
    daysLeft: (Trip) -> Int,
    onBack: () -> Unit,
    onRestore: (String) -> Unit,
    onDeleteForever: (String) -> Unit,
) {
    val s = LocalStrings.current
    var confirmForeverId by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        TopBar(s.recentlyDeleted, onBack = onBack)

        if (trips.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🗑️", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        s.noRecentlyDeleted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        s.recentlyDeletedInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                item {
                    Text(
                        s.recentlyDeletedInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
                items(trips, key = { it.id }) { trip ->
                    val left = daysLeft(trip)
                    Column {
                        TripCardContent(
                            trip = trip,
                            countriesWord = s.countries,
                            legsWord = s.legs,
                            legWordSingular = s.leg,
                            doneWord = s.done,
                            noDatesWord = s.noDatesYet,
                            onClick = {},
                            isPinned = false,
                        )
                        Row(
                            Modifier.fillMaxWidth().padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if (left <= 1) s.oneDayLeft else s.daysLeftN.replace("%d", left.toString()),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (left <= 3) MaterialTheme.itinera.destructive else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                            )
                            // The pills wrap onto a second line rather than overflow when a translation is long.
                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ActionPill(s.restore, Icons.Filled.Refresh, MaterialTheme.colorScheme.primary, { onRestore(trip.id) })
                                ActionPill(s.deleteForever, Icons.Filled.Delete, MaterialTheme.itinera.destructive, { confirmForeverId = trip.id })
                            }
                        }
                    }
                }
            }
        }
    }

    confirmForeverId?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmForeverId = null },
            title = { Text(s.deleteForeverQ) },
            text = { Text(s.cantBeUndone) },
            confirmButton = {
                TextButton(onClick = { onDeleteForever(id); confirmForeverId = null }) {
                    Text(s.deleteForever, color = MaterialTheme.itinera.destructive)
                }
            },
            dismissButton = { TextButton(onClick = { confirmForeverId = null }) { Text(s.cancel) } },
        )
    }
}
