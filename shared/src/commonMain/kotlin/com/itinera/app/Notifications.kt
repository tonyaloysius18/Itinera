package com.itinera.app

import com.itinera.app.model.Leg
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime

/**
 * Cross-platform local-notification scheduler.
 *
 * Implemented per-platform (androidMain / iosMain). Schedules a single local
 * notification to fire at an absolute wall-clock time, even if the app is closed.
 *
 * `id` should be STABLE per reminder (we use the leg id) so re-scheduling or
 * cancelling targets the same pending notification.
 */
expect class NotificationScheduler() {

    /** Ask the OS for permission to post notifications. Returns true if granted. */
    suspend fun requestPermission(): Boolean

    /** True if the user has already granted notification permission. */
    fun hasPermission(): Boolean

    /**
     * Schedule (or replace) a notification with the given id to fire at
     * [atEpochMillis]. If the time is already in the past, nothing is scheduled.
     */
    fun schedule(id: String, title: String, body: String, atEpochMillis: Long)

    /** Cancel a previously scheduled notification by id. */
    fun cancel(id: String)
}

/** Reminder offset choices, in minutes. 0 means reminders are OFF. */
object ReminderOffset {
    const val OFF = 0
    const val MIN_15 = 15
    const val MIN_30 = 30
    const val HOUR_1 = 60
    const val DAY_1 = 1440

    val choices = listOf(MIN_30, HOUR_1, DAY_1)
}

/**
 * Compute the absolute epoch-millis at which a leg's reminder should fire,
 * given the user's chosen [offsetMinutes] before departure.
 *
 * Returns null when:
 *  - reminders are off (offset == 0), or
 *  - the computed fire time is in the past.
 *
 * `timeLabel` is expected as 24-hour "HH:mm". If it's blank or unparseable we
 * fall back to 09:00 local time on the leg's date.
 */
@OptIn(ExperimentalTime::class)
fun legReminderFireTime(
    leg: Leg,
    offsetMinutes: Int,
    nowMillis: Long,
): Long? {
    if (offsetMinutes <= 0) return null

    val (hour, minute) = parseHourMinute(leg.timeLabel)
    val departMillis = localDateTimeToEpochMillis(leg.date, hour, minute)
    val fireAt = departMillis - offsetMinutes * 60_000L
    return if (fireAt > nowMillis) fireAt else null
}

/** Parse "HH:mm" (24h). Falls back to 09:00 on blank/garbage. */
internal fun parseHourMinute(timeLabel: String): Pair<Int, Int> {
    val parts = timeLabel.trim().split(":")
    if (parts.size == 2) {
        val h = parts[0].toIntOrNull()
        val m = parts[1].toIntOrNull()
        if (h != null && m != null && h in 0..23 && m in 0..59) return h to m
    }
    return 9 to 0
}

/**
 * Convert a local date + hour/minute to epoch millis in the device's time zone.
 * Implemented per-platform to use the correct local time zone.
 */
expect fun localDateTimeToEpochMillis(date: LocalDate, hour: Int, minute: Int): Long