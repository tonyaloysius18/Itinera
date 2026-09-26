// Data tools Nera can call mid-conversation. Each returns a small JSON-serialisable object;
// errors are returned as { error } so the model can tell the traveller instead of failing.

const crypto = require("crypto");

const DAY_MS = 86400000;
const FORECAST_DAYS = 15;   // Open-Meteo forecast horizon (16 days incl. today)
const MAX_RANGE_DAYS = 16;
const PLACES_CACHE_MS = 7 * DAY_MS;

const iso = (d) => d.toISOString().slice(0, 10);
const isIso = (s) => typeof s === "string" && /^\d{4}-\d{2}-\d{2}$/.test(s) && !isNaN(Date.parse(s));
const round1 = (n) => Math.round(n * 10) / 10;
const avg = (xs) => (xs.length ? round1(xs.reduce((a, b) => a + b, 0) / xs.length) : null);

async function getJson(fetchImpl, url, init = {}) {
  const res = await fetchImpl(url, { ...init, signal: AbortSignal.timeout(8000) });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

/** get_weather: real forecast when the dates are close, last year's actuals as a climate guide otherwise. */
async function getWeather({ city, start_date, end_date }, { fetchImpl = fetch, now = new Date() } = {}) {
  if (typeof city !== "string" || !city.trim()) return { error: "city is required" };
  try {
    const geo = await getJson(fetchImpl,
      `https://geocoding-api.open-meteo.com/v1/search?count=1&name=${encodeURIComponent(city.trim())}`);
    const place = geo.results && geo.results[0];
    if (!place) return { error: `Couldn't find a place called "${city}".` };

    const today = new Date(iso(now) + "T00:00:00Z");
    let start = isIso(start_date) ? new Date(start_date + "T00:00:00Z") : today;
    let end = isIso(end_date) ? new Date(end_date + "T00:00:00Z") : new Date(Math.min(+start + 6 * DAY_MS, +start + MAX_RANGE_DAYS * DAY_MS));
    if (end < start) end = start;
    if ((end - start) / DAY_MS >= MAX_RANGE_DAYS) end = new Date(+start + (MAX_RANGE_DAYS - 1) * DAY_MS);

    const withinForecast = start >= today && end <= new Date(+today + FORECAST_DAYS * DAY_MS);
    const daily = "temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max";
    let source, url;
    if (withinForecast) {
      source = "live forecast";
      url = `https://api.open-meteo.com/v1/forecast?latitude=${place.latitude}&longitude=${place.longitude}` +
        `&daily=${daily}&timezone=auto&start_date=${iso(start)}&end_date=${iso(end)}`;
    } else {
      // Beyond the forecast horizon (or in the past): use the same dates in the most recent complete year.
      let years = 1;
      const shift = (d, y) => { const c = new Date(d); c.setUTCFullYear(c.getUTCFullYear() - y); return c; };
      while (shift(end, years) > new Date(+today - 7 * DAY_MS)) years++;
      const s = shift(start, years), e = shift(end, years);
      source = `historical actuals from ${s.getUTCFullYear()} for the same dates (a guide, not a forecast)`;
      url = `https://archive-api.open-meteo.com/v1/archive?latitude=${place.latitude}&longitude=${place.longitude}` +
        `&daily=temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=auto&start_date=${iso(s)}&end_date=${iso(e)}`;
    }
    const data = await getJson(fetchImpl, url);
    const d = data.daily || {};
    const highs = (d.temperature_2m_max || []).filter((x) => x != null);
    const lows = (d.temperature_2m_min || []).filter((x) => x != null);
    const rain = d.precipitation_sum || [];
    return {
      place: [place.name, place.admin1, place.country].filter(Boolean).join(", "),
      source,
      unit: "celsius, mm",
      avgHigh: avg(highs),
      avgLow: avg(lows),
      rainyDays: rain.filter((x) => x != null && x >= 1).length,
      totalRainMm: round1(rain.reduce((a, b) => a + (b || 0), 0)),
      days: (d.time || []).slice(0, MAX_RANGE_DAYS).map((t, i) => ({
        date: t,
        high: d.temperature_2m_max?.[i] ?? null,
        low: d.temperature_2m_min?.[i] ?? null,
        rainMm: d.precipitation_sum?.[i] ?? null,
        ...(d.precipitation_probability_max ? { rainChancePct: d.precipitation_probability_max[i] ?? null } : {}),
      })),
    };
  } catch (e) {
    console.error("get_weather failed:", e && e.message);
    return { error: "Weather service is unavailable right now." };
  }
}

const PRICE = {
  PRICE_LEVEL_FREE: "free", PRICE_LEVEL_INEXPENSIVE: "$", PRICE_LEVEL_MODERATE: "$$",
  PRICE_LEVEL_EXPENSIVE: "$$$", PRICE_LEVEL_VERY_EXPENSIVE: "$$$$",
};

/**
 * search_places: real, rated places via Google Places (New) text search.
 * `cache` is { get(key), set(key, value) } backed by Firestore so repeat queries cost nothing.
 */
async function searchPlaces({ query, city, max_results }, { apiKey, cache, fetchImpl = fetch } = {}) {
  if (typeof query !== "string" || !query.trim()) return { error: "query is required" };
  if (!apiKey) return { error: "Place search isn't configured." };
  const limit = Math.min(Math.max(parseInt(max_results, 10) || 6, 1), 8);
  const textQuery = [query.trim(), typeof city === "string" && city.trim() ? `in ${city.trim()}` : ""].filter(Boolean).join(" ");
  const key = crypto.createHash("sha1").update(`${textQuery.toLowerCase()}|${limit}`).digest("hex");
  try {
    const hit = cache && (await cache.get(key));
    if (hit) return hit;
    const data = await getJson(fetchImpl, "https://places.googleapis.com/v1/places:searchText", {
      method: "POST",
      headers: {
        "content-type": "application/json",
        "X-Goog-Api-Key": apiKey,
        "X-Goog-FieldMask":
          "places.displayName,places.formattedAddress,places.rating,places.userRatingCount,places.priceLevel," +
          "places.primaryTypeDisplayName,places.location,places.googleMapsUri",
      },
      body: JSON.stringify({ textQuery, maxResultCount: limit, languageCode: "en" }),
    });
    const result = {
      places: (data.places || []).map((p) => ({
        name: p.displayName?.text || "",
        type: p.primaryTypeDisplayName?.text || "",
        address: p.formattedAddress || "",
        rating: p.rating ?? null,
        ratingCount: p.userRatingCount ?? null,
        price: PRICE[p.priceLevel] || null,
        lat: p.location?.latitude ?? null,
        lng: p.location?.longitude ?? null,
        mapsUrl: p.googleMapsUri || "",
      })).filter((p) => p.name),
    };
    if (cache && result.places.length) await cache.set(key, result, PLACES_CACHE_MS);
    return result;
  } catch (e) {
    console.error("search_places failed:", e && e.message);
    return { error: "Place search is unavailable right now." };
  }
}

const TRANSPORT_MODES = ["train", "bus", "flight"];

// Real timetables from Transitous (open-source, community-run journey planner over public GTFS feeds: SNCF, DB, FlixBus
// and many more). Free and keyless, so it is used politely: identified User-Agent and a 1 hour cache per route and date.
const TRANSITOUS = "https://api.transitous.org/api";
const TRANSITOUS_UA = "Itinera trip planner (github.com/tonyaloysius18/Itinera)";
const TIMETABLE_CACHE_MS = 60 * 60 * 1000;
const MAX_TIMETABLE_OPTIONS = 5;

const modeOf = (m) => {
  if (/RAIL|TRAIN|LONG_DISTANCE/.test(m)) return "train";
  if (m === "COACH" || m === "BUS") return "bus";
  if (m === "FERRY") return "ferry";
  if (m === "AIRPLANE") return "flight";
  return null;   // WALK, TRANSFER, local metro/tram: not shown as a transport option in their own right
};

/** Offset (ms) of time zone [tz] from UTC at [instant]. */
function tzOffsetMs(tz, instant) {
  const p = Object.fromEntries(new Intl.DateTimeFormat("en-US", {
    timeZone: tz, hourCycle: "h23", year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit", second: "2-digit",
  }).formatToParts(new Date(instant)).map((x) => [x.type, x.value]));
  return Date.UTC(p.year, p.month - 1, p.day, p.hour, p.minute, p.second) - Math.floor(instant / 1000) * 1000;
}

/** The instant at which the wall clock in [tz] reads [dateIso] [hour]:00. */
function localToInstant(dateIso, hour, tz) {
  const guess = Date.parse(`${dateIso}T${String(hour).padStart(2, "0")}:00:00Z`);
  return guess - tzOffsetMs(tz, guess - tzOffsetMs(tz, guess));
}

const fmtLocal = (iso, tz) => new Intl.DateTimeFormat("en-GB", { timeZone: tz, hour: "2-digit", minute: "2-digit", hourCycle: "h23" }).format(new Date(iso));
const localDate = (iso, tz) => new Intl.DateTimeFormat("en-CA", { timeZone: tz }).format(new Date(iso));

const km = (a, b) => {
  const r = Math.PI / 180, dLat = (b.lat - a.lat) * r, dLon = (b.lon - a.lon) * r;
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(a.lat * r) * Math.cos(b.lat * r) * Math.sin(dLon / 2) ** 2;
  return 12742 * Math.asin(Math.sqrt(h));
};

/**
 * Where to route from/to for a city name. Searching stops by name alone is unreliable ("Cologne" finds a tiny Italian
 * bus stop, "Paris" one in Brazil), so first find the CITY, then use the busiest rail stop within 10 km of it, or
 * the city centre when there is none (the journey planner walks to the nearest stops itself).
 */
async function geocodeCity(city, fetchImpl) {
  const get = (extra) => getJson(fetchImpl, `${TRANSITOUS}/v1/geocode?text=${encodeURIComponent(city)}&language=en${extra}`,
    { headers: { "user-agent": TRANSITOUS_UA } });
  const [any, stops] = await Promise.all([get(""), get("&type=STOP").catch(() => [])]);
  const list = Array.isArray(any) ? any : [];
  const place = list.find((r) => r.type === "PLACE") || list[0];
  if (!place || !Number.isFinite(place.lat) || !Number.isFinite(place.lon)) return null;
  const station = [...list, ...(Array.isArray(stops) ? stops : [])]
    .filter((r) => r.type === "STOP" && Array.isArray(r.modes) && r.modes.some((m) => /RAIL|COACH/.test(m)) && km(place, r) <= 10)
    .sort((x, y) => (y.importance || 0) - (x.importance || 0))[0];
  const at = station || place;
  return { lat: at.lat, lon: at.lon, tz: place.tz || at.tz || "UTC" };
}

/**
 * Scheduled journeys between two cities on [date]: departure/arrival in local time, duration, changes, modes, operators.
 * Returns null when nothing usable came back (no coverage, service down); the caller then falls back to links only.
 */
async function timetable(from, to, date, { fetchImpl = fetch, cache } = {}) {
  const key = `tt:${from.toLowerCase()}|${to.toLowerCase()}|${date}`;
  const hit = cache && (await cache.get(key).catch(() => null));
  if (hit) return hit;
  try {
    const [a, b] = await Promise.all([geocodeCity(from, fetchImpl), geocodeCity(to, fetchImpl)]);
    if (!a || !b) return null;
    const start = new Date(localToInstant(date, 5, a.tz)).toISOString();
    const plan = await getJson(fetchImpl,
      `${TRANSITOUS}/v5/plan?fromPlace=${a.lat},${a.lon}&toPlace=${b.lat},${b.lon}&time=${encodeURIComponent(start)}&numItineraries=8`,
      { headers: { "user-agent": TRANSITOUS_UA } });
    const seen = new Set();
    const options = [];
    for (const it of plan.itineraries || []) {
      if (localDate(it.startTime, a.tz) !== date) continue;
      let legs = (it.legs || []).filter((l) => l.mode !== "WALK");
      // A short local ride at either end (metro, tram, city bus, RER) is the way to/from the station, not a change.
      const isLastMile = (l) => !/HIGHSPEED|LONG_DISTANCE|NIGHT|COACH|AIRPLANE|FERRY/.test(l.mode) && (l.duration || 0) <= 25 * 60;
      while (legs.length > 1 && isLastMile(legs[0])) legs = legs.slice(1);
      while (legs.length > 1 && isLastMile(legs[legs.length - 1])) legs = legs.slice(0, -1);
      legs = legs.map((l) => ({ ...l, kind: modeOf(l.mode) })).filter((l) => l.kind);
      if (!legs.length) continue;
      const depart = fmtLocal(it.startTime, a.tz), arrive = fmtLocal(it.endTime, b.tz);
      if (seen.has(depart + arrive)) continue;
      seen.add(depart + arrive);
      options.push({
        depart, arrive,
        durationMin: Math.round((it.duration || 0) / 60),
        changes: Math.max(0, legs.length - 1),
        modes: [...new Set(legs.map((l) => l.kind))],
        operators: [...new Set(legs.map((l) => l.agencyName).filter(Boolean))].slice(0, 3),
        lines: legs.map((l) => l.routeShortName || l.displayName).filter(Boolean).slice(0, 3),
      });
      if (options.length >= MAX_TIMETABLE_OPTIONS) break;
    }
    if (!options.length) return null;
    const result = { from, to, date, options };
    if (cache) await cache.set(key, result, TIMETABLE_CACHE_MS).catch(() => {});
    return result;
  } catch (e) {
    console.error("timetable failed:", e && e.message);
    return null;
  }
}

/**
 * transport_options: real scheduled train/bus options for a route (times, changes, operators; no prices) plus
 * booking/comparison links. Timetables come from Transitous; if there is no coverage the result is links only.
 * The app shows [timetable] as cards and [links] as buttons under Nera's reply.
 */
async function transportOptions({ from, to, date, modes }, { fetchImpl = fetch, cache, now = new Date() } = {}) {
  const a = typeof from === "string" ? from.trim().slice(0, 80) : "";
  const b = typeof to === "string" ? to.trim().slice(0, 80) : "";
  if (!a || !b) return { error: "from and to are required" };
  const want = Array.isArray(modes) ? modes.filter((m) => TRANSPORT_MODES.includes(m)) : [];
  const has = (m) => want.length === 0 || want.includes(m);
  const slug = (c) => encodeURIComponent(c.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, ""));
  const routeKey = `${a.toLowerCase()}|${b.toLowerCase()}`;
  const links = [{ mode: "all", route: routeKey, from: a, to: b, provider: "Rome2Rio", label: `${a} → ${b}: compare all (Rome2Rio)`, url: `https://www.rome2rio.com/map/${encodeURIComponent(a)}/${encodeURIComponent(b)}` }];
  if (has("train")) links.push({ mode: "train", route: routeKey, from: a, to: b, provider: "Omio", label: `${a} → ${b}: trains (Omio)`, url: `https://www.omio.com/trains/${slug(a)}/${slug(b)}` });
  if (has("bus")) links.push({ mode: "bus", route: routeKey, from: a, to: b, provider: "Omio", label: `${a} → ${b}: buses (Omio)`, url: `https://www.omio.com/buses/${slug(a)}/${slug(b)}` });
  if (has("flight")) {
    const when = isIso(date) ? ` on ${date}` : "";
    links.push({ mode: "flight", route: routeKey, from: a, to: b, provider: "Google Flights", label: `${a} → ${b}: flights (Google Flights)`, url: `https://www.google.com/travel/flights?q=${encodeURIComponent(`Flights from ${a} to ${b}${when}`)}` });
  }
  const day = isIso(date) ? date : iso(new Date(+now + DAY_MS));   // no date given: show tomorrow
  const tt = want.length === 1 && want[0] === "flight" ? null : await timetable(a, b, day, { fetchImpl, cache });
  const result = {
    linksShownToTraveller: links.map((l) => l.label),
    links,
    note: "Booking links are shown to the traveller as buttons under your reply; do NOT write any URL yourself. " +
      "You have NO prices and no flight times. ",
  };
  if (tt) {
    result.timetable = { ...tt, route: routeKey };
    result.timetableNote = `REAL scheduled departures on ${tt.date} (local times), shown to the traveller as cards under your reply. ` +
      "Use them: recommend one by the traveller's priorities and quote its times; when you fill a leg, use that option's " +
      "departure and arrival as the leg's time and end_time. Do not copy the whole list into your text. Times can change, " +
      "so say the traveller should confirm on the booking site, where the prices are. The list may not include every operator.";
  } else {
    result.note += "No timetable is available for this route, so describe the options only in general terms (typical modes, " +
      "rough duration, whether a change is usual), say they are approximate, and point to the linked sites for times and prices.";
  }
  return result;
}

const DATA_TOOLS = [
  {
    name: "get_weather",
    description:
      "Get weather for a city. Uses a live forecast when the dates are within ~15 days, otherwise last year's " +
      "actuals for the same dates as a climate guide. Call this before answering any weather question and when " +
      "planning around the weather.",
    input_schema: {
      type: "object",
      properties: {
        city: { type: "string", description: "City name, e.g. 'London'." },
        start_date: { type: "string", description: "ISO date YYYY-MM-DD. Defaults to today." },
        end_date: { type: "string", description: "ISO date YYYY-MM-DD. Defaults to a week after start." },
      },
      required: ["city"],
    },
  },
  {
    name: "transport_options",
    description:
      "Get comparison/booking links for travelling between two cities (train, bus, flight). Call it whenever the " +
      "traveller asks how to get somewhere or which transport to take, and when choosing the transport for a leg. " +
      "It returns real scheduled train/bus options (times, changes, operators) where available, plus booking links; never prices.",
    input_schema: {
      type: "object",
      properties: {
        from: { type: "string", description: "Departure city, e.g. 'Toulouse'." },
        to: { type: "string", description: "Arrival city, e.g. 'Grenoble'." },
        date: { type: "string", description: "ISO date YYYY-MM-DD of travel, if known." },
        modes: { type: "array", items: { type: "string", enum: TRANSPORT_MODES }, description: "Only these modes; omit for all." },
      },
      required: ["from", "to"],
    },
  },
  {
    name: "search_places",
    description:
      "Search real places (restaurants, cafes, attractions, museums, markets) with ratings, price level and " +
      "coordinates. Use it to ground every food recommendation and named venue in an itinerary, instead of " +
      "relying on memory.",
    input_schema: {
      type: "object",
      properties: {
        query: { type: "string", description: "What to find, e.g. 'best traditional fish and chips', 'vegetarian restaurants', 'art museums'." },
        city: { type: "string", description: "City or area to search in." },
        max_results: { type: "integer", description: "1-8, default 6." },
      },
      required: ["query", "city"],
    },
  },
];

module.exports = { DATA_TOOLS, getWeather, searchPlaces, transportOptions };
