# Itinera — App Privacy (Apple) & Data Safety (Google) draft answers

Drafted from a full read of the codebase (Firebase Auth/Firestore/Storage, the Nera Cloudflare
Worker, RevenueCat, Google Places, Google/Apple Sign-In, Open-Meteo, Unsplash). Review this
before entering it into App Store Connect / Play Console — I did not submit anything, this is a
draft for you to check and correct.

## Full data inventory (source of truth for both forms)

| Data | Where it lives | Why collected |
|---|---|---|
| Name, surname | Firestore `users/{uid}` (`UserProfile`) | Profile, greeting Nera by name, trip member display |
| Email address | Firebase Auth + Firestore profile | Sign-in, account identification, friends & family allowlist |
| Phone number (`mobile`) | Firestore profile | Optional profile field, shown to trip members |
| Date of birth (`dob`) | Firestore profile | Optional profile field |
| Street, city, postal code | Firestore profile | Optional profile field; `city` is also used as Nera's "home city" for travel legs |
| Profile photo | Firebase Storage + Firestore `photoUrl` | Avatar shown to self and trip members |
| Trip content (itinerary, activities, legs, dates, notes) | Firestore `trips/{tripId}` | Core app function |
| Travel documents (tickets, boarding passes, etc., user-uploaded) | Firebase Storage + Firestore `trips/{tripId}/documents` | Core app function (document wallet) — **can include passport/ID photos if the user chooses to upload them**, since the picker doesn't restrict file content |
| Expenses & debt-splitting records | Firestore `trips/{tripId}/expenses`, `payments` | Core app function — amounts and who-owes-whom, not real payment processing |
| Checklist items | Firestore `checklists` | Core app function |
| Nera AI chat messages | Firestore `trips/{tripId}/neraMessages` **and** sent to Anthropic's Claude API via our Cloudflare Worker | AI itinerary planning |
| Home city, destination cities/countries | Firestore | Itinerary content, sent to Nera as context |
| Precise device location (heading only, not coordinates) | Device sensor via `CLLocationManager`/Android `SensorManager`, **not transmitted or stored** | In-app compass |
| Purchase record (Nera Plus lifetime, product id, price) | RevenueCat, keyed by Firebase UID | One-time unlock entitlement |
| Firebase UID | Firebase Auth, used as the RevenueCat `app_user_id` and Firestore doc keys | Account identity |
| Reports/blocks (who reported/blocked whom) | Firestore `reports` | User safety feature |

**Third parties data is sent to:**
- **Anthropic** (Claude API) — the content of Nera chat messages (destination, dates, traveller counts, preferences), to generate itineraries. No account identity is sent, just the conversation.
- **Google** — Sign-In identity token exchange; Places API location/text search queries (no account identity attached).
- **RevenueCat** — Firebase UID (as `app_user_id`) + purchase event data.
- **Firebase / Google Cloud** (Auth, Firestore, Storage) and **Cloudflare** (Workers, D1) — infrastructure/hosting for the above; treated as service providers, not independent third-party use.
- **Open-Meteo** — destination + travel dates only (no identity), for weather.
- **Unsplash** — destination search text only (no identity), for trip cover photos.

**Not collected:** contacts, precise GPS coordinates, browsing/search history outside the app, advertising identifiers, analytics/crash SDKs (none integrated), payment card/bank details (RevenueCat/Apple/Google handle the actual transaction; the app never sees card data).

**Account & data deletion:** supported in-app (Settings → Account → Delete Account), which deletes the Firebase Auth account, the Firestore profile, and (per `TripService.deleteTripCascade`) a trip's documents/expenses/activities/payments/neraMessages when the owner deletes their account.

---

## Apple: App Privacy ("Nutrition Label") — draft

For each category below: **Collected**, and whether it's **linked to identity** (yes, via Firebase UID/email for all of these) and used for **tracking** (no — nothing here is used to track users across other companies' apps/websites for advertising).

| Category | Types | Used for |
|---|---|---|
| **Contact Info** | Name, Email Address, Phone Number, Physical Address | App Functionality, Product Personalization |
| **User Content** | Photos or Videos (profile photo, postcard photos, uploaded documents), Other User Content (itineraries, notes, chat messages, checklists) | App Functionality |
| **Identifiers** | User ID | App Functionality |
| **Purchases** | Purchase History | App Functionality |
| **Location** | *(none — see note)* | — |
| **Financial Info** | *(none — no card/bank data collected by the app itself)* | — |
| **Diagnostics** | *(none — no crash/analytics SDK)* | — |
| **Other Data** | Date of birth | App Functionality |

**Note on Location:** technically no `CLLocationManager` *coordinates* are read or stored — only a compass heading. Apple's questionnaire may still expect you to declare **Location (Coarse)** as "collected" simply because the app requests location authorization at all (for the compass). If in doubt, the safer answer is to declare **Coarse Location → App Functionality, not linked to identity** rather than omit it, since the OS-level permission prompt exists.

**Data used to track you:** No.

---

## Google Play: Data Safety — draft

**Does your app collect or share any of the required user data types?** Yes.

**Is all user data encrypted in transit?** Yes (HTTPS/Firebase/Cloudflare throughout).

**Do you provide a way for users to request data deletion?** Yes (in-app account deletion).

| Category | Type | Collected? | Shared? | Purpose |
|---|---|---|---|---|
| Personal info | Name | Yes | No | App functionality, Account management |
| Personal info | Email address | Yes | No | App functionality, Account management |
| Personal info | Phone number | Yes | No | App functionality |
| Personal info | Address | Yes | No | App functionality |
| Personal info | Other info (date of birth) | Yes | No | App functionality |
| Photos and videos | Photos | Yes | No | App functionality |
| Files and docs | Files and docs | Yes | No | App functionality (travel document wallet) |
| Financial info | Purchase history | Yes | Yes (RevenueCat) | App functionality |
| App activity | Other user-generated content (itineraries, Nera chat) | Yes | Yes (Anthropic, Nera chat content only) | App functionality |
| App info and performance | *(none — no crash/analytics SDK)* | No | — | — |
| Location | *(none collected/transmitted — compass heading only, on-device)* | No* | — | — |

*\* Same caveat as the Apple section: Google's form asks about permissions requested, not just data transmitted. Compass uses `ACCESS_FINE_LOCATION`-adjacent iOS location auth but Android's implementation uses raw sensors with no location permission at all — so this is genuinely iOS-only and arguably doesn't need declaring on the Android side. On iOS's own form (above) I'd lean toward declaring it to be safe.*

---

## What I did NOT do
I have not entered any of this into App Store Connect or Play Console — both forms have legal weight (Apple can reject a build over a mismatched label; Google can suspend the app over an inaccurate Data Safety form), so I've drafted it for your review rather than clicking submit myself. Once you confirm this looks right (or correct anything I got wrong about what's collected), I can walk through entering it in both consoles with you, or you can paste this straight in.
