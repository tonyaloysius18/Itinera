// Nera on Cloudflare Workers. Same behaviour as functions/index.js (Firebase), no Google billing needed:
//   POST /  { messages: [{role, content}], currentItinerary? }   Authorization: Bearer <Firebase ID token>
//   -> { type: "say" | "itinerary", message, quickReplies?, itinerary? }
// Prompt, tools and response shaping are shared with the Firebase version via ../../functions.

import { MODEL, buildSystem, cleanMessages, runAgent } from "../../functions/nera.js";
import { DATA_TOOLS, getWeather, searchPlaces } from "../../functions/tools.js";
import { verifyFirebaseToken } from "./firebaseAuth.js";

const DAILY_LIMIT = 40;            // Nera requests per user per UTC day
const MAX_BODY_BYTES = 100_000;
const ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";

const json = (body, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { "content-type": "application/json" } });

/** Count this request against the user's daily quota; false when over the limit. Atomic in one statement. */
async function consumeQuota(db, uid, day) {
  const row = await db
    .prepare(
      "INSERT INTO nera_usage (uid, day, count) VALUES (?1, ?2, 1) " +
      "ON CONFLICT(uid, day) DO UPDATE SET count = count + 1 WHERE count < ?3 RETURNING count")
    .bind(uid, day, DAILY_LIMIT)
    .first();
  return row !== null;
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
    body: JSON.stringify({ model: MODEL, max_tokens: 6000, system, tools, tool_choice: { type: "any" }, messages }),
  });
  if (!res.ok) throw new UpstreamError(`Anthropic ${res.status}: ${await res.text()}`);
  return res.json();
}

export default {
  async fetch(request, env, ctx) {
    if (request.method !== "POST") return json({ error: "method_not_allowed" }, 405);

    // 1. Who is calling? (Firebase login)
    let uid;
    try {
      const token = (request.headers.get("Authorization") || "").replace(/^Bearer /, "");
      uid = await verifyFirebaseToken(token, { projectId: env.FIREBASE_PROJECT_ID, jwksUrl: env.JWKS_URL || undefined });
    } catch {
      return json({ error: "unauthenticated" }, 401);
    }

    // 2. Validate input
    const raw = await request.text();
    if (raw.length > MAX_BODY_BYTES) return json({ error: "too_large" }, 413);
    let body;
    try { body = JSON.parse(raw); } catch { return json({ error: "bad_json" }, 400); }
    const messages = cleanMessages(body?.messages);
    if (!messages) return json({ error: "bad_messages" }, 400);

    // 3. Rate limit
    const today = new Date().toISOString().slice(0, 10);
    if (!(await consumeQuota(env.DB, uid, today))) {
      return json({ error: "quota", message: "You've reached today's limit for Nera. Please try again tomorrow." }, 429);
    }
    if (Math.random() < 0.01) { // housekeeping: drop counters older than 3 days
      const cutoff = new Date(Date.now() - 3 * 86400000).toISOString().slice(0, 10);
      ctx.waitUntil(env.DB.prepare("DELETE FROM nera_usage WHERE day < ?1").bind(cutoff).run());
    }

    // 4. Run Nera
    try {
      const places = Boolean(env.GOOGLE_PLACES_API_KEY && env.GOOGLE_PLACES_API_KEY.trim());
      const system = buildSystem(today, body?.currentItinerary, { places });
      const dataTools = places ? DATA_TOOLS : DATA_TOOLS.filter((t) => t.name !== "search_places");
      const cache = placesCache(env.DB);
      const runTool = async (name, input) => {
        if (name === "get_weather") return getWeather(input);
        if (name === "search_places") return searchPlaces(input, { apiKey: env.GOOGLE_PLACES_API_KEY, cache });
        return { error: `Unknown tool ${name}` };
      };
      const reply = await runAgent({ callModel: (args) => callModel(env, system, args), runTool, messages, dataTools });
      return json(reply);
    } catch (e) {
      console.error("nera failed", e);
      return json({ error: e instanceof UpstreamError ? "upstream" : "internal" }, e instanceof UpstreamError ? 502 : 500);
    }
  },
};
