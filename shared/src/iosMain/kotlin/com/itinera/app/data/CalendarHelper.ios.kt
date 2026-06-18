package com.itinera.app.data

import androidx.compose.runtime.Composable

actual class CalendarHelper {
    actual fun addEvent(event: CalendarEvent) {
        IosCalendar.provider?.invoke(
            event.title, event.location,
            event.startEpochMillis, event.endEpochMillis, event.allDay,
        )
    }
}

@Composable
actual fun rememberCalendarHelper(): CalendarHelper = CalendarHelper()

object IosCalendar {
    // title, location, startMillis, endMillis, allDay
    var provider: ((String, String, Long, Long, Boolean) -> Unit)? = null
}