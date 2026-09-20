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

module.exports = { DATA_TOOLS, getWeather, searchPlaces };
