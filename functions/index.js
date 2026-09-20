const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret, defineString } = require("firebase-functions/params");
const admin = require("firebase-admin");
const { MODEL, buildSystem, cleanMessages, runAgent } = require("./nera");
const { DATA_TOOLS, getWeather, searchPlaces } = require("./tools");

admin.initializeApp();

const ANTHROPIC_API_KEY = defineSecret("ANTHROPIC_API_KEY");
// Optional: leave blank to run without place search. To enable, put GOOGLE_PLACES_API_KEY=<key> in
// functions/.env and redeploy. (A plain env param, not a secret, so deploys never require it.)
const GOOGLE_PLACES_API_KEY = defineString("GOOGLE_PLACES_API_KEY", { default: "" });
const DAILY_LIMIT = 40; // Nera requests per user per UTC day

/** Count this request against the user's daily quota. Returns false when over the limit. */
async function consumeQuota(uid, day) {
  const ref = admin.firestore().collection("nera_usage").doc(`${uid}_${day}`);
  return admin.firestore().runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    const used = snap.exists ? snap.data().count : 0;
    if (used >= DAILY_LIMIT) return false;
    tx.set(ref, { uid, day, count: used + 1 });
    return true;
  });
}

/** Firestore-backed cache for place searches (collection nera_cache; server-only, no client rules). */
const placesCache = {
  async get(key) {
    const snap = await admin.firestore().collection("nera_cache").doc(key).get();
    if (!snap.exists || snap.data().expiresAt < Date.now()) return null;
    return snap.data().value;
  },
  async set(key, value, ttlMs) {
    await admin.firestore().collection("nera_cache").doc(key).set({ value, expiresAt: Date.now() + ttlMs });
  },
};

async function runTool(name, input) {
  if (name === "get_weather") return getWeather(input);
  if (name === "search_places") return searchPlaces(input, { apiKey: GOOGLE_PLACES_API_KEY.value(), cache: placesCache });
  return { error: `Unknown tool ${name}` };
}

async function callModel(system, { tools, messages }) {
  const apiRes = await fetch("https://api.anthropic.com/v1/messages", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      "x-api-key": ANTHROPIC_API_KEY.value(),
      "anthropic-version": "2023-06-01",
    },
    body: JSON.stringify({ model: MODEL, max_tokens: 6000, system, tools, tool_choice: { type: "any" }, messages }),
  });
  if (!apiRes.ok) throw new Error(`Anthropic ${apiRes.status}: ${await apiRes.text()}`);
  return apiRes.json();
}

/**
 * POST /nera  { messages: [{role, content}], currentItinerary?: {...} }
 * Authorization: Bearer <Firebase ID token>
 * -> { type: "say" | "itinerary", message, quickReplies?, itinerary? }
 */
exports.nera = onRequest(
  { secrets: [ANTHROPIC_API_KEY], region: "europe-west1", timeoutSeconds: 180, memory: "256MiB", maxInstances: 20 },
  async (req, res) => {
    if (req.method !== "POST") return void res.status(405).json({ error: "method_not_allowed" });

    const token = (req.get("Authorization") || "").replace(/^Bearer /, "");
    let uid;
    try {
      uid = (await admin.auth().verifyIdToken(token)).uid;
    } catch {
      return void res.status(401).json({ error: "unauthenticated" });
    }

    const messages = cleanMessages(req.body?.messages);
    if (!messages) return void res.status(400).json({ error: "bad_messages" });

    const now = new Date();
    const today = now.toISOString().slice(0, 10);
    if (!(await consumeQuota(uid, today))) {
      return void res.status(429).json({ error: "quota", message: "You've reached today's limit for Nera. Please try again tomorrow." });
    }

    try {
      const places = GOOGLE_PLACES_API_KEY.value().trim() !== "";
      const system = buildSystem(today, req.body?.currentItinerary, { places });
      // Without a Places key, don't offer search_places at all so Nera can't call a dead tool.
      const dataTools = places ? DATA_TOOLS : DATA_TOOLS.filter((t) => t.name !== "search_places");
      res.json(await runAgent({ callModel: (args) => callModel(system, args), runTool, messages, dataTools }));
    } catch (e) {
      console.error("nera failed", e);
      res.status(500).json({ error: "internal" });
    }
  },
);
