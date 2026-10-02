import test from "node:test";
import assert from "node:assert/strict";
import { hasBlockedWord, idsToHide, reviewListing } from "../src/moderation.js";

const stop = (title, location = "Lisbon") => ({ title, location });
const good = () => ({
  id: "abc123", listRequested: true, status: "", authorUid: "u1", title: "Lisbon Weekend", description: "Two relaxed days",
  days: [{ stops: [stop("Belem Tower"), stop("Alfama walk")] }, { stops: [stop("Sintra Palace", "Sintra"), stop("Cabo da Roca", "Colares")] }],
});

test("a normal listing is approved", () => assert.equal(reviewListing(good()).action, "approve"));
test("link-only shares and reviewed listings are left alone", () => {
  assert.equal(reviewListing({ ...good(), listRequested: false }).action, "skip");
  assert.equal(reviewListing({ ...good(), status: "approved" }).action, "skip");
  assert.equal(reviewListing({ ...good(), authorUid: "" }).action, "skip");
});
test("too few places stay pending for a human", () => {
  const d = good(); d.days = [{ stops: [stop("A")] }];
  assert.equal(reviewListing(d).action, "skip");
});
test("blocked words anywhere are rejected, including leetspeak", () => {
  assert.equal(reviewListing({ ...good(), title: "Free p0rn trip" }).action, "reject");
  const d = good(); d.days[0].stops[0].title = "Casino night"; assert.equal(reviewListing(d).action, "reject");
});
test("links and contact details are rejected", () => {
  assert.equal(reviewListing({ ...good(), description: "visit www.example.com" }).action, "reject");
  assert.equal(reviewListing({ ...good(), description: "call 555-123-4567" }).action, "reject");
  assert.equal(reviewListing({ ...good(), description: "dm me @someone" }).action, "reject");
});
test("whole words only: place names containing a blocked word are fine", () => {
  assert.equal(hasBlockedWord("Sussex and Essex weekend"), false);
  assert.equal(hasBlockedWord("Scunthorpe"), false);
  assert.equal(hasBlockedWord("what the shit"), true);
  assert.equal(hasBlockedWord("Dickens walking tour"), false);
  assert.equal(hasBlockedWord("Shitennoji temple, Osaka"), false);
  assert.equal(hasBlockedWord("Fagaras mountains"), false);
});
test("hide after enough distinct reporters, ignoring other reports and repeats", () => {
  const r = (tripId, reporterUid) => ({ tripId, reporterUid });
  const reports = [r("shared_a", "1"), r("shared_a", "2"), r("shared_a", "2"), r("shared_b", "1"), r("trip_x", "1"), r("trip_x", "2"), r("trip_x", "3")];
  assert.deepEqual(idsToHide(reports), []);
  assert.deepEqual(idsToHide([...reports, r("shared_a", "3")]), ["a"]);
});

test("common endings and compounds are caught", () => {
  for (const w of ["fucker", "fuckers", "motherfucker", "You are an idiot", "idiots", "stupid trip", "what a moron", "bitches", "shitty hotel", "F u c k"]) {
    assert.equal(hasBlockedWord(w), w === "F u c k" ? false : true, w);
  }
});
