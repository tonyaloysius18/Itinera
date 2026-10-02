// Automatic review of Community listings (option "approve, then moderate afterwards").
// Pure functions, so the rules are easy to test and to tune. No network here.

export const MIN_STOPS = 4;            // matches the in-app minimum for listing
export const REPORTS_TO_HIDE = 3;      // distinct people reporting a listed itinerary before it is hidden automatically
export const MAX_TITLE = 100;
export const MAX_DESCRIPTION = 500;

// Deliberately short and unambiguous. This is a first line of defence, not a complete filter: anything that slips
// through is caught by reports (auto-hide at REPORTS_TO_HIDE) and the moderator CLI. Matched on whole words, after
// lower-casing and stripping look-alike characters.
const BLOCKED_WORDS = [
  "fuck", "fucking", "shit", "bitch", "cunt", "asshole", "dick", "pussy", "nigger", "nigga", "faggot", "whore", "slut",
  "porn", "porno", "xxx", "sex", "escort", "nude", "nudes", "onlyfans", "casino", "viagra", "cialis",
  "bitcoin", "crypto", "forex", "loan", "nazi", "hitler", "kill yourself", "rape", "terrorist",
];
const URL_PATTERN = /(https?:\/\/|www\.|\b[a-z0-9-]+\.(com|net|org|io|ru|xyz|top|click|link|shop|biz)\b)/i;
const CONTACT_PATTERN = /(\b\d{3}[\s.-]?\d{3,4}[\s.-]?\d{4}\b|@[a-z0-9_.]{3,}|\bwhats ?app\b|\btelegram\b|\bt\.me\b)/i;

const normalize = (s) =>
  String(s || "").toLowerCase().replace(/[01345$@!]/g, (c) => ({ 0: "o", 1: "i", 3: "e", 4: "a", 5: "s", $: "s", "@": "a", "!": "i" })[c]);

/** True when [text] contains a blocked word as a whole word/phrase. */
export function hasBlockedWord(text) {
  const t = ` ${normalize(text).replace(/[^a-z\s]/g, " ").replace(/\s+/g, " ")} `;
  return BLOCKED_WORDS.some((w) => t.includes(` ${w} `));
}

/** Every piece of free text a person typed in the listing: title, description and each place's name and location. */
function freeText(doc) {
  const parts = [doc.title, doc.description];
  for (const day of doc.days || []) for (const s of day.stops || []) parts.push(s.title, s.location);
  return parts.filter(Boolean);
}

export const stopCount = (doc) => (doc.days || []).reduce((n, d) => n + (d.stops || []).length, 0);

/**
 * Decides what to do with a listing request.
 *   "approve"  list it in the feed now
 *   "reject"   clearly not acceptable (blocked words, links or contact details): never listed
 *   "skip"     leave it alone (not a listing request, already reviewed, or too thin: stays pending for a human)
 */
export function reviewListing(doc) {
  if (!doc || doc.listRequested !== true || (doc.status || "") !== "") return { action: "skip", reason: "not pending" };
  if (!doc.authorUid) return { action: "skip", reason: "no author" };
  const title = String(doc.title || "").trim();
  if (title.length < 3 || title.length > MAX_TITLE) return { action: "skip", reason: "title length" };
  if (String(doc.description || "").length > MAX_DESCRIPTION) return { action: "skip", reason: "description too long" };
  if (stopCount(doc) < MIN_STOPS) return { action: "skip", reason: "too few places" };

  const text = freeText(doc);
  if (text.some(hasBlockedWord)) return { action: "reject", reason: "blocked word" };
  if (text.some((t) => URL_PATTERN.test(t))) return { action: "reject", reason: "link in text" };
  if (text.some((t) => CONTACT_PATTERN.test(t))) return { action: "reject", reason: "contact details in text" };
  return { action: "approve", reason: "passed checks" };
}

/**
 * Which listed itineraries to hide, given the open reports. [reports] are { tripId, reporterUid }; only reports on
 * shared itineraries count (tripId "shared_<id>"), and several reports from one person count once.
 * Returns the itinerary ids that reached the threshold.
 */
export function idsToHide(reports, threshold = REPORTS_TO_HIDE) {
  const byId = new Map();
  for (const r of reports) {
    if (typeof r.tripId !== "string" || !r.tripId.startsWith("shared_")) continue;
    const id = r.tripId.slice(7);
    if (!byId.has(id)) byId.set(id, new Set());
    byId.get(id).add(r.reporterUid || `anon_${byId.get(id).size}`);
  }
  return [...byId].filter(([, who]) => who.size >= threshold).map(([id]) => id);
}
