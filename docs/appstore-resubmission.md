# Itinera iOS 1.0 — Resubmission Package

Rejection: **Guideline 2.1 – Information Needed** (new-account info request). Because the app has
trip sharing/collaboration (user-generated content shared between users), **Guideline 1.2 (UGC safety)**
also applies. This package covers both.

---

## 0. Pre-submission tasks (do these first)

1. **Add the Firestore rule for reports** (Firebase Console → Firestore → Rules). Verified missing —
   report writes currently return `PERMISSION_DENIED`. Merge into the existing ruleset (do not replace it):
   ```
   match /reports/{reportId} {
     allow create: if request.auth != null;
     allow read, update, delete: if false;
   }
   ```
   (Block works without this — it writes to the user's own profile. Only Report needs it.)

2. **Publish the Terms page.** `TERMS_URL` in `CreateAccountScreen.kt` points to
   `https://tonyaloysius18.github.io/Itinera/terms.html` (same GitHub Pages site as the privacy policy).
   GitHub Pages serves `main` → `/docs`, so `docs/terms.html` must be committed to **main** and pushed
   for the page to go live.

3. **Create a demo account** in the app and put its credentials in
   App Store Connect → your app → **App Review Information → Notes** (see reply below).

4. **Build & upload a new iOS build** from Xcode as usual (Product → Archive). The KMP framework
   compiles fine in Xcode; the only earlier failure was an external headless-build sandbox, not the project.

---

## 1. Reply to paste into the Resolution Center (and App Review → Notes)

> Hello, and thank you for reviewing Itinera. Below is all the requested information. We have also added
> it to the App Review Information → Notes field for future reference.
>
> **App purpose and target audience**
> Itinera is a personal travel-planning and trip-organizer app for individual leisure and business
> travellers. It keeps everything for a trip in one place: day-by-day itineraries (legs of travel and
> places to visit), tickets and travel documents, expenses, a live currency converter, weather, and a
> pre-departure packing checklist. The problem it solves is fragmentation — bookings, documents, budgets
> and plans normally scattered across emails and apps — by consolidating them into one itinerary.
> Available in 40+ languages. Free, with no in-app purchases.
>
> **Setting up and accessing the main features**
> On first launch the user creates an account or signs in (email/password, Google, or Apple). Then:
> create a trip (+ button) with a name, destination and dates; plan the itinerary by adding legs and
> activities (grouped automatically by date); attach tickets/documents and expenses; use the currency
> converter and weather from within a trip; add packing-checklist items. Profile & Settings allow editing
> the profile, changing language/currency, viewing archived trips, and deleting the account.
>
> Demo account (please use this to access all features):
> - Email: `[DEMO_EMAIL]`
> - Password: `[DEMO_PASSWORD]`
>
> **User-generated content — reporting and blocking**
> Trips can optionally be shared with people the user invites (via an invite code), so members can
> collaborate. Members can moderate each other: open a shared trip → the **members/travellers** list →
> the **⋮ menu** on any other member → **Report Member** (reason + details) or **Block** (their
> activities, documents and expenses are hidden from you on shared trips; reversible via Unblock).
> Users accept a zero-tolerance policy for objectionable content and abusive behavior at sign-up
> (see the Terms link on the Create Account screen). We review reports and act within 24 hours,
> removing offending content and ejecting violators where warranted. Abuse contact: ynotlabs.dev@gmail.com.
>
> **Account deletion**
> Available in Settings → Account → Delete Account, which permanently removes the account and its data.
>
> **External services used for core functionality**
> - Firebase Authentication (email/password, Google, Apple) and Cloud Firestore (trip data).
> - Cloudinary — image hosting for user-uploaded photos and document scans.
> - Unsplash — optional destination cover photos.
> - Frankfurter API & ExchangeRate-API — currency exchange rates.
> - Open-Meteo (forecast/archive/geocoding) — weather and location lookup.
> - Photon (Komoot) — place-name search.
> - OpenFreeMap + MapLibre — map tiles/rendering; Apple Maps / Google Maps launched externally.
> - Google ML Kit (on-device) — text translation and barcode/QR scanning for tickets.
> No payment processors; no in-app purchases.
>
> **Regional differences**
> The app functions consistently across all regions. The only variation is localization (UI language,
> defaulting to the device language with an English fallback) and the user's chosen default currency.
>
> **Regulated industry / protected material**
> None. All third-party data is accessed through the public APIs listed above under their standard terms.
>
> Please let us know if any further information would help. Thank you.

Replace `[DEMO_EMAIL]` / `[DEMO_PASSWORD]` with the demo account you create in step 0.3.

---

## 2. Screen-recording shot list (physical device, latest iOS)

Record one continuous take, starting from app launch:

1. Launch the app → sign-in screen (show the **Terms / zero-tolerance line** on Create Account).
2. Sign in (or register a new account, showing the account-creation flow).
3. Create a trip; add a leg and a place (activity).
4. Open the members list → create an **invite code** (owner side).
5. (If you can, on a second device/account) join with the code to show collaboration.
6. In the members/travellers list, open the **⋮ menu** → show **Report Member** (reason picker) and
   **Block / Unblock**.
7. Settings → Account → **Delete Account** (show the deletion flow).

---

## 3. Status of the code changes (already implemented & verified)

- Report/Block/Unblock on members — in **both** the owner-reached Members screen and the
  **Travellers** screen that non-owner members use (so any member can moderate).
- Blocking hides the blocked user's activities/documents/expenses (author-stamped; legacy items unaffected).
- Zero-tolerance agreement + Terms link on Create Account; `docs/terms.html` added.
- Verified live on Android (two accounts, invite-code join): report dialog works; block hides the
  owner's activity and unblock restores it. iOS + Android both compile.
