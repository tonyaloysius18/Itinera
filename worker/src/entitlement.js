// Who may use Nera right now. Pure function so it is trivially testable.

/**
 * @param row        { paid_until } from nera_entitlements (epoch millis)
 * @param tripsUsed  how many trips this user has created with Nera (approved drafts)
 * @param freeTrips  how many trips are free before the subscription is needed
 * @returns { status: "paid", source }
 *        | { status: "free", tripsUsed, tripsLeft, freeTrips }    free trips remain
 *        | { status: "limit", tripsUsed, tripsLeft: 0, freeTrips } free trips used up: new trips need the subscription
 */
export function evaluateEntitlement(row, now, tripsUsed, freeTrips, { friend = false } = {}) {
  if (friend) return { status: "paid", source: "friend" };
  if (row.paid_until > now) return { status: "paid", source: "subscription" };
  const tripsLeft = Math.max(0, freeTrips - tripsUsed);
  return { status: tripsLeft > 0 ? "free" : "limit", tripsUsed, tripsLeft, freeTrips };
}

/**
 * Friends & family get Nera free, by email. Only a VERIFIED email counts: otherwise anyone could sign up with a
 * friend's address and get in.
 */
export async function isFriend(db, email, emailVerified) {
  if (!emailVerified || !email) return false;
  const row = await db.prepare("SELECT 1 AS ok FROM nera_friends WHERE email = ?1").bind(email).first();
  return row !== null;
}

/** Reads/creates the user's entitlement row and counts their Nera trips, then evaluates where they stand. */
export async function getEntitlement(db, uid, now, freeTrips, { friend = false } = {}) {
  await db
    .prepare("INSERT OR IGNORE INTO nera_entitlements (uid, trial_started_at, paid_until) VALUES (?1, ?2, 0)")
    .bind(uid, now)
    .run();
  const row = await db.prepare("SELECT paid_until FROM nera_entitlements WHERE uid = ?1").bind(uid).first();
  const used = await db.prepare("SELECT COUNT(*) AS n FROM nera_trips WHERE uid = ?1").bind(uid).first();
  return evaluateEntitlement(row, now, used ? used.n : 0, freeTrips, { friend });
}

/** Remembers that this user created a trip with Nera. Idempotent per trip id, so a retry never counts twice. */
export async function recordTrip(db, uid, tripId, now) {
  await db
    .prepare("INSERT OR IGNORE INTO nera_trips (uid, trip_id, created_at) VALUES (?1, ?2, ?3)")
    .bind(uid, tripId, now)
    .run();
}
