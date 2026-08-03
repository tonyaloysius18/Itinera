package com.itinera.app

import java.util.Locale

actual fun deviceCountryName(): String {
    val country = Locale.getDefault().country
    if (country.isBlank()) return ""
    // Ask for the English name specifically — languageForCountry() matches on
    // English keys, and displayCountry alone would return "Frankreich" on a
    // German-language phone.
    return Locale("", country).getDisplayCountry(Locale.ENGLISH)
}