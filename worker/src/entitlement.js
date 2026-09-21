// Who may use Nera right now. Pure function so it is trivially testable.

const DAY_MS = 86_400_000;

/**
 * @param row  { trial_started_at, paid_until } from nera_entitlements (epoch millis)
 * @returns { status: "paid" }
 *        | { status: "trial", trialEndsAt, daysLeft }
 *        | { status: "expired", trialEndsAt }
 */
export function evaluateEntitlement(row, now, trialDays, { friend = false } = {}) {
  if (friend) return { status: "paid", source: "friend" };
  if (row.paid_until > now) return { status: "paid", source: "subscription" };
  const trialEndsAt = row.trial_started_at + trialDays * DAY_MS;
  if (now < trialEndsAt) {
    return { status: "trial", trialEndsAt, daysLeft: Math.max(1, Math.ceil((trialEndsAt - now) / DAY_MS)) };
  }
  return { status: "expired", trialEndsAt };
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

/** Reads/creates the user's entitlement row (the trial starts at the first ever request) and evaluates it. */
export async function getEntitlement(db, uid, now, trialDays, { friend = false } = {}) {
  await db
    .prepare("INSERT OR IGNORE INTO nera_entitlements (uid, trial_started_at, paid_until) VALUES (?1, ?2, 0)")
    .bind(uid, now)
    .run();
  const row = await db
    .prepare("SELECT trial_started_at, paid_until FROM nera_entitlements WHERE uid = ?1")
    .bind(uid)
    .first();
  return evaluateEntitlement(row, now, trialDays, { friend });
}
