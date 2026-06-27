package com.itinera.app.data

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator as MlTranslator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android translation via Google ML Kit. On-device, free, offline after a
 * one-time model download per language. Clients are cached per language pair.
 */
actual class Translator actual constructor() {

    private val clients = mutableMapOf<String, MlTranslator>()

    private fun clientFor(sourceLang: String, targetLang: String): MlTranslator {
        val key = "$sourceLang>$targetLang"
        return clients.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.fromLanguageTag(sourceLang) ?: TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLang) ?: TranslateLanguage.ENGLISH)
                .build()
            Translation.getClient(options)
        }
    }

    actual suspend fun prepare(sourceLang: String, targetLang: String) {
        val client = clientFor(sourceLang, targetLang)
        // allow download over any network; first run needs connectivity
        val conditions = DownloadConditions.Builder().build()
        suspendCancellableCoroutine<Unit> { cont ->
            client.downloadModelIfNeeded(conditions)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    actual suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
        if (text.isBlank()) return ""
        val client = clientFor(sourceLang, targetLang)
        // ensure model present
        prepare(sourceLang, targetLang)
        return suspendCancellableCoroutine { cont ->
            client.translate(text)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }
}