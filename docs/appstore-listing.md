# Itinera — App Store Connect Listing

Draft metadata for App Store submission. Copy-paste into App Store Connect.
Character limits noted per field.

---

## Name (30 chars max)
Itinera

## Subtitle (30 chars max)
Trips, tickets & currency

## Promotional Text (170 chars max — editable anytime without review)
Plan every trip day by day, keep tickets and documents in one place, convert currencies with live rates, and breeze through a pre-departure checklist.

## Keywords (100 chars max, comma-separated, no spaces)
travel,itinerary,trip planner,tickets,currency,checklist,packing,vacation,journey,organizer

## Description (4000 chars max)
Itinera is a clean, fast travel companion that keeps every trip in one place — from the first plan to the final boarding pass.

PLAN DAY BY DAY
Organize each trip into day-numbered legs and activities, grouped automatically by date. See your whole journey at a glance and know exactly what comes next.

TICKETS & DOCUMENTS
Attach booking references and travel documents to each trip, so your confirmations are never buried in your inbox when you need them.

LIVE CURRENCY CONVERSION
Convert between currencies using real-time exchange rates from the European Central Bank. Swap, recalculate, and budget on the go.

PRE-DEPARTURE CHECKLIST
Build a packing and prep checklist with smart, keyword-based category suggestions — so nothing gets left behind.

BEAUTIFUL, YOURS
Full light, dark, and system themes. A polished interface with smooth gestures — pin, edit, archive, or delete trips with a swipe.

40+ LANGUAGES
Itinera speaks your language, with translations across more than 40 languages and an English fallback.

Whether it's a weekend away or a multi-city adventure, Itinera keeps your plans organized, your documents handy, and your budget clear.

## What's New (4000 chars max — for version 1.0)
Welcome to Itinera 1.0! Plan trips day by day, organize tickets and documents, convert currencies with live rates, and stay ready with a smart pre-departure checklist. Available in 40+ languages with full light and dark themes.

## Support URL (required)
https://ynotlabs.dev            <!-- TODO: confirm a real, reachable URL -->

## Marketing URL (optional)
https://ynotlabs.dev

## Privacy Policy URL (required)
https://ynotlabs.dev/privacy    <!-- TODO: must be live before submission -->

---

## App Information

- **Primary Language:** English (U.S.)
- **Bundle ID:** dev.ynotlabs.itinera
- **Primary Category:** Travel
- **Secondary Category (optional):** Productivity
- **Content Rights:** Does your app contain, show, or access third-party content? — Yes (Unsplash imagery, exchange-rate data)

## Age Rating
Expected 4+ (no objectionable content). Complete the questionnaire in App Store Connect — all "None" unless something changes.

---

## Privacy "Nutrition Label" — Data Collection

Based on code audit (Firebase Auth + Google Sign-In, Firestore, Storage, camera).
NO Firebase Analytics / Crashlytics SDK is linked, so DO NOT declare Diagnostics/Usage Data.

| Data type | Collected | Linked to user | Used for tracking | Purpose |
|-----------|-----------|----------------|-------------------|---------|
| Email address | Yes | Yes | No | App Functionality (account) |
| Name | Yes | Yes | No | App Functionality (account) |
| User Content (trips, docs, photos) | Yes | Yes | No | App Functionality |
| User ID (Firebase UID) | Yes | Yes | No | App Functionality |
| Photos (camera) | Only if user takes a photo | No | No | App Functionality (trip postcard) |

Tracking: NO (nothing shared with data brokers / used for cross-app tracking).

---

## Pre-submission checklist
- [ ] Privacy Policy URL live and reachable
- [ ] Support URL live and reachable
- [ ] New Firebase iOS app registered for bundle `dev.ynotlabs.itinera`, fresh GoogleService-Info.plist added
- [ ] Team ID swapped in Xcode project (currently personal C92K6B4N2J)
- [ ] App icon present at all required sizes (no placeholder)
- [ ] Screenshots: 6.9" (iPhone 16 Pro Max) + 6.5" required; iPad if supported
- [ ] Remove junk: iosApp/.../AppIcon.appiconset/LICENSE.htm + LICENSE_files/
- [ ] Archive validates against ynotlabs team
