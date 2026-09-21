-- Who may use Nera: a free trial that starts at a user's first request, and an optional paid unlock.
CREATE TABLE IF NOT EXISTS nera_entitlements (
  uid              TEXT PRIMARY KEY,
  trial_started_at INTEGER NOT NULL,             -- epoch millis of the first Nera request
  paid_until       INTEGER NOT NULL DEFAULT 0    -- epoch millis; 0 = never paid. Written by the purchase webhook later.
);
