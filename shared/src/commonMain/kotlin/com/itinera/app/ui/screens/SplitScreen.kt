package com.itinera.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Expense
import com.itinera.app.model.Trip
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.EmptyState
import com.itinera.app.ui.components.TopBar

@Composable
fun SplitScreen(
    trips: List<Trip>,
    expenses: List<Expense>,
    onOpenTrip: (String) -> Unit,
) {
    val s = LocalStrings.current

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }

    Column(Modifier.fillMaxSize()) {
        TopBar(s.split.toTitleCase())

        if (trips.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.ReceiptLong,
                title = s.noTripsToSplit,
                subtitle = s.noTripsToSplitSubtitle,
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(trips, key = { it.id }) { trip ->
                    val tripExpenses = expenses.filter { it.tripId == trip.id }
                    val total = tripExpenses.sumOf { it.amount }
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenTrip(trip.id) },
                        shape = CardShape,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(trip.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(2.dp))
                                val count = tripExpenses.size
                                Text(
                                    "$count ${if (count == 1) s.expenseSingular else s.expensePlural}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                )
                            }
                            Text(
                                formatMoney(total, trip.currencyCode),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}