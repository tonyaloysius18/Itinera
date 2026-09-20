import test from "node:test";
import assert from "node:assert/strict";
import { verifyFirebaseToken, _resetKeyCache } from "../src/firebaseAuth.js";

const PROJECT = "itinera-test";
const b64url = (buf) => Buffer.from(buf).toString("base64url");

const pair = await crypto.subtle.generateKey(
  { name: "RSASSA-PKCS1-v1_5", modulusLength: 2048, publicExponent: new Uint8Array([1, 0, 1]), hash: "SHA-256" },
  true, ["sign", "verify"]);
const other = await crypto.subtle.generateKey(
  { name: "RSASSA-PKCS1-v1_5", modulusLength: 2048, publicExponent: new Uint8Array([1, 0, 1]), hash: "SHA-256" },
  true, ["sign", "verify"]);
const jwk = { ...(await crypto.subtle.exportKey("jwk", pair.publicKey)), kid: "k1", alg: "RS256", use: "sig" };

let jwksFetches = 0;
const fetchImpl = async () => { jwksFetches++; return { ok: true, json: async () => ({ keys: [jwk] }) }; };

const NOW = Date.UTC(2026, 8, 21, 12, 0, 0);
const nowS = Math.floor(NOW / 1000);
const good = { aud: PROJECT, iss: `https://securetoken.google.com/${PROJECT}`, sub: "user123", iat: nowS - 60, exp: nowS + 3000, auth_time: nowS - 60 };

async function sign(payload, { key = pair.privateKey, header = { alg: "RS256", kid: "k1", typ: "JWT" } } = {}) {
  const h = b64url(JSON.stringify(header)), p = b64url(JSON.stringify(payload));
  const sig = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key, new TextEncoder().encode(`${h}.${p}`));
  return `${h}.${p}.${b64url(sig)}`;
}
const verify = (token) => verifyFirebaseToken(token, { projectId: PROJECT, jwksUrl: "http://jwks", fetchImpl, now: NOW });
const rejects = async (token, msg) => { _resetKeyCache(); await assert.rejects(verify(token), new RegExp(msg)); };

test("accepts a valid token and returns the uid", async () => {
  _resetKeyCache();
  assert.equal(await verify(await sign(good)), "user123");
});

test("caches signing keys between calls", async () => {
  _resetKeyCache(); jwksFetches = 0;
  await verify(await sign(good)); await verify(await sign(good));
  assert.equal(jwksFetches, 1);
});

test("rejects a token signed by a different key", async () => rejects(await sign(good, { key: other.privateKey }), "bad_signature"));
test("rejects a tampered payload", async () => {
  const [h, , s] = (await sign(good)).split(".");
  await rejects(`${h}.${b64url(JSON.stringify({ ...good, sub: "admin" }))}.${s}`, "bad_signature");
});
test("rejects wrong audience", async () => rejects(await sign({ ...good, aud: "other-project" }), "bad_audience"));
test("rejects wrong issuer", async () => rejects(await sign({ ...good, iss: "https://evil.example" }), "bad_issuer"));
test("rejects an expired token", async () => rejects(await sign({ ...good, exp: nowS - 1 }), "expired"));
test("rejects a token issued in the future", async () => rejects(await sign({ ...good, iat: nowS + 3600 }), "bad_iat"));
test("rejects empty subject", async () => rejects(await sign({ ...good, sub: "" }), "bad_subject"));
test("rejects alg none / HS256", async () => {
  await rejects(await sign(good, { header: { alg: "none", kid: "k1" } }), "bad_header");
  await rejects(await sign(good, { header: { alg: "HS256", kid: "k1" } }), "bad_header");
});
test("rejects unknown key id", async () => rejects(await sign(good, { header: { alg: "RS256", kid: "nope" } }), "unknown_key"));
test("rejects garbage", async () => {
  for (const t of ["", "a.b", "x.y.z", undefined, null]) {
    _resetKeyCache();
    await assert.rejects(verify(t));
  }
});
