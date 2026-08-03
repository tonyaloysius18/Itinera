package com.itinera.app

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.currentLocale
import platform.Foundation.localeWithLocaleIdentifier

actual fun deviceCountryName(): String {
    val code = NSLocale.currentLocale.objectForKey(NSLocaleCountryCode) as? String ?: return ""
    // en_US as the display locale, so we get "France" rather than a localised
    // name — languageForCountry() matches on English keys.
    val english = NSLocale.localeWithLocaleIdentifier("en_US")
    return english.displayNameForKey(NSLocaleCountryCode, code) ?: ""
}