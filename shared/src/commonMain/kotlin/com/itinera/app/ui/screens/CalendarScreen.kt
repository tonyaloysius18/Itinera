package com.itinera.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itinera.app.data.rememberCalendarHelper
import com.itinera.app.data.toCalendarEvent
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Activity
import com.itinera.app.model.Leg
import com.itinera.app.model.Trip
import com.itinera.app.model.label
import com.itinera.app.parseHourMinute
import com.itinera.app.ui.components.TopBar
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/*
 * ─── New string keys (EN + FR) ──────────────────────────────────────────────
 *   upcoming          "Upcoming"          / "À venir"
 *   tomorrow          "Tomorrow"          / "Demain"
 *   inDays            "In %s days"        / "Dans %s jours"
 *   daysAgo           "%s days ago"       / "Il y a %s jours"
 *   yesterday         "Yesterday"         / "Hier"
 *   addAll            "Add all"           / "Tout ajouter"
 *   added             "Added"             / "Ajouté"
 *   addShort          "Add"               / "Ajouter"
 *   nAddedOfM         "%1$s of %2$s added" / "%1$s sur %2$s ajoutés"
 *   nothingUpcoming   "Nothing upcoming"  / "Rien à venir"
 *
 * Reused: calendar, all, today, legs, noResults, noResultsSubtitle,
 * addToPhoneCalendar.
 */

/** One row in the agenda — a leg or an activity, ordered together by clock time. */
private sealed interface AgendaEntry {
    val date: LocalDate
    val time: String

    data class LegItem(val leg: Leg) : AgendaEntry {
        override val date get() = leg.date
        override val time get() = leg.timeLabel
    }

    data class ActItem(val act: Activity) : AgendaEntry {
        override val date get() = act.date
        override val time get() = act.time
    }
}

@OptIn(ExperimentalTime::class)
private fun todayDate(): LocalDate =
    Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

/** "SAT 12 JUL" */
private fun LocalDate.headerLabel(): String {
    val dow = dayOfWeek.name.take(3)
    return "$dow ${label()}".uppercase()
}

@Composable
fun CalendarScreen(
    trips: List<Trip>,
    onMarkAdded: (tripId: String, legId: String) -> Unit,
    /**
     * ⬅ ADD — activities carry a date, time and location, and TripDetailScreen
     * already merges them with legs. A calendar that omits "Porsche Museum,
     * 09:00" is showing half the itinerary. Defaulted so existing call sites
     * still compile; pass repository.activities from App.kt to light this up.
     */
    activities: List<Activity> = emptyList(),
    /**
     * Optional: activities can't be added to the phone calendar until there's
     * an Activity.toCalendarEvent() to match the one Leg has. Pass a handler
     * and the chip appears on activity rows too.
     */
    onAddActivityToCalendar: ((Activity) -> Unit)? = null,
) {
    val s = LocalStrings.current
    val calendar = rememberCalendarHelper()
    val scrollState = rememberScrollState()
    val today = remember { todayDate() }

    // ⬅ ADD — past legs are noise by default on a screen you open to see
    // what's next.
    var upcomingOnly by rememberSaveable { mutableStateOf(true) }

    // Trip -> date -> entries, sorted by clock time within each day.
    val sections = remember(trips, activities, upcomingOnly, today) {
        trips.mapNotNull { trip ->
            val entries: List<AgendaEntry> =
                trip.legs.map { AgendaEntry.LegItem(it) } +
                        activities.filter { it.tripId == trip.id }.map { AgendaEntry.ActItem(it) }

            val filtered = if (upcomingOnly) entries.filter { it.date >= today } else entries
            if (filtered.isEmpty()) return@mapNotNull null

            val byDay = filtered
                .sortedWith(
                    compareBy(
                        { it.date },
                        { parseHourMinute(it.time).first },
                        { parseHourMinute(it.time).second },
                    )
                )
                .groupBy { it.date }
            trip to byDay
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(s.calendar)

        if (trips.none { it.legs.isNotEmpty() } && activities.isEmpty()) {
            CalendarEmptyState(title = s.noResults, subtitle = s.noResultsSubtitle)
            return@Column
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            RangeToggle(
                upcomingOnly = upcomingOnly,
                onSelect = { upcomingOnly = it },
            )

            Spacer(Modifier.height(16.dp))

            if (sections.isEmpty()) {
                Text(
                    s.nothingUpcoming,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }

            sections.forEach { (trip, byDay) ->
                TripSectionHeader(
                    trip = trip,
                    onAddAll = {
                        // Only legs have a calendar event today; see the
                        // onAddActivityToCalendar note above.
                        trip.legs.filterNot { it.addedToCalendar }.forEach { leg ->
                            calendar.addEvent(leg.toCalendarEvent())
                            onMarkAdded(trip.id, leg.id)
                        }
                    },
                )

                byDay.forEach { (date, entries) ->
                    DayHeaderRow(date = date, today = today, count = entries.size)

                    Box(Modifier.fillMaxWidth()) {
                        // Spine behind the markers, so a day's entries read as one run.
                        if (entries.size > 1) {
                            val spine = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            Canvas(Modifier.matchParentSize()) {
                                val x = 54.dp.toPx() + 11.dp.toPx()
                                val inset = 26.dp.toPx()
                                drawLine(
                                    color = spine,
                                    start = Offset(x, inset),
                                    end = Offset(x, size.height - inset),
                                    strokeWidth = 1.5.dp.toPx(),
                                )
                            }
                        }

                        Column(Modifier.fillMaxWidth()) {
                            entries.forEach { entry ->
                                when (entry) {
                                    is AgendaEntry.LegItem -> {
                                        val leg = entry.leg
                                        AgendaRow(
                                            startTime = leg.timeLabel,
                                            endTime = leg.endTimeLabel,
                                            icon = transportIcon(leg.transport),
                                            highlighted = false,
                                            title = "${leg.fromCity} → ${leg.toCity}",
                                            subtitle = legSubtitle(leg),
                                            added = leg.addedToCalendar,
                                            onAdd = {
                                                calendar.addEvent(leg.toCalendarEvent())
                                                onMarkAdded(trip.id, leg.id)
                                            },
                                        )
                                    }

                                    is AgendaEntry.ActItem -> {
                                        val act = entry.act
                                        AgendaRow(
                                            startTime = act.time,
                                            endTime = "",
                                            icon = Icons.Filled.Place,
                                            highlighted = false,
                                            title = act.title,
                                            subtitle = act.location,
                                            added = false,
                                            onAdd = onAddActivityToCalendar?.let { handler ->
                                                { handler(act) }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
            }

            // Clears the floating nav pill — content used to scroll underneath it.
            Spacer(Modifier.height(120.dp))
        }
    }
}

private fun legSubtitle(leg: Leg): String {
    val duration = if (leg.timeLabel.isNotBlank() && leg.endTimeLabel.isNotBlank()) {
        val (h1, m1) = parseHourMinute(leg.timeLabel)
        val (h2, m2) = parseHourMinute(leg.endTimeLabel)
        var diff = (h2 * 60 + m2) - (h1 * 60 + m1)
        if (diff < 0) diff += 24 * 60   // crosses midnight
        val h = diff / 60
        val m = diff % 60
        if (h > 0) "${h}h${m}m" else "${m}m"
    } else ""
    return listOf(leg.operator, duration).filter { it.isNotBlank() }.joinToString(" · ")
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RangeToggle(upcomingOnly: Boolean, onSelect: (Boolean) -> Unit) {
    val s = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
    ) {
        Row(Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            listOf(true to s.upcoming, false to s.all).forEach { (value, label) ->
                val active = value == upcomingOnly
                Surface(
                    modifier = Modifier.weight(1f).clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onSelect(value) },
                    shape = RoundedCornerShape(9.dp),
                    color = if (active) MaterialTheme.colorScheme.surface else Color.Transparent,
                ) {
                    Box(Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (active) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripSectionHeader(trip: Trip, onAddAll: () -> Unit) {
    val s = LocalStrings.current
    val addedCount = trip.legs.count { it.addedToCalendar }
    val allAdded = trip.legs.isNotEmpty() && addedCount == trip.legs.size

    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(width = 3.dp, height = 28.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                trip.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                s.nAddedOfM
                    .replace("%1\$s", "$addedCount")
                    .replace("%2\$s", "${trip.legs.size}"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
        // ⬅ ADD — eighteen legs used to mean eighteen taps.
        if (!allAdded) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.clickable { onAddAll() },
            ) {
                Text(
                    s.addAll,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun DayHeaderRow(date: LocalDate, today: LocalDate, count: Int) {
    val s = LocalStrings.current
    val diff = date.toEpochDays() - today.toEpochDays()
    val relative = when {
        diff == 0L -> s.today
        diff == 1L -> s.tomorrow
        diff == -1L -> s.yesterday
        diff > 1L -> s.inDays.replace("%s", "$diff")
        else -> s.daysAgoRelative.replace("%s", "${-diff}")
    }
    val isToday = diff == 0L

    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            date.headerLabel(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )
        Spacer(Modifier.width(8.dp))
        if (isToday) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            ) {
                Text(
                    relative,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                )
            }
        } else {
            Text(
                relative,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            "$count ${s.legs}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        )
    }
}

/**
 * Agenda row: fixed-width time rail, marker, then content.
 *
 * The rail is what stops long city pairs wrapping to two lines and leaving
 * ragged rows — the title always starts at the same x and ellipsizes instead
 * of growing the row.
 */
@Composable
private fun AgendaRow(
    startTime: String,
    endTime: String,
    icon: ImageVector,
    highlighted: Boolean,
    title: String,
    subtitle: String,
    added: Boolean,
    onAdd: (() -> Unit)?,
) {
    val s = LocalStrings.current
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.width(42.dp)) {
            Text(
                startTime.ifBlank { "—" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
            if (endTime.isNotBlank()) {
                Text(
                    endTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.45f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Box(
            Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    if (highlighted) MaterialTheme.colorScheme.primary
                    else onSurface.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (highlighted) Color.White else onSurface.copy(alpha = 0.65f),
                modifier = Modifier.size(13.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.45f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (onAdd != null) {
            Spacer(Modifier.width(8.dp))
            // ⬅ CHANGED — was a bare icon where dim meant "already added", which
            // reads as disabled. Now the state says what it is.
            val green = Color(0xFF1D9E75)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (added) green.copy(alpha = 0.14f) else Color.Transparent,
                border = if (added) null
                else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.clickable { onAdd() },   // still re-tappable when added
            ) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (added) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = s.addToPhoneCalendar,
                        tint = if (added) green else onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (added) s.added else s.addShort,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (added) green else onSurface.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarEmptyState(title: String, subtitle: String) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // ⬅ CHANGED — was Text("🗓️"), which doesn't tint with the theme.
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
        )
    }
}