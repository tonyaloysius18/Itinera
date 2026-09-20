-- Per-user daily request counter (rate limit).
CREATE TABLE IF NOT EXISTS nera_usage (
  uid   TEXT NOT NULL,
  day   TEXT NOT NULL,          -- UTC date, YYYY-MM-DD
  count INTEGER NOT NULL,
  PRIMARY KEY (uid, day)
);

-- Cache for place-search results (no user data).
CREATE TABLE IF NOT EXISTS nera_cache (
  key        TEXT PRIMARY KEY,
  value      TEXT NOT NULL,
  expires_at INTEGER NOT NULL   -- epoch millis
);
