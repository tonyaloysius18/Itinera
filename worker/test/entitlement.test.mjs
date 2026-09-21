import test from "node:test";
import assert from "node:assert/strict";
import { evaluateEntitlement } from "../src/entitlement.js";

const DAY = 86_400_000;
const NOW = Date.UTC(2026, 8, 21, 12, 0, 0);

test("a fresh user is in trial with the full period left", () => {
  const r = evaluateEntitlement({ trial_started_at: NOW, paid_until: 0 }, NOW, 7);
  assert.deepEqual([r.status, r.daysLeft], ["trial", 7]);
});

test("days left rounds up so the last partial day still counts", () => {
  const r = evaluateEntitlement({ trial_started_at: NOW - 6.5 * DAY, paid_until: 0 }, NOW, 7);
  assert.deepEqual([r.status, r.daysLeft], ["trial", 1]);
});

test("the trial expires exactly at the end of the period", () => {
  assert.equal(evaluateEntitlement({ trial_started_at: NOW - 7 * DAY + 1, paid_until: 0 }, NOW, 7).status, "trial");
  assert.equal(evaluateEntitlement({ trial_started_at: NOW - 7 * DAY, paid_until: 0 }, NOW, 7).status, "expired");
  assert.equal(evaluateEntitlement({ trial_started_at: NOW - 30 * DAY, paid_until: 0 }, NOW, 7).status, "expired");
});

test("a paid unlock wins over an expired trial", () => {
  assert.equal(evaluateEntitlement({ trial_started_at: NOW - 30 * DAY, paid_until: NOW + DAY }, NOW, 7).status, "paid");
});

test("a paid period that has run out falls back to the trial rules", () => {
  assert.equal(evaluateEntitlement({ trial_started_at: NOW - 30 * DAY, paid_until: NOW - 1 }, NOW, 7).status, "expired");
});

test("trial length is configurable", () => {
  assert.equal(evaluateEntitlement({ trial_started_at: NOW - 10 * DAY, paid_until: 0 }, NOW, 14).status, "trial");
  assert.equal(evaluateEntitlement({ trial_started_at: NOW - 10 * DAY, paid_until: 0 }, NOW, 3).status, "expired");
});
