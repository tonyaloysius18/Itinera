// RevenueCat webhook: turns subscription events into paid_until on nera_entitlements.
// https://www.revenuecat.com/docs/integrations/webhooks/event-types-and-fields
// The app sets RevenueCat's app_user_id to the Firebase uid, so events map straight onto our users.

export const ENTITLEMENT_ID = "nera";

/** Constant-time string comparison (for the shared webhook secret). */
export function safeEqual(a, b) {
  if (typeof a !== "string" || typeof b !== "string" || a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i++) diff |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return diff === 0;
}

/**
 * Pure: what (if anything) should this event change?
 * Returns { uid, paidUntil, eventAt } or null when the event is not about a Nera subscription for a real user.
 */
export function eventToUpdate(event, now = Date.now()) {
  if (!event || typeof event !== "object") return null;
  if (event.type === "TEST" || event.type === "TRANSFER") return null;
  const uid = typeof event.app_user_id === "string" ? event.app_user_id : "";
  if (!uid || uid.startsWith("$RCAnonymousID") || uid.length > 128) return null;

  const ids = Array.isArray(event.entitlement_ids) ? event.entitlement_ids : event.entitlement_id ? [event.entitlement_id] : [];
  const forNera = ids.includes(ENTITLEMENT_ID) || (ids.length === 0 && /nera/i.test(String(event.product_id || "")));
  if (!forNera) return null;

  const exp = event.expiration_at_ms;
  if (typeof exp !== "number" || !isFinite(exp) || exp <= 0) return null;

  let paidUntil = exp;
  // A refund revokes access immediately, even though the paid period had time left.
  if (event.type === "CANCELLATION" && event.cancel_reason === "CUSTOMER_SUPPORT") paidUntil = Math.min(exp, now);

  const eventAt = typeof event.event_timestamp_ms === "number" ? event.event_timestamp_ms : now;
  return { uid, paidUntil, eventAt };
}

/** Applies an update; an older event never overwrites a newer one. Returns true if a row was written. */
export async function applyUpdate(db, update, now = Date.now()) {
  const res = await db
    .prepare(
      "INSERT INTO nera_entitlements (uid, trial_started_at, paid_until, paid_event_at) VALUES (?1, ?2, ?3, ?4) " +
      "ON CONFLICT(uid) DO UPDATE SET paid_until = excluded.paid_until, paid_event_at = excluded.paid_event_at " +
      "WHERE excluded.paid_event_at >= nera_entitlements.paid_event_at")
    .bind(update.uid, now, update.paidUntil, update.eventAt)
    .run();
  return (res.meta?.changes ?? 0) > 0;
}
