package com.itinera.app

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * Holds the Application context so the shared scheduler can reach Android APIs.
 * Set this once from MainActivity.onCreate via AndroidApp.init(this).
 */
object AndroidApp {
    lateinit var context: Context
        private set

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }
}

private const val CHANNEL_ID = "itinera_leg_reminders"
private const val CHANNEL_NAME = "Trip reminders"

actual class NotificationScheduler actual constructor() {

    private val ctx: Context get() = AndroidApp.context

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply { description = "Reminders before your trip legs depart" }
                mgr.createNotificationChannel(channel)
            }
        }
    }

    actual fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        }
        return NotificationManagerCompat.from(ctx).areNotificationsEnabled()
    }

    /**
     * The runtime permission dialog must be shown from an Activity, so the UI
     * layer drives it (see MainActivity). Here we just report current state.
     */
    actual suspend fun requestPermission(): Boolean = hasPermission()

    actual fun schedule(id: String, title: String, body: String, atEpochMillis: Long) {
        if (atEpochMillis <= System.currentTimeMillis()) return
        ensureChannel()

        val intent = Intent(ctx, AlarmReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
            putExtra("body", body)
        }
        val pending = PendingIntent.getBroadcast(
            ctx,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val alarm = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canExact =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarm.canScheduleExactAlarms()
        if (canExact) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atEpochMillis, pending)
        } else {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atEpochMillis, pending)
        }
    }

    actual fun cancel(id: String) {
        val intent = Intent(ctx, AlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            ctx,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val alarm = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pending)
    }
}

/** Fired by AlarmManager at the scheduled time; posts the actual notification. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val id = intent.getStringExtra("id") ?: return
        val title = intent.getStringExtra("title") ?: "Itinera"
        val body = intent.getStringExtra("body") ?: ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                mgr.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                )
            }
        }

        // Tap -> open the app (launcher intent; no cross-module class reference needed).
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val contentPending = launchIntent?.let {
            PendingIntent.getActivity(
                context,
                id.hashCode(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        // Proper notification silhouette icon. Generate ic_stat_itinera via
        // Android Studio Image Asset (Notification Icons). Falls back to a
        // built-in silhouette if you haven't created one yet.
        val iconRes = runCatching {
            val resId = context.resources.getIdentifier(
                "ic_stat_itinera", "drawable", context.packageName,
            )
            if (resId != 0) resId else android.R.drawable.stat_notify_chat
        }.getOrDefault(android.R.drawable.stat_notify_chat)

        val builder = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setColor(0xFF85B7EB.toInt())                 // brand blue tint
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)

        if (contentPending != null) builder.setContentIntent(contentPending)

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(id.hashCode(), builder.build())
        }
    }
}

actual fun localDateTimeToEpochMillis(date: LocalDate, hour: Int, minute: Int): Long {
    val ldt = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, minute)
    return ldt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}