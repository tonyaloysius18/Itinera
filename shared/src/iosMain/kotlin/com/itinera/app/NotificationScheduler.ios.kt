package com.itinera.app

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

actual class NotificationScheduler actual constructor() {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun requestPermission(): Boolean =
        suspendCancellableCoroutine { cont ->
            val options = UNAuthorizationOptionAlert or
                    UNAuthorizationOptionSound or
                    UNAuthorizationOptionBadge
            center.requestAuthorizationWithOptions(options) { granted, _ ->
                if (cont.isActive) cont.resume(granted)
            }
        }

    actual fun hasPermission(): Boolean {
        // Synchronous best-effort: UN settings are async, so we optimistically
        // return true and rely on requestPermission() having been called. The UI
        // calls requestPermission() before enabling reminders, so this is safe.
        return true
    }

    actual fun schedule(id: String, title: String, body: String, atEpochMillis: Long, tripId: String) {
        // persist so the reminder list survives (mirrors Android; harmless on iOS)
        ReminderStore.put(PendingReminder(id, title, body, atEpochMillis, tripId))

        val seconds = (atEpochMillis - currentTimeMillisIos()) / 1000.0
        if (seconds <= 0) return
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound)
            setUserInfo(mapOf("tripId" to tripId))      // ⬅ carry tripId for deep-link
        }
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = seconds,
            repeats = false,
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = id,
            content = content,
            trigger = trigger,
        )
        center.addNotificationRequest(request) { /* ignore error */ }
    }

    actual fun cancel(id: String) {
        ReminderStore.remove(id)
        center.removePendingNotificationRequestsWithIdentifiers(listOf(id))
    }
}

private fun currentTimeMillisIos(): Long =
    (NSDate().timeIntervalSince1970 * 1000.0).toLong()

actual fun localDateTimeToEpochMillis(date: LocalDate, hour: Int, minute: Int): Long {
    val ldt = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, minute)
    return ldt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}