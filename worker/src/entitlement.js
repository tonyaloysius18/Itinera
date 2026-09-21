// Who may use Nera right now. Pure function so it is trivially testable.

const DAY_MS = 86_400_000;

/**
 * @param row  { trial_started_at, paid_until } from nera_entitlements (epoch millis)
 * @returns { status: "paid" }
 *        | { status: "trial", trialEndsAt, daysLeft }
 *        | { status: "expired", trialEndsAt }
 */
export function evaluateEntitlement(row, now, trialDays) {
  if (row.paid_until > now) return { status: "paid" };
  const trialEndsAt = row.trial_started_at + trialDays * DAY_MS;
  if (now < trialEndsAt) {
    return { status: "trial", trialEndsAt, daysLeft: Math.max(1, Math.ceil((trialEndsAt - now) / DAY_MS)) };
  }
  return { status: "expired", trialEndsAt };
}

/** Reads/creates the user's entitlement row (the trial starts at the first ever request) and evaluates it. */
export async function getEntitlement(db, uid, now, trialDays) {
  await db
    .prepare("INSERT OR IGNORE INTO nera_entitlements (uid, trial_started_at, paid_until) VALUES (?1, ?2, 0)")
    .bind(uid, now)
    .run();
  const row = await db
    .prepare("SELECT trial_started_at, paid_until FROM nera_entitlements WHERE uid = ?1")
    .bind(uid)
    .first();
  return evaluateEntitlement(row, now, trialDays);
}
