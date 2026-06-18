package com.itinera.app.data

import com.itinera.app.model.Leg
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

/**
 * Turns a Leg into a CalendarEvent.
 *  - If a start time is set, makes a timed event. End time is used when present,
 *    otherwise the event defaults to a 1-hour duration.
 *  - If no start time, makes an all-day event on the leg's date.
 */
fun Leg.toCalendarEvent(): CalendarEvent {
    val title = "$fromCity → $toCity"
    val tz = TimeZone.currentSystemDefault()

    val start = timeLabel.trim().takeIf { it.isNotEmpty() }
        ?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
    val end = endTimeLabel.trim().takeIf { it.isNotEmpty() }
        ?.let { runCatching { LocalTime.parse(it) }.getOrNull() }

    return if (start != null) {
        val startMillis = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, start.hour, start.minute)
            .toInstant(tz).toEpochMilliseconds()

        val endMillis = if (end != null) {
            // If end is earlier than start, it's an overnight leg → end is on the NEXT day.
            val endDate = if (end < start) date.plus(DatePeriod(days = 1)) else date
            LocalDateTime(endDate.year, endDate.monthNumber, endDate.dayOfMonth, end.hour, end.minute)
                .toInstant(tz).toEpochMilliseconds()
        } else {
            startMillis + 60 * 60 * 1000   // default 1-hour duration
        }

        // Guard against an end that's not after the start (e.g. left default or typo):
        val safeEnd = if (endMillis > startMillis) endMillis else startMillis + 60 * 60 * 1000

        CalendarEvent(
            title = title,
            startEpochMillis = startMillis,
            endEpochMillis = safeEnd,
            location = toCity,
            allDay = false,
        )
    } else {
        val startMillis = date.atStartOfDayIn(tz).toEpochMilliseconds()
        CalendarEvent(
            title = title,
            startEpochMillis = startMillis,
            endEpochMillis = startMillis + 24 * 60 * 60 * 1000,
            location = toCity,
            allDay = true,
        )
    }
}