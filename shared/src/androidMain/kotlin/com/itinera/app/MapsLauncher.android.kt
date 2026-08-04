package com.itinera.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

actual fun openInMaps(label: String, latitude: Double?, longitude: Double?) {
    val ctx = AndroidApp.context ?: return
    if (label.isBlank() && (latitude == null || longitude == null)) return

    val encoded = Uri.encode(label)
    // geo:lat,lng?q=lat,lng(label) drops a pin exactly; geo:0,0?q=<text> makes
    // the maps app search. 0,0 is the documented "no coordinates" form, not a
    // point in the Atlantic.
    val geo = if (latitude != null && longitude != null) {
        Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encoded)")
    } else {
        Uri.parse("geo:0,0?q=$encoded")
    }

    // ⬅ CHANGED — try Google Maps by package first. A bare geo: intent shows the
    // app chooser every time when more than one app handles it; naming the
    // package goes straight there, and the unpackaged attempt still covers
    // devices without Google Maps.
    val attempts = listOf(
        Intent(Intent.ACTION_VIEW, geo).setPackage("com.google.android.apps.maps"),
        Intent(Intent.ACTION_VIEW, geo),
        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$encoded")),
    )

    for (intent in attempts) {
        // Launching from a non-Activity context requires this.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (ctx.launch(intent)) return
    }
}

private fun Context.launch(intent: Intent): Boolean = try {
    startActivity(intent)
    true
} catch (_: ActivityNotFoundException) {
    false
}