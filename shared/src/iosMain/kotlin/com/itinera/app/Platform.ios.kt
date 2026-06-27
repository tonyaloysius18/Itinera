package com.itinera.app

import platform.Foundation.NSLocale
import platform.Foundation.NSURL
import platform.Foundation.currentLocale
import platform.Foundation.countryCode
import platform.Foundation.preferredLanguages
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val isIos = true
}

actual fun deviceLanguageCode(): String =
    (NSLocale.preferredLanguages.firstOrNull() as? String)?.take(2) ?: "en"
actual fun getPlatform(): Platform = IOSPlatform()

actual fun deviceRegion(): String =
    NSLocale.currentLocale.countryCode ?: ""

actual fun dial(number: String) {
    // tel: (not tel://) is the correct phone-URL scheme; strip spaces so the URL parses
    val cleaned = number.filter { !it.isWhitespace() }
    val url = NSURL(string = "tel:$cleaned") ?: return
    val app = UIApplication.sharedApplication
    // modern API (iOS 10+); the bare openURL(url) is deprecated and can silently fail
    app.openURL(url, options = mapOf<Any?, Any?>(), completionHandler = null)
}