import test from "node:test";
import assert from "node:assert/strict";
import { evaluateEntitlement } from "../src/entitlement.js";

const DAY = 86_400_000;
const NOW = Date.UTC(2026, 8, 21, 12, 0, 0);
const UNPAID = { paid_until: 0 };

test("a fresh user has all the free trips left", () => {
  const r = evaluateEntitlement(UNPAID, NOW, 0, 3);
  assert.deepEqual([r.status, r.tripsUsed, r.tripsLeft, r.freeTrips], ["free", 0, 3, 3]);
});

test("each approved trip uses one free trip", () => {
  assert.equal(evaluateEntitlement(UNPAID, NOW, 1, 3).tripsLeft, 2);
  const last = evaluateEntitlement(UNPAID, NOW, 2, 3);
  assert.deepEqual([last.status, last.tripsLeft], ["free", 1]);
});

test("after the third trip the user is at the limit", () => {
  const r = evaluateEntitlement(UNPAID, NOW, 3, 3);
  assert.deepEqual([r.status, r.tripsLeft], ["limit", 0]);
});

test("more trips than the limit never yields a negative count", () => {
  const r = evaluateEntitlement(UNPAID, NOW, 5, 3);
  assert.deepEqual([r.status, r.tripsLeft], ["limit", 0]);
});

test("time does not matter: the free trips do not expire", () => {
  assert.equal(evaluateEntitlement(UNPAID, NOW + 400 * DAY, 0, 3).status, "free");
});

test("a paid subscription wins over used-up free trips", () => {
  const r = evaluateEntitlement({ paid_until: NOW + DAY }, NOW, 9, 3);
  assert.deepEqual([r.status, r.source], ["paid", "subscription"]);
});

test("a subscription that has run out falls back to the free-trip rules", () => {
  assert.equal(evaluateEntitlement({ paid_until: NOW - 1 }, NOW, 3, 3).status, "limit");
  assert.equal(evaluateEntitlement({ paid_until: NOW - 1 }, NOW, 1, 3).status, "free");
});

test("friends are always unlocked", () => {
  const r = evaluateEntitlement(UNPAID, NOW, 9, 3, { friend: true });
  assert.deepEqual([r.status, r.source], ["paid", "friend"]);
});

test("the number of free trips is configurable", () => {
  assert.equal(evaluateEntitlement(UNPAID, NOW, 3, 5).status, "free");
  assert.equal(evaluateEntitlement(UNPAID, NOW, 1, 1).status, "limit");
});
