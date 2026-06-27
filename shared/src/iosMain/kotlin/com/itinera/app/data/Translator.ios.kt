package com.itinera.app.data

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS translation via a Swift provider that wraps Apple's Translation framework
 * (iOS 17.4+). Mirrors the IosGoogleSignIn injection pattern: Swift sets
 * `IosTranslator.provider` at startup; this actual calls through it.
 */
actual class Translator actual constructor() {

    actual suspend fun prepare(sourceLang: String, targetLang: String) {
        // Apple downloads/prepares models as part of translate(); nothing to pre-warm here.
    }

    actual suspend fun translate(text: String, sourceLang: String, targetLang: String): String =
        suspendCoroutine { cont ->
            val provider = IosTranslator.provider
            if (provider == null) {
                cont.resume("[Translation unavailable]")
            } else {
                provider(text, sourceLang, targetLang) { translated ->
                    cont.resume(translated ?: "[Couldn't translate]")
                }
            }
        }
}

/**
 * Bridge object. Swift assigns [provider] at app startup. The lambda receives
 * (text, sourceLang, targetLang, onResult); Swift calls onResult with the
 * translated string (or null on failure).
 */
object IosTranslator {
    var provider: ((
        text: String,
        sourceLang: String,
        targetLang: String,
        onResult: (String?) -> Unit,
    ) -> Unit)? = null
}