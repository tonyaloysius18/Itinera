-- Guards against out-of-order webhook events: only the newest event may change paid_until.
ALTER TABLE nera_entitlements ADD COLUMN paid_event_at INTEGER NOT NULL DEFAULT 0;

-- Monthly fair-use counter for paying users.
CREATE TABLE IF NOT EXISTS nera_usage_month (
  uid   TEXT NOT NULL,
  month TEXT NOT NULL,          -- UTC month, YYYY-MM
  count INTEGER NOT NULL,
  PRIMARY KEY (uid, month)
);

-- Aggregated real cost per day, so the price can be checked against reality.
CREATE TABLE IF NOT EXISTS nera_cost (
  day           TEXT PRIMARY KEY,   -- UTC date
  requests      INTEGER NOT NULL DEFAULT 0,
  model_calls   INTEGER NOT NULL DEFAULT 0,
  input_tokens  INTEGER NOT NULL DEFAULT 0,
  output_tokens INTEGER NOT NULL DEFAULT 0
);

-- Friends & family: these verified emails get Nera for free.
CREATE TABLE IF NOT EXISTS nera_friends (
  email    TEXT PRIMARY KEY,        -- lowercase
  note     TEXT NOT NULL DEFAULT '',
  added_at INTEGER NOT NULL
);
