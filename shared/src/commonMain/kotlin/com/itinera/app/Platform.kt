package com.itinera.app

interface Platform {
    val name: String
    val isIos: Boolean
}

expect fun deviceLanguageCode(): String
expect fun getPlatform(): Platform

/** ISO 3166-1 alpha-2 region code of the device (e.g. "FR", "US"). "" if unknown. */
expect fun deviceRegion(): String

/** Open the phone dialer pre-filled with [number]. The user still taps to call. */
expect fun dial(number: String)