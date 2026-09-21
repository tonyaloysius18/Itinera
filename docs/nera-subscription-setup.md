# Nera subscription: setup guide

Everything in the app and the server is built and tested. What is left is **your accounts**: creating the two store
products, connecting RevenueCat, and pasting a few keys. Do the steps in order; each one says what you get out of it.

**The plan:** €4.99 a month, auto-renewing, cancel any time, 7-day free trial (handled by Nera's server, no card
needed), 150 messages a month fair-use cap, friends and family free by email.

## Names to use (they must match exactly)

| What | Value |
|---|---|
| iOS bundle ID | `dev.ynotlabs.itinera` |
| Android package | `com.itinera.app` |
| Subscription group (Apple) | `Nera` |
| Apple product ID | `dev.ynotlabs.itinera.nera.monthly` |
| Google subscription ID / base plan ID | `nera_monthly` / `monthly` |
| RevenueCat entitlement | `nera` |
| RevenueCat offering | `default` (mark it as **Current**), with the **Monthly** package |
| Price | €4.99 / month |

Product IDs cannot be changed after creation, so double-check them.

---

## Step 1: Apple (App Store Connect)

1. **Agreements first.** App Store Connect, Business, **Agreements, Tax, and Banking**. The **Paid Apps Agreement**
   must be *Active*, with a bank account and tax forms completed. Nothing else works until it is. This can take a
   few days, so start here.
2. Also apply to the **App Store Small Business Program** (Apple's commission drops to 15%).
3. Open **Itinera**, then **Monetization, Subscriptions**, then **Create** a subscription group named **Nera**.
4. Inside the group, **Create** a subscription:
   - Reference name: `Nera Monthly`. Product ID: `dev.ynotlabs.itinera.nera.monthly`.
   - **Subscription duration:** 1 month.
   - **Subscription prices:** add a price, choose the base country (for example Ireland or Germany), pick
     **€4.99**. Apple converts it for other countries; review the list.
   - **Localization:** display name `Nera Plus`, description `Your AI travel planner`. Add other languages later.
   - Do **not** add an Apple introductory offer. The free trial is handled by Nera's server.
   - **Review information:** a screenshot of the paywall (open the trial banner in the chat) and a short note.
5. **Offer codes** (optional, for gifting): the subscription, **Offer Codes**, create an offer (for example free for
   1 month) and generate codes. Friends and family are easier by email; see the end of this guide.
6. **Key for RevenueCat:** Users and Access, **Integrations**, **In-App Purchase**, generate a key. Download the
   `.p8` file once, and note the **Key ID** and **Issuer ID**.
7. **Sandbox testers:** Users and Access, **Sandbox**, add a tester (an email you control, not your real Apple ID).
8. The **first** subscription must be attached to an app version when you submit it (the version page has an
   *In-App Purchases and Subscriptions* section).

## Step 2: Google Play Console

1. Setup, **Payments profile**: make sure a merchant profile is linked.
2. Upload a build that includes the billing permission (the current build does: it is in `1.1.0 (20)`) to at least the
   **Internal testing** track. Subscriptions cannot be created before a build with billing exists.
3. **Monetize with Play, Products, Subscriptions, Create subscription:**
   - Product ID: `nera_monthly`. Name: `Nera Plus`.
   - Add a **base plan**: ID `monthly`, **auto-renewing**, billing period **1 month**, price **€4.99** (use "set
     prices" and let Google convert the rest). **Activate** it.
4. **License testing:** Setup, **License testing**, add the emails of your test accounts.
5. **Access for RevenueCat:** in Google Cloud (a project of your choice, no billing needed) create a **service
   account** and a **JSON key**; enable the **Google Play Android Developer API**. In Play Console, Users and
   permissions, **Invite** that service account and give it: *View app information*, *View financial data*, *Manage
   orders and subscriptions*. Permissions can take up to a day to apply.

## Step 3: RevenueCat

1. Create an account and a project named **Itinera**.
2. **Add apps:** iOS (bundle `dev.ynotlabs.itinera`, upload the `.p8`, Key ID, Issuer ID) and Android (package
   `com.itinera.app`, upload the service account JSON).
3. **Products:** add the two store products above.
4. **Entitlement:** create `nera` and attach both products.
5. **Offering:** create `default`, add a **Monthly** package with both products, and make it the **Current** offering.
6. **Keys** (Project settings, API keys):
   - Public SDK keys (`appl_...` and `goog_...`): put them in the repo's `local.properties` (never committed):
     ```
     revenueCatIosKey=appl_xxxxxxxx
     revenueCatAndroidKey=goog_xxxxxxxx
     ```
   - The **Secret API key** (`sk_...`): store it on the server (paste when asked; do not put it in a file):
     ```
     cd worker && npx wrangler secret put REVENUECAT_SECRET_KEY
     ```
     This lets the app unlock a user the instant they buy, without waiting for a webhook.
7. **Webhook** (Integrations, Webhooks): URL `https://nera.itinera-nera-worker.workers.dev/revenuecat`. Make a long
   random string (`openssl rand -hex 32`), store it with `npx wrangler secret put REVENUECAT_WEBHOOK_AUTH`, and paste
   the **same string** into RevenueCat's "Authorization header value". This handles renewals, cancellations and refunds.

The app identifies each user to RevenueCat by their **Firebase uid**, which is how the server links a purchase to an
account. Nothing to configure for that.

## Step 4: Turn it on and test

1. Rebuild the app (the keys are baked in at build time).
2. Test with a sandbox tester (Apple) or a license tester (Google): open Nera, tap the trial banner, subscribe.
   The paywall should close with "Nera is unlocked".
3. Test **Restore purchases**, and cancelling from the store's subscription settings.
4. When it all works, enforce the trial: in `worker/wrangler.toml` set `PAYWALL_ENABLED = "true"` and run
   `npx wrangler deploy`. **Do not do this before step 3 works**, or expired users would have no way to pay.
5. Watch real cost with the query in `worker/README.md` (table `nera_cost`).

## Friends and family (free, no store involved)

```
cd worker
npm run friend -- add mum@example.com "Mum"
npm run friend -- list
```

They sign in with that email (Google sign-in always counts as verified) and Nera is unlocked, with the same
150-a-month fair-use cap. Apple's "Hide My Email" addresses will not match.

## What the paywall must keep (both stores check this)

The price and period, "renews automatically / cancel any time", links to Terms and Privacy, and a **Restore
purchases** button. All of these are already in the app's paywall in 42 languages.

## Before you ship

- [ ] Paid Apps Agreement active; bank and tax done
- [ ] Both products created and *approved / active*
- [ ] RevenueCat entitlement `nera`, offering `default` current
- [ ] Keys in `local.properties`; `REVENUECAT_SECRET_KEY` and `REVENUECAT_WEBHOOK_AUTH` stored on the server
- [ ] Sandbox purchase, restore and cancel tested on iOS and Android
- [ ] Privacy policy on the website matches the repo's `docs/privacy-policy.html` (merge `stage` to `main`)
- [ ] Then, and only then, `PAYWALL_ENABLED = "true"`
