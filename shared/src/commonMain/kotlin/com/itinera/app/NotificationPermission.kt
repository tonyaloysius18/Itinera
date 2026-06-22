package com.itinera.app

/**
 * Bridge so shared/Compose code can trigger the platform notification-permission
 * dialog. On Android, MainActivity sets [requester] to launch the system
 * POST_NOTIFICATIONS dialog. On iOS the request is handled directly by
 * NotificationScheduler.requestPermission(), so this stays null there.
 */
object NotificationPermission {
    var requester: (() -> Unit)? = null

    /** Triggers the platform permission dialog if a requester is registered. */
    fun request() {
        requester?.invoke()
    }
}