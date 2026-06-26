package com.itinera.app

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** A persisted record of one scheduled reminder, so it can survive a reboot. */
@Serializable
data class PendingReminder(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val atEpochMillis: Long = 0L,
    val tripId: String = "",
)

/**
 * Device-local persistence of scheduled reminders (multiplatform-settings).
 * The OS clears scheduled alarms on reboot, so we keep our own copy and
 * re-schedule from it (see the Android BootReceiver). Also carries tripId so a
 * tapped notification can deep-link to the right trip.
 */
object ReminderStore {

    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }
    private const val KEY = "pending_reminders"

    fun all(): List<PendingReminder> {
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<PendingReminder>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Add or replace a reminder (keyed by id). */
    fun put(reminder: PendingReminder) {
        if (reminder.id.isBlank()) return
        val updated = all().filterNot { it.id == reminder.id } + reminder
        settings[KEY] = json.encodeToString(updated)
    }

    /** Remove a reminder by id. */
    fun remove(id: String) {
        val updated = all().filterNot { it.id == id }
        settings[KEY] = json.encodeToString(updated)
    }

    /** Drop reminders whose fire time has already passed (housekeeping). */
    fun pruneExpired(nowMillis: Long) {
        val updated = all().filter { it.atEpochMillis > nowMillis }
        settings[KEY] = json.encodeToString(updated)
    }
}

/**
 * A one-shot holder for a trip the user tapped a notification for. App() reads
 * and clears this on launch/resume to navigate to the trip.
 */
object PendingDeepLink {
    var tripId: String? = null
}