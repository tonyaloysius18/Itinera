package com.itinera.app.data

/**
 * On-device text translation.
 *
 * Android: Google ML Kit Translate (free, offline after a one-time model
 * download per language). iOS: Apple's Translation framework (iOS 17.4+) —
 * implemented via a small Swift helper (see iosApp).
 *
 * Language codes are ISO-639-1 (e.g. "en", "fr", "es"); both backends accept these.
 */
expect class Translator() {
    /**
     * Translate [text] from [sourceLang] to [targetLang]. Downloads the needed
     * model on first use (requires network that once). Returns the translated
     * string, or throws on failure.
     */
    suspend fun translate(text: String, sourceLang: String, targetLang: String): String

    /** Download/prepare models for a language pair ahead of time. Safe to call repeatedly. */
    suspend fun prepare(sourceLang: String, targetLang: String)
}

/** A language offered in the translator UI. */
data class TranslateLang(val code: String, val name: String)

/**
 * Languages supported by BOTH ML Kit and Apple Translation (intersection, common ones).
 * Kept deliberately to widely-supported codes so behaviour matches across platforms.
 */
val translateLanguages: List<TranslateLang> = listOf(
    TranslateLang("en", "English"),
    TranslateLang("fr", "French"),
    TranslateLang("es", "Spanish"),
    TranslateLang("de", "German"),
    TranslateLang("it", "Italian"),
    TranslateLang("pt", "Portuguese"),
    TranslateLang("nl", "Dutch"),
    TranslateLang("pl", "Polish"),
    TranslateLang("ru", "Russian"),
    TranslateLang("uk", "Ukrainian"),
    TranslateLang("tr", "Turkish"),
    TranslateLang("ar", "Arabic"),
    TranslateLang("hi", "Hindi"),
    TranslateLang("zh", "Chinese"),
    TranslateLang("ja", "Japanese"),
    TranslateLang("ko", "Korean"),
    TranslateLang("th", "Thai"),
    TranslateLang("vi", "Vietnamese"),
    TranslateLang("id", "Indonesian"),
    TranslateLang("el", "Greek"),
    TranslateLang("cs", "Czech"),
    TranslateLang("ro", "Romanian"),
    TranslateLang("sv", "Swedish"),
    TranslateLang("da", "Danish"),
    TranslateLang("fi", "Finnish"),
    TranslateLang("no", "Norwegian"),
)