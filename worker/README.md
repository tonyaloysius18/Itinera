# Nera on Cloudflare Workers

Free-tier host for Nera's server (no Google billing needed). It reuses the prompt, tools and response
shaping in `../functions/` (`nera.js`, `tools.js`), and adds Firebase login verification (`src/firebaseAuth.js`)
and a D1 database for the per-user daily limit and place-search cache.

## Deploy (one time)

1. Create a free Cloudflare account (no card needed) at https://dash.cloudflare.com/sign-up
2. `cd worker && npm install && npx wrangler login`
3. `npx wrangler d1 create nera` — copy the printed `database_id` into `wrangler.toml`
4. `npx wrangler d1 migrations apply nera --remote`
5. `npx wrangler secret put ANTHROPIC_API_KEY` (paste your key when prompted)
6. Optional, enables ratings/prices/map pins: `npx wrangler secret put GOOGLE_PLACES_API_KEY`
7. `npx wrangler deploy` — prints `https://nera.<your-subdomain>.workers.dev`
8. Put that URL in the repo-root `local.properties`: `neraEndpoint=https://nera.<your-subdomain>.workers.dev`

## Test

- `npm test` — Firebase token verification (12 cases, real RSA keys).
- Full local run: `npx wrangler d1 migrations apply nera --local`, then `npx wrangler dev`. `.dev.vars`
  (gitignored) can point `JWKS_URL` / `ANTHROPIC_URL` at local mocks; never set those in production.
  After changing `database_id`, run `npx wrangler d1 migrations apply nera --local` again: local state is keyed by it.

## Free trips and paid unlock

- Every user can create **`FREE_TRIPS` trips with Nera for free** (default 3, in `wrangler.toml`). A trip counts when the
  user approves a draft: the app calls `POST /trip { tripId }`, and each `(uid, tripId)` is stored once in `nera_trips`,
  so a retry never uses up a second trip. There is no time limit.
- After the last free trip the user is at the **limit**: chat keeps working (weather, food, questions), but Nera is only
  offered the `say` tool, so she cannot draft or change itineraries, and the app asks them to unlock Nera Plus before approving
  any older draft. The one-time purchase (or a friends-and-family email) lifts the limit.
- Free users get `FREE_MONTHLY_LIMIT` requests a month (default 60) so people who never buy cannot run up cost;
  paying and friend users get `PAID_MONTHLY_LIMIT` (default 150). The counter is per calendar month, whatever the tier.
- Trips are counted whether or not the paywall is enforced. **Enforcement is `PAYWALL_ENABLED`.** Turn it on only once an
  in-app purchase exists, otherwise users at the limit would have no way to pay.
- `POST /entitlement` (no model call, no quota) returns `{ enforced, entitlement: { status, tripsLeft, tripsUsed, freeTrips } }`
  with `status` one of `free`, `limit`, `paid`. Chat replies carry the same `entitlement` when the paywall is enforced.
- Grant a paid unlock by hand (this is what the purchase webhook writes):
  `npx wrangler d1 execute nera --remote --command "UPDATE nera_entitlements SET paid_until = 9999999999999 WHERE uid = '<firebase uid>'"`
- Reset someone's free trips: `DELETE FROM nera_trips WHERE uid = '<firebase uid>'`.
- **Apply migrations before deploying** a version that needs a new table: `npx wrangler d1 migrations apply nera --remote`,
  then `npx wrangler deploy`.

## Friends & family (free access by email)

Emails on this list get Nera free, with no trip limit and no purchase. Only a **verified** email counts (Google sign-in
always is), so nobody can sign up with someone else's address.

```
npm run friend -- add mum@example.com "Mum"
npm run friend -- remove mum@example.com
npm run friend -- list
```

(Add `--local` to use the test database.) Friends still have the monthly fair-use cap below. Apple's "Hide My Email"
addresses will not match, so ask friends to use Google sign-in or their real email.

## Nera Plus purchase (RevenueCat)

RevenueCat tells the Worker about purchases at `POST /revenuecat`. Setup:

1. Choose a long random string and store it: `openssl rand -hex 32`, then
   `npx wrangler secret put REVENUECAT_WEBHOOK_AUTH` and paste it.
2. In RevenueCat, Project settings, Integrations, Webhooks, add
   `https://nera.<your-subdomain>.workers.dev/revenuecat` and put the **same string** in "Authorization header value".
3. The app must log in to RevenueCat with the **Firebase uid** as the app user id (it does), and the entitlement
   must be named `nera`.

Right after a purchase or restore the app also calls `POST /entitlement {"refresh": true}`, which makes the Worker ask
RevenueCat directly (needs `npx wrangler secret put REVENUECAT_SECRET_KEY`, the RevenueCat **secret** API key), so the
user is unlocked immediately even if the webhook is slow. The step-by-step store and RevenueCat setup is in
`docs/nera-purchase-setup.md`.

Events set `paid_until`. The Nera Plus purchase is one-time: RevenueCat sends `NON_RENEWING_PURCHASE` with no expiry date,
which the Worker stores as lifetime access (`paid_until` = 9999999999999). A refund revokes access immediately, and an
older event can never overwrite a newer one. Events that do carry an expiry (renewals, cancellations) still work, which
keeps the RevenueCat Test Store's subscription-style products usable for testing. Until step 1 is done the endpoint answers 503.

## Fair use and cost

- Everyone: `DAILY_LIMIT` (40) requests a day. Paying and friend users also get `PAID_MONTHLY_LIMIT` (150) a month
  (`wrangler.toml`); over it the app shows "monthly limit reached".
- Real token usage per day is recorded in `nera_cost`. Estimated spend (Haiku 4.5 at $1 / $5 per million tokens,
  check current pricing):

```
npx wrangler d1 execute nera --remote --command "SELECT day, requests, model_calls, input_tokens, output_tokens, ROUND(input_tokens/1e6*1.0 + output_tokens/1e6*5.0, 3) AS est_usd FROM nera_cost ORDER BY day DESC LIMIT 14"
```

## Notes

- Model is set in `../functions/nera.js` (`MODEL`). Daily limit is `DAILY_LIMIT` in `src/index.js`.
- Firebase project id is `FIREBASE_PROJECT_ID` in `wrangler.toml`; tokens for other projects are rejected.
- Workers Free allows 100k requests/day and 10 ms CPU per request (time spent waiting on Anthropic doesn't count).
