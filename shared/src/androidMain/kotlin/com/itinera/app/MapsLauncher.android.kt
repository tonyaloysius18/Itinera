package com.itinera.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri

actual fun openInMaps(label: String, latitude: Double?, longitude: Double?) {
    val ctx = AndroidApp.context ?: return
    val encoded = Uri.encode(label)

    // geo:lat,lng?q=lat,lng(label) drops a pin exactly; geo:0,0?q=<text> makes
    // the maps app search. 0,0 is the documented "no coordinates" form, not a
    // location in the Atlantic.
    val uri = if (latitude != null && longitude != null) {
        Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encoded)")
    } else {
        Uri.parse("geo:0,0?q=$encoded")
    }

    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        // Launching from a non-Activity context requires this.
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        ctx.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No maps app installed — fall back to the browser.
        val web = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$encoded"),
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        runCatching { ctx.startActivity(web) }
    }
}