// Nera on Cloudflare Workers. Same behaviour as functions/index.js (Firebase), no Google billing needed:
//   POST /  { messages: [{role, content}], currentItinerary? }   Authorization: Bearer <Firebase ID token>
//   -> { type: "say" | "itinerary", message, quickReplies?, itinerary? }
// Prompt, tools and response shaping are shared with the Firebase version via ../../functions.

import { FINAL_TOOLS, MODEL, buildSystem, cleanMessages, runAgent, withLimitReminder } from "../../functions/nera.js";
import { DATA_TOOLS, getWeather, searchPlaces } from "../../functions/tools.js";
import { verifyFirebaseClaims } from "./firebaseAuth.js";
import { getEntitlement, isFriend, recordTrip } from "./entitlement.js";
import { applyUpdate, eventToUpdate, fetchSubscriberExpiry, safeEqual } from "./revenuecat.js";

const DEFAULT_DAILY_LIMIT = 80;    // Nera requests per user per UTC day, for everyone except OWNER_UIDS
const DEFAULT_PAID_MONTHLY_LIMIT = 150;   // fair-use cap per paying/friend user per UTC month
const DEFAULT_FREE_MONTHLY_LIMIT = 60;    // messages per month for users on the free trips (keeps cost bounded)
const DEFAULT_FREE_TRIPS = 3;             // trips created with Nera before the subscription is needed
const MAX_TRIP_ID = 128;
const positiveInt = (v, fallback) => (Number(v) > 0 ? Math.floor(Number(v)) : fallback);
const entitlementBody = (e) => ({ status: e.status, tripsLeft: e.tripsLeft, tripsUsed: e.tripsUsed, freeTrips: e.freeTrips, source: e.source });
const MAX_BODY_BYTES = 100_000;
const ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";

/** Firebase uids with no daily cap at all (still subject to the monthly fair-use cap). Comma-separated env var. */
const isOwnerUid = (env, uid) => (env.OWNER_UIDS || "").split(",").map((s) => s.trim()).filter(Boolean).includes(uid);

const json = (body, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { "content-type": "application/json" } });

/** Count this request against the user's daily quota; false when over the limit. Atomic in one statement. */
async function consumeQuota(db, uid, day, limit) {
  const row = await db
    .prepare(
      "INSERT INTO nera_usage (uid, day, count) VALUES (?1, ?2, 1) " +
      "ON CONFLICT(uid, day) DO UPDATE SET count = count + 1 WHERE count < ?3 RETURNING count")
    .bind(uid, day, limit)
    .first();
  return row !== null;
}

/** Monthly fair-use counter (paying users). Atomic; false when the month's cap is used up. */
async function consumeMonthly(db, uid, month, limit) {
  const row = await db
    .prepare(
      "INSERT INTO nera_usage_month (uid, month, count) VALUES (?1, ?2, 1) " +
      "ON CONFLICT(uid, month) DO UPDATE SET count = count + 1 WHERE count < ?3 RETURNING count")
    .bind(uid, month, limit)
    .first();
  return row !== null;
}

/** Give back one monthly request when the same request is then refused by the daily limit. */
async function refundMonthly(db, uid, month) {
  await db.prepare("UPDATE nera_usage_month SET count = count - 1 WHERE uid = ?1 AND month = ?2 AND count > 0").bind(uid, month).run();
}

/** Aggregate real token usage per day so the price can be checked against reality. */
function recordCost(db, day, usage) {
  return db
    .prepare(
      "INSERT INTO nera_cost (day, requests, model_calls, input_tokens, output_tokens) VALUES (?1, 1, ?2, ?3, ?4) " +
      "ON CONFLICT(day) DO UPDATE SET requests = requests + 1, model_calls = model_calls + ?2, " +
      "input_tokens = input_tokens + ?3, output_tokens = output_tokens + ?4")
    .bind(day, usage.calls, usage.input, usage.output)
    .run();
}

/** POST /revenuecat: subscription events from RevenueCat. Authenticated by a shared secret, not by Firebase. */
async function handleRevenueCat(request, env) {
  const expected = env.REVENUECAT_WEBHOOK_AUTH;
  if (!expected) return json({ error: "webhook_not_configured" }, 503);
  if (!safeEqual(request.headers.get("Authorization") || "", expected)) return json({ error: "unauthorized" }, 401);
  let payload;
  try { payload = JSON.parse(await request.text()); } catch { return json({ error: "bad_json" }, 400); }
  const update = eventToUpdate(payload?.event);
  if (!update) return json({ ok: true, ignored: true });          // always 200 so RevenueCat does not retry
  const written = await applyUpdate(env.DB, update);
  return json({ ok: true, applied: written });
}

const placesCache = (db) => ({
  async get(key) {
    const row = await db.prepare("SELECT value FROM nera_cache WHERE key = ?1 AND expires_at > ?2")
      .bind(key, Date.now()).first();
    return row ? JSON.parse(row.value) : null;
  },
  async set(key, value, ttlMs) {
    await db.prepare("INSERT OR REPLACE INTO nera_cache (key, value, expires_at) VALUES (?1, ?2, ?3)")
      .bind(key, JSON.stringify(value), Date.now() + ttlMs).run();
  },
});

class UpstreamError extends Error {}

async function callModel(env, system, { tools, messages }) {
  const res = await fetch(env.ANTHROPIC_URL || ANTHROPIC_URL, {
    method: "POST",
    headers: {
      "content-type": "application/json",
      "x-api-key": env.ANTHROPIC_API_KEY,
      "anthropic-version": "2023-06-01",
    },
    body: JSON.stringify({ model: MODEL, max_tokens: 8000, system, tools, tool_choice: { type: "any" }, messages }),
  });
  if (!res.ok) throw new UpstreamError(`Anthropic ${res.status}: ${await res.text()}`);
  return res.json();
}

export default {
  async fetch(request, env, ctx) {
    if (request.method !== "POST") return json({ error: "method_not_allowed" }, 405);
    if (new URL(request.url).pathname === "/revenuecat") return handleRevenueCat(request, env);

    // 1. Who is calling? (Firebase login)
    let uid, email = "", emailVerified = false;
    try {
      const token = (request.headers.get("Authorization") || "").replace(/^Bearer /, "");
      ({ uid, email, emailVerified } = await verifyFirebaseClaims(token, {
        projectId: env.FIREBASE_PROJECT_ID, jwksUrl: env.JWKS_URL || undefined }));
    } catch {
      return json({ error: "unauthenticated" }, 401);
    }

    const path = new URL(request.url).pathname;
    const freeTrips = positiveInt(env.FREE_TRIPS, DEFAULT_FREE_TRIPS);

    // POST /entitlement: where does this user stand? No model call and no quota used.
    if (path === "/entitlement") {
      // { refresh: true } (sent by the app right after a purchase or restore) checks RevenueCat directly, so the user is
      // unlocked immediately instead of waiting for the webhook.
      let refresh = false;
      try { refresh = JSON.parse((await request.text()) || "{}").refresh === true; } catch { /* no body */ }
      if (refresh && env.REVENUECAT_SECRET_KEY) {
        const expiry = await fetchSubscriberExpiry(uid, env.REVENUECAT_SECRET_KEY, { baseUrl: env.REVENUECAT_API_URL || undefined });
        if (expiry) await applyUpdate(env.DB, { uid, paidUntil: expiry, eventAt: Date.now() });
      }
      const friend = await isFriend(env.DB, email, emailVerified);
      const e = await getEntitlement(env.DB, uid, Date.now(), freeTrips, { friend });
      return json({ enforced: env.PAYWALL_ENABLED === "true", entitlement: entitlementBody(e) });
    }

    // POST /trip { tripId }: the app tells us the user approved a Nera draft and it became a trip. Counted once per id.
    if (path === "/trip") {
      let tripId = "";
      try { tripId = String(JSON.parse((await request.text()) || "{}").tripId ?? ""); } catch { /* fall through */ }
      if (!tripId || tripId.length > MAX_TRIP_ID) return json({ error: "bad_trip" }, 400);
      await recordTrip(env.DB, uid, tripId, Date.now());
      const friend = await isFriend(env.DB, email, emailVerified);
      const e = await getEntitlement(env.DB, uid, Date.now(), freeTrips, { friend });
      return json({ enforced: env.PAYWALL_ENABLED === "true", entitlement: entitlementBody(e) });
    }

    // 2. Validate input
    const raw = await request.text();
    if (raw.length > MAX_BODY_BYTES) return json({ error: "too_large" }, 413);
    let body;
    try { body = JSON.parse(raw); } catch { return json({ error: "bad_json" }, 400); }
    const messages = cleanMessages(body?.messages);
    if (!messages) return json({ error: "bad_messages" }, 400);
    const homeCity = typeof body?.homeCity === "string" ? body.homeCity.trim().slice(0, 80) : "";

    // 3. Where does the user stand? Free trips are always counted; they are only enforced when PAYWALL_ENABLED="true"
    //    (keep it off until a way to subscribe exists, or users past their free trips would have no way to pay).
    const paywall = env.PAYWALL_ENABLED === "true";
    const friend = await isFriend(env.DB, email, emailVerified);
    const entitlement = await getEntitlement(env.DB, uid, Date.now(), freeTrips, { friend });
    const limited = paywall && entitlement.status === "limit";   // free trips used up: chat still works, new trips do not

    // 4. Rate limits: a daily cap for everyone except OWNER_UIDS (unlimited), plus a monthly cap
    //    (higher for paying and friend users) that still applies even to owners.
    const today = new Date().toISOString().slice(0, 10);
    const month = today.slice(0, 7);
    const owner = isOwnerUid(env, uid);
    const monthlyLimit = entitlement.status === "paid"
      ? positiveInt(env.PAID_MONTHLY_LIMIT, DEFAULT_PAID_MONTHLY_LIMIT)
      : positiveInt(env.FREE_MONTHLY_LIMIT, DEFAULT_FREE_MONTHLY_LIMIT);
    if (!(await consumeMonthly(env.DB, uid, month, monthlyLimit))) {
      return json({ error: "monthly_quota" }, 429);
    }
    const dailyLimit = positiveInt(env.DAILY_LIMIT, DEFAULT_DAILY_LIMIT);
    if (!owner && !(await consumeQuota(env.DB, uid, today, dailyLimit))) {
      await refundMonthly(env.DB, uid, month);
      return json({ error: "quota", message: "You've reached today's limit for Nera. Please try again tomorrow." }, 429);
    }
    if (Math.random() < 0.01) { // housekeeping: drop counters older than 3 days
      const cutoff = new Date(Date.now() - 3 * 86400000).toISOString().slice(0, 10);
      ctx.waitUntil(env.DB.prepare("DELETE FROM nera_usage WHERE day < ?1").bind(cutoff).run());
      ctx.waitUntil(env.DB.prepare("DELETE FROM nera_usage_month WHERE month < ?1").bind(cutoff.slice(0, 7)).run());
    }

    // 5. Run Nera
    try {
      const places = Boolean(env.GOOGLE_PLACES_API_KEY && env.GOOGLE_PLACES_API_KEY.trim());
      const system = buildSystem(today, body?.currentItinerary, { places, limited, homeCity });
      const dataTools = places ? DATA_TOOLS : DATA_TOOLS.filter((t) => t.name !== "search_places");
      const cache = placesCache(env.DB);
      const runTool = async (name, input) => {
        if (name === "get_weather") return getWeather(input);
        if (name === "search_places") return searchPlaces(input, { apiKey: env.GOOGLE_PLACES_API_KEY, cache });
        return { error: `Unknown tool ${name}` };
      };
      const usage = { calls: 0, input: 0, output: 0 };
      const onUsage = (u) => { usage.calls++; usage.input += u.input_tokens || 0; usage.output += u.output_tokens || 0; };
      // Free trips used up: Nera may chat but not draft, so only the "say" tool is offered.
      const finalTools = limited ? FINAL_TOOLS.filter((t) => t.name === "say") : FINAL_TOOLS;
      const reply = await runAgent({ callModel: (args) => callModel(env, system, args), runTool, messages: limited ? withLimitReminder(messages) : messages, dataTools, finalTools, onUsage });
      ctx.waitUntil(recordCost(env.DB, today, usage));
      // Tell the app where the user stands (only when enforcing), so it can show "free trips left".
      return json(paywall ? { ...reply, entitlement: entitlementBody(entitlement) } : reply);
    } catch (e) {
      console.error("nera failed", e);
      return json({ error: e instanceof UpstreamError ? "upstream" : "internal" }, e instanceof UpstreamError ? 502 : 500);
    }
  },
};
