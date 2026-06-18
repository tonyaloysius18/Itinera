package com.itinera.app.data

import androidx.compose.runtime.Composable

data class CalendarEvent(
    val title: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val location: String = "",
    val allDay: Boolean = false,
)

expect class CalendarHelper {
    fun addEvent(event: CalendarEvent)
}

@Composable
expect fun rememberCalendarHelper(): CalendarHelper