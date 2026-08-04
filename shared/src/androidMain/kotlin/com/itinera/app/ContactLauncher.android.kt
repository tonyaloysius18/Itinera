package com.itinera.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri

private fun launch(uri: Uri) {
    val ctx = AndroidApp.context ?: return
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        // Launching from a non-Activity context requires this.
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        ctx.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No mail client or dialer — nothing sensible to fall back to.
    }
}

actual fun openEmail(email: String) {
    if (email.isBlank()) return
    launch(Uri.parse("mailto:${Uri.encode(email)}"))
}

actual fun openPhone(phone: String) {
    if (phone.isBlank()) return
    // ACTION_VIEW on tel: opens the dialer pre-filled. ACTION_CALL would place
    // the call directly and needs the CALL_PHONE permission — not worth it.
    launch(Uri.parse("tel:${Uri.encode(phone)}"))
}