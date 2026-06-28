package com.itinera.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.data.rememberCalendarHelper
import com.itinera.app.data.toCalendarEvent
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Trip
import com.itinera.app.model.label
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.TopBar

/**
 * Calendar derives its events from existing legs — no separate data. A full month
 * grid is omitted in the skeleton; this shows the agenda list, which is the part
 * that proves the data wiring. Add a month grid + native calendar sync (.ics on
 * Android Intent / iOS EventKit) in production.
 */
@Composable
fun CalendarScreen(
    trips: List<Trip>,
    onMarkAdded: (tripId: String, legId: String) -> Unit,   // ⬅ ADD
) {
    val s = LocalStrings.current
    val calendar = rememberCalendarHelper()

    Column(Modifier.fillMaxSize()) {
        TopBar(s.calendar)
            if (trips.none { it.legs.isNotEmpty() }) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("🗓️", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        s.noResults,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        s.noResultsSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            } else {
        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            trips.forEach { trip ->
                Text(
                    trip.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                trip.legs.forEach { leg ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = CardShape,
                        tonalElevation = 1.dp,
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(transportIcon(leg.transport), null, tint = Color(0xFF378ADD), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${leg.fromCity} → ${leg.toCity}", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${leg.date.label()} · ${leg.timeLabel}".trimEnd(' ', '·'),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = s.addToPhoneCalendar,
                                tint = if (leg.addedToCalendar)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)   // dimmed = already added (a hint)
                                else
                                    MaterialTheme.colorScheme.primary,                       // bright = not yet added
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {                                             // always re-tappable
                                        calendar.addEvent(leg.toCalendarEvent())
                                        onMarkAdded(trip.id, leg.id)
                                    },
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}

