// Minimal Firestore access for the Worker with a service account, using only fetch + WebCrypto (no Admin SDK).
// Used for moderation: the only code besides the moderator CLI allowed to set status / feed / approvedAt.

const SCOPE = "https://www.googleapis.com/auth/datastore";
const TOKEN_URL = "https://oauth2.googleapis.com/token";
let cached = { token: "", exp: 0 };

const b64url = (bytes) => btoa(String.fromCharCode(...new Uint8Array(bytes))).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
const encode = (obj) => b64url(new TextEncoder().encode(JSON.stringify(obj)));

async function importKey(pem) {
  const body = pem.replace(/-----[A-Z ]+-----/g, "").replace(/\s+/g, "");
  const der = Uint8Array.from(atob(body), (c) => c.charCodeAt(0));
  return crypto.subtle.importKey("pkcs8", der, { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" }, false, ["sign"]);
}

/** OAuth access token for the service account (cached until shortly before it expires). */
export async function accessToken(sa, { fetchImpl = fetch, now = Date.now() } = {}) {
  if (cached.token && cached.exp - 60_000 > now) return cached.token;
  const iat = Math.floor(now / 1000);
  const unsigned = `${encode({ alg: "RS256", typ: "JWT" })}.${encode({ iss: sa.client_email, scope: SCOPE, aud: TOKEN_URL, iat, exp: iat + 3600 })}`;
  const sig = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", await importKey(sa.private_key), new TextEncoder().encode(unsigned));
  const res = await fetchImpl(TOKEN_URL, {
    method: "POST",
    headers: { "content-type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({ grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer", assertion: `${unsigned}.${b64url(sig)}` }),
  });
  if (!res.ok) throw new Error(`token_${res.status}`);
  const { access_token, expires_in } = await res.json();
  cached = { token: access_token, exp: now + expires_in * 1000 };
  return access_token;
}

/** Firestore REST value -> plain JS value. */
export function decodeValue(v) {
  if (v === undefined || v === null) return undefined;
  if ("stringValue" in v) return v.stringValue;
  if ("integerValue" in v) return Number(v.integerValue);
  if ("doubleValue" in v) return v.doubleValue;
  if ("booleanValue" in v) return v.booleanValue;
  if ("nullValue" in v) return null;
  if ("arrayValue" in v) return (v.arrayValue.values || []).map(decodeValue);
  if ("mapValue" in v) return decodeFields(v.mapValue.fields || {});
  return undefined;
}
export const decodeFields = (fields) => Object.fromEntries(Object.entries(fields).map(([k, v]) => [k, decodeValue(v)]));

const base = (projectId) => `https://firestore.googleapis.com/v1/projects/${projectId}/databases/(default)/documents`;
const auth = async (env) => ({ authorization: `Bearer ${await accessToken(JSON.parse(env.FIREBASE_SERVICE_ACCOUNT))}`, "content-type": "application/json" });

export async function getShared(env, id) {
  const res = await fetch(`${base(env.FIREBASE_PROJECT_ID)}/sharedItineraries/${encodeURIComponent(id)}`, { headers: await auth(env) });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(`get_${res.status}`);
  return decodeFields((await res.json()).fields || {});
}

/** Sets the moderator-controlled fields only; every other field is left exactly as the author wrote it. */
export async function setReview(env, id, { status, feed, approvedAt }) {
  const mask = ["status", "feed", ...(approvedAt !== undefined ? ["approvedAt"] : [])].map((f) => `updateMask.fieldPaths=${f}`).join("&");
  const fields = { status: { stringValue: status }, feed: { booleanValue: feed }, ...(approvedAt !== undefined ? { approvedAt: { integerValue: String(approvedAt) } } : {}) };
  const res = await fetch(`${base(env.FIREBASE_PROJECT_ID)}/sharedItineraries/${encodeURIComponent(id)}?${mask}&currentDocument.exists=true`, {
    method: "PATCH", headers: await auth(env), body: JSON.stringify({ fields }),
  });
  if (!res.ok) throw new Error(`patch_${res.status}`);
}

async function runQuery(env, structuredQuery) {
  const res = await fetch(`${base(env.FIREBASE_PROJECT_ID)}:runQuery`, { method: "POST", headers: await auth(env), body: JSON.stringify({ structuredQuery }) });
  if (!res.ok) throw new Error(`query_${res.status}`);
  return (await res.json()).filter((r) => r.document).map((r) => decodeFields(r.document.fields || {}));
}
const eq = (field, value) => ({ fieldFilter: { field: { fieldPath: field }, op: "EQUAL", value } });
const and = (...filters) => ({ compositeFilter: { op: "AND", filters } });

/** Listing requests nobody has reviewed yet (oldest first, bounded). */
export const pendingListings = (env, limit = 50) => runQuery(env, {
  from: [{ collectionId: "sharedItineraries" }],
  where: and(eq("listRequested", { booleanValue: true }), eq("status", { stringValue: "" })),
  limit,
});

/** Open reports (all kinds; the caller keeps the ones about shared itineraries). */
export const openReports = (env, limit = 500) => runQuery(env, {
  from: [{ collectionId: "reports" }],
  where: eq("status", { stringValue: "open" }),
  limit,
});
