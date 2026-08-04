package com.itinera.app

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

private fun launch(urlString: String) {
    val url = NSURL.URLWithString(urlString) ?: return
    val app = UIApplication.sharedApplication
    if (app.canOpenURL(url)) {
        app.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}

actual fun openEmail(email: String) {
    if (email.isBlank()) return
    launch("mailto:$email")
}

actual fun openPhone(phone: String) {
    if (phone.isBlank()) return
    // tel: is blocked on iPad and iPod, hence the canOpenURL guard in launch().
    launch("tel:${phone.filter { it.isDigit() || it == '+' }}")
}