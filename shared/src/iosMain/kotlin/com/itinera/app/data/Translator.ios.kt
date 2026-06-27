package com.itinera.app.data

/**
 * iOS translation scaffold.
 *
 * Apple's Translation framework (iOS 17.4+) has no clean imperative API callable
 * from Kotlin/Native — it's SwiftUI-oriented. The plan is to add a small Swift
 * helper in iosApp that wraps TranslationSession and expose it here. Until that
 * helper is wired, this returns a clear message rather than failing silently.
 *
 * NEXT STEP: implement a Swift `TranslatorHelper` and call it from these actuals.
 */
actual class Translator actual constructor() {

    actual suspend fun prepare(sourceLang: String, targetLang: String) {
        // no-op until the Swift helper is wired
    }

    actual suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
        return "[iOS translation coming soon]"
    }
}