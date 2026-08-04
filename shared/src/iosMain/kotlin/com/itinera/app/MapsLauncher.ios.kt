package com.itinera.app

import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.create
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIApplication

/**
 * ⚠️ REQUIRES an Info.plist entry, or canOpenURL always returns false for
 * custom schemes and you fall through to the browser:
 *
 *     <key>LSApplicationQueriesSchemes</key>
 *     <array>
 *         <string>comgooglemaps</string>
 *         <string>maps</string>
 *     </array>
 *
 * In iosApp/Info.plist, alongside the existing top-level keys.
 */
actual fun openInMaps(label: String, latitude: Double?, longitude: Double?) {
    if (label.isBlank() && (latitude == null || longitude == null)) return

    val encoded = NSString.create(string = label)
        .stringByAddingPercentEncodingWithAllowedCharacters(
            NSCharacterSet.URLQueryAllowedCharacterSet
        ) ?: return

    val hasCoords = latitude != null && longitude != null

    // ⬅ CHANGED — was https://maps.apple.com alone. That's a universal link, and
    // when Safari claims it first you land in the browser instead of the app.
    // Custom schemes open the app directly. Ordered by preference, https last as
    // a genuine fallback rather than the primary path.
    val candidates = buildList {
        if (hasCoords) {
            add("comgooglemaps://?q=$encoded&center=$latitude,$longitude&zoom=16")
            add("maps://?ll=$latitude,$longitude&q=$encoded")
        } else {
            add("comgooglemaps://?q=$encoded")
            add("maps://?q=$encoded")
        }
        add(
            if (hasCoords) "https://maps.apple.com/?ll=$latitude,$longitude&q=$encoded"
            else "https://maps.apple.com/?q=$encoded"
        )
    }

    val app = UIApplication.sharedApplication
    for (candidate in candidates) {
        val url = NSURL.URLWithString(candidate) ?: continue
        if (app.canOpenURL(url)) {
            app.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
            return
        }
    }
}