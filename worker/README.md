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

## Notes

- Model is set in `../functions/nera.js` (`MODEL`). Daily limit is `DAILY_LIMIT` in `src/index.js`.
- Firebase project id is `FIREBASE_PROJECT_ID` in `wrangler.toml`; tokens for other projects are rejected.
- Workers Free allows 100k requests/day and 10 ms CPU per request (time spent waiting on Anthropic doesn't count).
