import test from "node:test";
import assert from "node:assert/strict";
import { eventToUpdate, expiryFromSubscriber, fetchSubscriberExpiry, LIFETIME, safeEqual } from "../src/revenuecat.js";

const NOW = 1_800_000_000_000, DAY = 86_400_000;
const base = { type: "RENEWAL", app_user_id: "uid1", entitlement_ids: ["nera"], expiration_at_ms: NOW + 30 * DAY, event_timestamp_ms: NOW - 5 };

test("a normal renewal extends access to the expiry", () => {
  assert.deepEqual(eventToUpdate(base, NOW), { uid: "uid1", paidUntil: NOW + 30 * DAY, eventAt: NOW - 5 });
});
test("a refund revokes immediately", () => {
  assert.equal(eventToUpdate({ ...base, type: "CANCELLATION", cancel_reason: "CUSTOMER_SUPPORT" }, NOW).paidUntil, NOW);
});
test("a normal cancellation keeps access until the paid period ends", () => {
  assert.equal(eventToUpdate({ ...base, type: "CANCELLATION", cancel_reason: "UNSUBSCRIBE" }, NOW).paidUntil, NOW + 30 * DAY);
});
test("an expiration in the past ends access", () => {
  assert.equal(eventToUpdate({ ...base, type: "EXPIRATION", expiration_at_ms: NOW - DAY }, NOW).paidUntil, NOW - DAY);
});
test("ignores test events, transfers and anonymous users", () => {
  assert.equal(eventToUpdate({ ...base, type: "TEST" }, NOW), null);
  assert.equal(eventToUpdate({ ...base, type: "TRANSFER" }, NOW), null);
  assert.equal(eventToUpdate({ ...base, app_user_id: "$RCAnonymousID:abc" }, NOW), null);
  assert.equal(eventToUpdate({ ...base, app_user_id: "" }, NOW), null);
});
test("ignores other entitlements; falls back to the product id only when no entitlement is given", () => {
  assert.equal(eventToUpdate({ ...base, entitlement_ids: ["other"] }, NOW), null);
  assert.notEqual(eventToUpdate({ ...base, entitlement_ids: null, product_id: "dev.ynotlabs.itinera.nera.monthly" }, NOW), null);
  assert.equal(eventToUpdate({ ...base, entitlement_ids: null, product_id: "something.else" }, NOW), null);
});
test("ignores events without a usable expiry", () => {
  for (const exp of [undefined, null, 0, -1, "soon", NaN]) assert.equal(eventToUpdate({ ...base, expiration_at_ms: exp }, NOW), null);
});
test("garbage input is safe", () => {
  for (const ev of [null, undefined, 42, "x", []]) assert.equal(eventToUpdate(ev, NOW), null);
});
test("safeEqual compares secrets exactly", () => {
  assert.equal(safeEqual("abc", "abc"), true);
  assert.equal(safeEqual("abc", "abd"), false);
  assert.equal(safeEqual("abc", "abcd"), false);
  assert.equal(safeEqual("abc", undefined), false);
});

test("REST subscriber payload: expiry, lifetime, none, garbage", () => {
  const sub = (e) => ({ subscriber: { entitlements: { nera: e } } });
  assert.equal(expiryFromSubscriber(sub({ expires_date: "2026-10-21T10:00:00Z" })), Date.parse("2026-10-21T10:00:00Z"));
  assert.equal(expiryFromSubscriber(sub({ expires_date: null })), LIFETIME);
  assert.equal(expiryFromSubscriber({ subscriber: { entitlements: {} } }), null);
  assert.equal(expiryFromSubscriber({ subscriber: { entitlements: { other: { expires_date: "2030-01-01T00:00:00Z" } } } }), null);
  assert.equal(expiryFromSubscriber(sub({ expires_date: "not a date" })), null);
  for (const g of [null, undefined, {}, "x"]) assert.equal(expiryFromSubscriber(g), null);
});

test("fetchSubscriberExpiry sends the secret key, encodes the uid, and fails safe", async () => {
  let seen;
  const ok = async (url, init) => { seen = { url, auth: init.headers.Authorization }; return { ok: true, json: async () => ({ subscriber: { entitlements: { nera: { expires_date: "2026-10-21T10:00:00Z" } } } }) }; };
  assert.equal(await fetchSubscriberExpiry("a/b c", "sk_test", { fetchImpl: ok }), Date.parse("2026-10-21T10:00:00Z"));
  assert.equal(seen.url, "https://api.revenuecat.com/v1/subscribers/a%2Fb%20c");
  assert.equal(seen.auth, "Bearer sk_test");
  assert.equal(await fetchSubscriberExpiry("u", "k", { fetchImpl: async () => ({ ok: false, status: 401 }) }), null);
  assert.equal(await fetchSubscriberExpiry("u", "k", { fetchImpl: async () => { throw new Error("network"); } }), null);
});
