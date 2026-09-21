-- Free tier is counted in trips, not days: every approved Nera draft is one row here.
-- (uid, trip_id) is unique, so an app retry of the same approval never uses up a second free trip.
CREATE TABLE IF NOT EXISTS nera_trips (
  uid        TEXT NOT NULL,
  trip_id    TEXT NOT NULL,
  created_at INTEGER NOT NULL,          -- epoch millis
  PRIMARY KEY (uid, trip_id)
);
