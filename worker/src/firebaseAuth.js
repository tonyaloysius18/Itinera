// Verifies a Firebase Auth ID token (RS256 JWT) without the Admin SDK, using WebCrypto.
// https://firebase.google.com/docs/auth/admin/verify-id-tokens#verify_id_tokens_using_a_third-party_jwt_library

const GOOGLE_JWKS = "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";
const KEY_TTL_MS = 6 * 60 * 60 * 1000;   // how long fetched signing keys are trusted
const REFETCH_GAP_MS = 60 * 1000;        // don't refetch keys more than once a minute
const SKEW_S = 60;                       // allowed clock skew

let keyCache = { keys: new Map(), fetchedAt: 0 };

const b64urlToBytes = (s) => {
  const b64 = s.replace(/-/g, "+").replace(/_/g, "/") + "=".repeat((4 - (s.length % 4)) % 4);
  return Uint8Array.from(atob(b64), (c) => c.charCodeAt(0));
};
const parseJson = (part) => JSON.parse(new TextDecoder().decode(b64urlToBytes(part)));

async function loadKeys(jwksUrl, fetchImpl, now) {
  const res = await fetchImpl(jwksUrl);
  if (!res.ok) throw new Error("jwks_unavailable");
  const { keys } = await res.json();
  const map = new Map();
  for (const jwk of keys || []) {
    if (jwk.kid && jwk.kty === "RSA") {
      map.set(jwk.kid, await crypto.subtle.importKey(
        "jwk", jwk, { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" }, false, ["verify"]));
    }
  }
  keyCache = { keys: map, fetchedAt: now };
}

async function keyFor(kid, jwksUrl, fetchImpl, now) {
  const fresh = now - keyCache.fetchedAt < KEY_TTL_MS;
  if (fresh && keyCache.keys.has(kid)) return keyCache.keys.get(kid);
  // Unknown kid or stale cache: refetch, but rate-limited so junk tokens can't hammer Google.
  if (now - keyCache.fetchedAt >= REFETCH_GAP_MS) await loadKeys(jwksUrl, fetchImpl, now);
  return keyCache.keys.get(kid) || null;
}

/** Returns the Firebase uid, or throws. Any failure means "reject the request". */
export async function verifyFirebaseToken(token, { projectId, jwksUrl = GOOGLE_JWKS, fetchImpl = fetch, now = Date.now() }) {
  if (!projectId) throw new Error("no_project");
  const parts = typeof token === "string" ? token.split(".") : [];
  if (parts.length !== 3) throw new Error("malformed");

  const header = parseJson(parts[0]);
  if (header.alg !== "RS256" || !header.kid) throw new Error("bad_header");

  const key = await keyFor(header.kid, jwksUrl, fetchImpl, now);
  if (!key) throw new Error("unknown_key");
  const valid = await crypto.subtle.verify(
    "RSASSA-PKCS1-v1_5", key, b64urlToBytes(parts[2]), new TextEncoder().encode(`${parts[0]}.${parts[1]}`));
  if (!valid) throw new Error("bad_signature");

  const p = parseJson(parts[1]);
  const nowS = Math.floor(now / 1000);
  if (p.aud !== projectId) throw new Error("bad_audience");
  if (p.iss !== `https://securetoken.google.com/${projectId}`) throw new Error("bad_issuer");
  if (typeof p.exp !== "number" || p.exp <= nowS) throw new Error("expired");
  if (typeof p.iat !== "number" || p.iat > nowS + SKEW_S) throw new Error("bad_iat");
  if (typeof p.auth_time === "number" && p.auth_time > nowS + SKEW_S) throw new Error("bad_auth_time");
  if (typeof p.sub !== "string" || !p.sub || p.sub.length > 128) throw new Error("bad_subject");
  return p.sub;
}

/** Test hook: forget cached signing keys. */
export function _resetKeyCache() {
  keyCache = { keys: new Map(), fetchedAt: 0 };
}
