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

        // ───────── Planning your trip ─────────
        Faq(
            "How do I add travel legs and places to a trip?",
            "Open a trip and tap the + button to add a travel leg (a journey between two places, like a train or flight) or a place to visit. Each item is grouped by day, and tapping a leg marks it as done.",
        ),
        Faq(
            "How do I add travellers to a trip?",
            "Open a trip and tap the people icon in the top bar. You can add each traveller's name and details. You're added automatically as the trip owner. Travellers are used for splitting expenses.",
        ),
        Faq(
            "Can I add trip legs to my calendar?",
            "Yes. From a trip's journey legs you can add them to your device calendar, so your travel times appear alongside your other events.",
        ),
        Faq(
            "How do I get reminders before a leg?",
            "In Settings, then Notifications, choose how far ahead you'd like to be reminded. Itinera will notify you before each upcoming leg's departure. You'll need to allow notifications when asked.",
        ),
        Faq(
            "How do I archive or delete a trip?",
            "On the home screen, swipe a trip card to reveal options to pin, edit, archive, or delete it. Archived trips can be restored from Settings, then Archived Trips.",
        ),

        // ───────── Documents ─────────
        Faq(
            "Where are my travel documents stored?",
            "Open a trip and tap the documents icon to upload tickets and reservations. They're stored securely in the cloud and can be viewed in-app or shared.",
        ),
        Faq(
            "How do I open a leg's ticket quickly?",
            "When you upload a document you can attach it to a specific leg. After that, a ticket icon appears on that leg in the trip view — tap it to open the document directly, without searching the documents list.",
        ),

        // ───────── Money ─────────
        Faq(
            "How does expense splitting work?",
            "In the Split tab, open a trip and add expenses. Choose who paid and split equally or with custom amounts. Itinera then shows the simplest way to settle up, plus a per-person breakdown of who owes whom.",
        ),
        Faq(
            "How do I settle up and mark payments as paid?",
            "On a trip's expenses, the Settle up card shows the fewest payments needed to clear all debts. When you pay someone back, tap Mark paid on that line to record it. The trip owner can also tap Settle up trip to mark everything as settled.",
        ),
        Faq(
            "How do I change a trip's currency?",
            "Open a trip's expenses and tap the currency selector at the top. Each trip uses one currency, which you can set before adding your first expense.",
        ),
        Faq(
            "How do I convert between currencies?",
            "Use the Currency tab for live exchange rates. Enter an amount in either currency and the other updates automatically, with a recent rate history chart.",
        ),

        // ───────── Sharing ─────────
        Faq(
            "How do I share a trip with someone else?",
            "Open a trip and tap the members icon, then create an invite code to share. The person enters that code in their own Itinera account to join. You can set each member as an editor or viewer, and remove members at any time.",
        ),
        Faq(
            "What's the difference between a traveller and a member?",
            "A traveller is a name used for splitting expenses — they don't need the app. A member is another Itinera user you've shared the trip with, who can view or edit it on their own device.",
        ),

        // ───────── Travel tools ─────────
        Faq(
            "Can I translate text while travelling?",
            "Yes. Open Translate from Settings, pick your languages, and type to translate as you go. The first time you use a language it downloads once, after which translation works offline. Recent translations are saved, and you can star ones to keep.",
        ),
        Faq(
            "How do the Weather and World Clock tools work?",
            "From Settings, open Weather to save cities and see their current conditions and a 5-day forecast, or World Clock to track the time in places around the world alongside your own. Swipe a card to remove it.",
        ),
        Faq(
            "What is the Emergency feature?",
            "Open Emergency from Settings to see local emergency numbers (police, ambulance, fire) for your region, or pick another country. Tap a number to dial it. Always confirm the correct local number when abroad.",
        ),
        Faq(
            "Is there a compass?",
            "Yes. Open Compass from Settings for a live heading that points to magnetic north. On iPhone it asks for location permission, which Apple requires for the compass to work. Move your device in a figure-8 if it needs calibrating.",
        ),

        // ───────── Your data ─────────
        Faq(
            "Does my data sync across devices?",
            "Yes. Your trips, expenses, and documents are tied to your account, so signing in on another device loads everything. New changes appear after you sign in or reopen the trip.",
        ),
        Faq(
            "Can I use more than one account?",
            "Yes. In Settings, then Account, you can switch between accounts or add another. Each account keeps its own trips and data.",
        ),
        Faq(
            "Can I export or share a trip?",
            "Yes. Go to Settings, then Export Trips. Pick the trips you want and share them as a text summary or a PDF itinerary using your device's share sheet.",
        ),
        Faq(
            "Which features work offline?",
            "World Clock, Emergency numbers, and Compass work fully offline. Translation works offline once a language has been downloaded. Weather and currency rates, opening documents, and signing in need a connection.",
        ),
    )
}