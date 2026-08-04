package com.itinera.app

/**
 * Opens a mail composer for [email].
 *
 * Silently does nothing if no mail app is configured — a dead tap beats a
 * crash for a convenience action.
 */
expect fun openEmail(email: String)

/** Opens the dialer pre-filled with [phone]. Does not place the call. */
expect fun openPhone(phone: String)