# Nera Plus: setup guide (one-time purchase)

Everything in the app and the server is built and tested. What is left is **your accounts**: the two store
products, RevenueCat, and a few keys. Do the steps in order; each says what you get out of it.

**The plan:** the first 3 trips created with Nera are free (counted by Nera's server, no card needed). After that,
Nera needs **Nera Plus**: a single **€19.99** purchase, for life (for as long as Nera is offered). Not a
subscription, nothing renews. Fair use: 150 messages a month, 40 a day. Friends and family are free by email.

## Names to use (they must match exactly)

| What | Value |
|---|---|
| iOS bundle ID | `dev.ynotlabs.itinera` |
| Android package | `com.itinera.app` |
| Apple product type | **In-App Purchase, Non-Consumable** |
| Apple product ID | `dev.ynotlabs.itinera.nera.lifetime` |
| Google product type | **One-time product** |
| Google product ID | `nera_lifetime` |
| RevenueCat entitlement | `nera` |
| RevenueCat offering / package | `default` (Current) / **Lifetime** (`$rc_lifetime`) |
| Price | €19.99 |

Product IDs cannot be changed or reused after deletion, so double-check them.

The old monthly products (`dev.ynotlabs.itinera.nera.monthly`, `nera_monthly`) are no longer used. They were never
released, so they can be deleted (Apple: while the subscription is unapproved) or simply left unused.

---

## Step 1: Apple (App Store Connect)

1. **Agreements first.** Business, **Agreements, Tax, and Banking**: the Paid Apps Agreement must be *Active*.
2. Open **Itinera**, **Monetization, In-App Purchases**, **Create**:
   - Type: **Non-Consumable**. Reference name: `Nera Plus Lifetime`. Product ID: `dev.ynotlabs.itinera.nera.lifetime`.
   - **Price:** choose a base country (Ireland or Germany), pick **€19.99**, and review the other countries. Apple
     converts by tax and exchange rate; you can override any country.
   - **Localization:** display name `Nera Plus`, description `Your AI travel planner, unlocked for life`.
   - **Family Sharing:** optional; leave off.
   - **Review information:** a screenshot of the paywall (tap the free-trips banner in the Nera chat) and a note on how
     to reach it.
3. **Key for RevenueCat** (done once): Users and Access, **Integrations**, **In-App Purchase**.
4. The **first** in-app purchase must be attached to an app version when you submit it (the version page has an
   *In-App Purchases* section).
5. **Testing:** a TestFlight build makes sandbox purchases automatically. Use your normal Apple ID; nothing is charged.
   (The iOS simulator's sandbox sign-in is unreliable.)

## Step 2: Google Play Console

1. Setup, **Payments profile**: a merchant profile must be linked.
2. Upload a build with the billing permission to at least **Internal testing**.
3. **Monetize with Play, Products, One-time products, Create one-time product:**
   - Product ID: `nera_lifetime`. Name: `Nera Plus`. Price: **€19.99** (let Google convert the rest). **Activate** it.
4. **License testing:** Setup, **License testing**, add your test accounts.
5. **Access for RevenueCat** (done once): a service account with *View app information*, *View financial data*,
   *Manage orders and subscriptions*. Permissions can take up to about 36 hours to apply.

## Step 3: RevenueCat

1. **Products:** add `dev.ynotlabs.itinera.nera.lifetime` (App Store, type non-consumable / lifetime) and
   `nera_lifetime` (Play Store, one-time). Also add a Test Store lifetime product for quick testing.
2. **Entitlement `nera`:** attach the new products.
3. **Offering `default`:** replace the monthly package with a **Lifetime** package (`$rc_lifetime`) holding the three
   products, and keep the offering **Current**. The app only looks at the lifetime package.
4. **Keys** (already set up): public SDK keys in `local.properties` (`revenueCatIosKey`, `revenueCatAndroidKey`),
   the secret key and webhook secret stored on the server with `wrangler secret put`.
5. **Webhook:** `https://nera.itinera-nera-worker.workers.dev/revenuecat`. A one-time purchase arrives as
   `NON_RENEWING_PURCHASE` with no expiry date; the server stores that as lifetime access. A refund revokes it.

The app identifies each user to RevenueCat by their **Firebase uid**. Nothing to configure for that.

## Step 4: Test

1. Rebuild the app (keys are baked in at build time).
2. Open Nera, tap the free-trips banner, tap **Unlock for life**. The paywall closes with "Nera is unlocked".
3. Test **Restore purchases** (delete and reinstall, or use another device).
4. Watch real cost with the query in `worker/README.md` (table `nera_cost`).

## Friends and family (free, no store involved)

```
cd worker
npm run friend -- add mum@example.com "Mum"
npm run friend -- list
```

They sign in with that email (Google sign-in always counts as verified) and Nera is unlocked, with the same
fair-use caps. Apple's "Hide My Email" addresses will not match.

## What the paywall must keep (both stores check this)

The price, that it is a one-time purchase, links to Terms and Privacy, and a **Restore purchases** button. All are in
the app's paywall in 42 languages. There is no "renews automatically" text and no "Manage subscription" link, because
neither applies to a one-time purchase.

## Before you ship

- [ ] Paid Apps Agreement active; bank and tax done
- [ ] Both products created and *approved / active*
- [ ] RevenueCat entitlement `nera`, offering `default` current with the Lifetime package
- [ ] Sandbox purchase, restore and refund tested on iOS (TestFlight) and Android
- [ ] Privacy policy and Terms on the website match the repo's `docs/` (merge `stage` to `main`)
- [ ] App Privacy (Apple) and Data safety (Google) answers include Nera, RevenueCat and Cloudflare
