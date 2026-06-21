package com.itinera.app.ui.screens

/**
 * Help / FAQ content, kept separate from the main i18n Strings file.
 *
 * NOTE: this is English-only — it does NOT go through the app's translation
 * system. If you later want the FAQ localized, move these into Strings instead.
 * Edit the wording here freely.
 */
object HelpContent {

    const val intro = "Answers to common questions about using Itinera."

    data class Faq(val question: String, val answer: String)

    val faqs: List<Faq> = listOf(
        Faq(
            "How do I add travellers to a trip?",
            "Open a trip and tap the people icon in the top bar. You can add each traveller's name and details. You're added automatically as the trip owner.",
        ),
        Faq(
            "How does expense splitting work?",
            "In the Split tab, open a trip and add expenses. Choose who paid and split equally or with custom amounts. Itinera then shows the simplest way to settle up, plus a per-person breakdown of who owes whom.",
        ),
        Faq(
            "How do I change a trip's currency?",
            "Open a trip's expenses and tap the currency selector at the top. Each trip uses one currency, which you can set before adding your first expense.",
        ),
        Faq(
            "Where are my travel documents stored?",
            "Open a trip and tap the documents icon to upload tickets and reservations. They're stored securely in the cloud and can be viewed in-app or shared.",
        ),
        Faq(
            "Can I export or share a trip?",
            "Yes. Go to Settings, then Export Trips. Pick the trips you want and share them as a text summary or a PDF itinerary using your device's share sheet.",
        ),
        Faq(
            "Does my data sync across devices?",
            "Yes. Your trips, expenses, and documents are tied to your account, so signing in on another device loads everything. New changes appear after you sign in or reopen the trip.",
        ),
        Faq(
            "Can I add trip legs to my calendar?",
            "Yes. From a trip's journey legs you can add them to your device calendar, so your travel times appear alongside your other events.",
        ),
        Faq(
            "How do I archive or delete a trip?",
            "On the home screen, swipe a trip card to reveal options to pin, edit, archive, or delete it. Archived trips can be restored from Settings, then Archived Trips.",
        ),
    )
}