package com.itinera.app.model

/**
 * The text rules for listing an itinerary in the Community feed. Mirrors worker/src/moderation.js, so a listing that
 * passes here is not later rejected for its text; keep the two word lists in step. The app checks before publishing
 * (so the person gets a message straight away), the server checks again when it reviews the listing.
 */
object CommunityRules {
    private val blockedWords = listOf(
        "fuck", "fucking", "shit", "bitch", "cunt", "asshole", "dick", "pussy", "nigger", "nigga", "faggot", "whore", "slut",
        "porn", "porno", "xxx", "sex", "escort", "nude", "nudes", "onlyfans", "casino", "viagra", "cialis",
        "bitcoin", "crypto", "forex", "loan", "nazi", "hitler", "kill yourself", "rape", "terrorist",
    )
    private val urlPattern = Regex("(https?://|www\\.|\\b[a-z0-9-]+\\.(com|net|org|io|ru|xyz|top|click|link|shop|biz)\\b)", RegexOption.IGNORE_CASE)
    private val contactPattern = Regex("(\\b\\d{3}[\\s.-]?\\d{3,4}[\\s.-]?\\d{4}\\b|@[a-z0-9_.]{3,}|\\bwhats ?app\\b|\\btelegram\\b|\\bt\\.me\\b)", RegexOption.IGNORE_CASE)
    private val lookAlikes = mapOf('0' to 'o', '1' to 'i', '3' to 'e', '4' to 'a', '5' to 's', '$' to 's', '@' to 'a', '!' to 'i')

    private fun hasBlockedWord(text: String): Boolean {
        val normalized = text.lowercase().map { lookAlikes[it] ?: it }.joinToString("")
            .replace(Regex("[^a-z\\s]"), " ").replace(Regex("\\s+"), " ")
        val padded = " $normalized "
        return blockedWords.any { padded.contains(" $it ") }
    }

    /** True when [text] is acceptable in a public listing: no blocked words, links or contact details. */
    fun isAcceptable(text: String): Boolean =
        text.isBlank() || !(hasBlockedWord(text) || urlPattern.containsMatchIn(text) || contactPattern.containsMatchIn(text))
}
