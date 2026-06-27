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
    val url = NSURL(string = "tel://$number")
    UIApplication.sharedApplication.openURL(url)
}