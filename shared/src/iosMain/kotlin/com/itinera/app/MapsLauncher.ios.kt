package com.itinera.app

import platform.Foundation.NSCharacterSet
import platform.Foundation.NSURL
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIApplication

actual fun openInMaps(label: String, latitude: Double?, longitude: Double?) {
    val encoded = (label as platform.Foundation.NSString)
        .stringByAddingPercentEncodingWithAllowedCharacters(
            NSCharacterSet.URLQueryAllowedCharacterSet
        ) ?: return

    // maps.apple.com opens Apple Maps directly rather than Safari, and doesn't
    // need LSApplicationQueriesSchemes in Info.plist the way maps:// would.
    val urlString = if (latitude != null && longitude != null) {
        "https://maps.apple.com/?ll=$latitude,$longitude&q=$encoded"
    } else {
        "https://maps.apple.com/?q=$encoded"
    }

    val url = NSURL.URLWithString(urlString) ?: return
    val app = UIApplication.sharedApplication
    if (app.canOpenURL(url)) {
        app.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}