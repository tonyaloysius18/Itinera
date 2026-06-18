package com.itinera.app.data

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class CalendarHelper(private val context: Context) {
    actual fun addEvent(event: CalendarEvent) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startEpochMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endEpochMillis)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, event.allDay)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

@Composable
actual fun rememberCalendarHelper(): CalendarHelper {
    val context = LocalContext.current
    return CalendarHelper(context)
}