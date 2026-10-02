// Moderation tool for community itineraries (shared with "List in Community" on). Run it from the repo root.
//
//   GOOGLE_APPLICATION_CREDENTIALS=/path/to/key.json node tools/moderation/queue.js <command> [id] [reason]
//
// Commands
//   list              itineraries waiting for review (oldest first)
//   show <id>         everything in one itinerary: places, journeys, author id
//   approve <id>      list it in the Community feed
//   reject <id>       decline it (it stays reachable by its link, but is never listed)
//   takedown <id>     remove an approved one from the feed
//   remove <id>       delete it entirely (public copy, copy markers and the owner's record)
//   reports           open reports against shared itineraries, grouped by itinerary, most reported first
//   resolve <id>      mark every open report on that itinerary as reviewed
//
// Uses the firebase-admin copy already installed under functions/, so no separate npm install is needed.

const path = require("path");
const fs = require("fs");
const { createRequire } = require("module");
const requireFromFunctions = createRequire(path.join(__dirname, "..", "..", "functions", "package.json"));
const { initializeApp, cert } = requireFromFunctions("firebase-admin/app");
const { getFirestore } = requireFromFunctions("firebase-admin/firestore");

const keyPath = process.env.GOOGLE_APPLICATION_CREDENTIALS;
if (!keyPath) { console.error("Set GOOGLE_APPLICATION_CREDENTIALS to a service-account key file."); process.exit(1); }
initializeApp({ credential: cert(JSON.parse(fs.readFileSync(keyPath, "utf8"))) });
const db = getFirestore();
const shared = db.collection("sharedItineraries");

const summary = (d) => {
  const stops = d.days.reduce((n, x) => n + x.stops.length, 0);
  return `${d.id}  "${d.title}"  [${(d.countries || []).join(", ") || "no country"}]  ${d.days.length} days, ${stops} places`;
};

async function get(id) {
  if (!id) throw new Error("This command needs an itinerary id.");
  const snap = await shared.doc(id).get();
  if (!snap.exists) throw new Error(`No shared itinerary with id ${id}.`);
  return snap;
}

const commands = {
  async list() {
    const snap = await shared.where("listRequested", "==", true).where("status", "==", "").get();
    const rows = snap.docs.map((s) => s.data()).sort((a, b) => (a.sharedAt || 0) - (b.sharedAt || 0));
    console.log(rows.length ? `${rows.length} waiting for review:\n` : "Nothing waiting for review.");
    rows.forEach((d) => console.log("  " + summary(d)));
  },
  async show(id) {
    const d = (await get(id)).data();
    console.log(summary(d));
    console.log(`status: ${d.status || "pending"} | listed in feed: ${d.feed} | copies: ${d.copyCount} | author uid: ${d.authorUid || "(link-only)"}`);
    if (d.description) console.log(`description: ${d.description}`);
    d.days.forEach((day) => {
      console.log(`\nDay ${day.dayNumber}`);
      day.stops.forEach((s) => console.log(`  ${s.time || "     "}  ${s.title}  —  ${s.location}`));
    });
    (d.legs || []).forEach((l) => console.log(`\nJourney, day ${l.dayNumber}: ${l.fromCity} -> ${l.toCity} by ${l.transport}`));
  },
  async approve(id) {
    const d = (await get(id)).data();
    if (!d.listRequested) throw new Error("The owner did not ask to list this one in Community.");
    await shared.doc(id).update({ status: "approved", feed: true, approvedAt: Date.now() });
    console.log("Approved and listed:", summary(d));
  },
  async reject(id) {
    await get(id);
    await shared.doc(id).update({ status: "rejected", feed: false });
    console.log("Rejected:", id);
  },
  async takedown(id) {
    await get(id);
    await shared.doc(id).update({ status: "taken_down", feed: false });
    console.log("Removed from the feed:", id);
  },
  async remove(id) {
    await get(id);
    const copies = await shared.doc(id).collection("copies").get();
    await Promise.all(copies.docs.map((c) => c.ref.delete()));
    // The owner's private record sits at users/{authorUid}/sharedLinks/{id}; link-only shares have no authorUid.
    const authorUid = (await shared.doc(id).get()).data().authorUid;
    const owners = authorUid ? [db.doc(`users/${authorUid}/sharedLinks/${id}`)] : [];
    await Promise.all(owners.map((o) => o.delete()));
    await shared.doc(id).delete();
    console.log(`Deleted ${id} (and ${copies.size} copy markers, ${owners.length} owner record).`);
  },
  async reports() {
    const snap = await db.collection("reports").where("status", "==", "open").get();
    const groups = {};
    snap.docs.forEach((r) => {
      const d = r.data();
      if (typeof d.tripId === "string" && d.tripId.startsWith("shared_")) (groups[d.tripId.slice(7)] ||= []).push(d.reason);
    });
    const ids = Object.keys(groups).sort((a, b) => groups[b].length - groups[a].length);
    console.log(ids.length ? `${ids.length} shared itinerary(ies) with open reports:\n` : "No open reports on shared itineraries.");
    for (const id of ids) {
      const s = await shared.doc(id).get();
      console.log(`  ${groups[id].length} report(s) [${[...new Set(groups[id])].join(", ")}]  ${s.exists ? summary(s.data()) : id + " (already removed)"}`);
    }
  },
  async resolve(id) {
    if (!id) throw new Error("This command needs an itinerary id.");
    const snap = await db.collection("reports").where("tripId", "==", "shared_" + id).where("status", "==", "open").get();
    await Promise.all(snap.docs.map((r) => r.ref.update({ status: "reviewed" })));
    console.log(`Marked ${snap.size} report(s) as reviewed.`);
  },
};

const [cmd, id] = process.argv.slice(2);
if (!commands[cmd]) { console.error("Usage: node tools/moderation/queue.js <list|show|approve|reject|takedown|remove|reports|resolve> [id]"); process.exit(1); }
commands[cmd](id).then(() => process.exit(0)).catch((e) => { console.error(e.message); process.exit(1); });
