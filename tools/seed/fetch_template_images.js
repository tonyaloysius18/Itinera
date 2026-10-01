// One-off script: looks up a cover photo per trip template on Unsplash (the
// same source and access key the app uses for real trip cover photos — see
// shared/src/commonMain/kotlin/com/itinera/app/data/UnsplashApi.kt) and writes
// the URL into each template's coverImageUrl in trip_templates.json.
//
// Usage: node tools/seed/fetch_template_images.js
// Reads the key from local.properties (unsplashAccessKey) — the same file the
// Gradle build reads it from. After running, re-run seed_trip_templates.js to
// push the updated coverImageUrl values to Firestore.

const fs = require("fs");
const path = require("path");

const ROOT = path.join(__dirname, "..", "..");
const TEMPLATES_PATH = path.join(__dirname, "trip_templates.json");

function readUnsplashKey() {
  const props = fs.readFileSync(path.join(ROOT, "local.properties"), "utf8");
  const line = props.split("\n").find((l) => l.trim().startsWith("unsplashAccessKey"));
  if (!line) throw new Error("unsplashAccessKey not found in local.properties");
  return line.split("=")[1].trim();
}

// A specific, hand-picked search term per template — more reliable than
// deriving one from the title/country programmatically (several templates
// share a country, e.g. six in Italy, and need distinct photos).
const QUERIES = {
  tpl_london_5d: "London Big Ben landmark",
  tpl_maldives_6d: "Maldives overwater bungalow",
  tpl_seychelles_6d: "Seychelles beach granite boulders",
  tpl_bali_7d: "Bali rice terrace",
  tpl_chamonix_5d: "Chamonix Mont Blanc",
  tpl_interlaken_4d: "Interlaken Switzerland lake mountains",
  tpl_banff_6d: "Banff Lake Louise Canada",
  tpl_morocco_sahara_5d: "Sahara desert dunes Morocco",
  tpl_rajasthan_7d: "Jaipur Amber Fort Rajasthan",
  tpl_lake_como_4d: "Lake Como Italy",
  tpl_venice_3d: "Venice canal Italy",
  tpl_rome_4d: "Rome Colosseum",
  tpl_sicily_6d: "Taormina Sicily coast",
  tpl_amalfi_coast_5d: "Amalfi Coast Positano",
  tpl_dolomites_5d: "Dolomites mountains Italy",
  tpl_sydney_reef_7d: "Sydney Opera House harbour",
  tpl_peru_machu_picchu_7d: "Machu Picchu Peru",
  tpl_tokyo_6d: "Tokyo Shibuya crossing night",
  tpl_kyoto_osaka_5d: "Kyoto Fushimi Inari torii gates",
  tpl_hokkaido_winter_5d: "Hokkaido Niseko snow winter",
  tpl_beijing_5d: "Beijing Forbidden City",
  tpl_shanghai_hangzhou_4d: "Shanghai Bund skyline",
  tpl_seoul_5d: "Seoul Gyeongbokgung palace",
  tpl_busan_jeju_5d: "Jeju island Seongsan Ilchulbong",
  tpl_melbourne_gor_5d: "Twelve Apostles Great Ocean Road",
  tpl_queenstown_milford_5d: "Queenstown Lake Wakatipu mountains",
  tpl_nz_north_island_6d: "Hobbiton New Zealand Matamata",
  tpl_bavaria_christmas_5d: "Nuremberg Christmas market",
  tpl_berlin_dresden_christmas_5d: "Berlin Brandenburg Gate winter",
  tpl_vienna_salzburg_christmas_5d: "Vienna Christmas market Rathaus",
  tpl_innsbruck_tyrol_4d: "Innsbruck Tyrol winter alps",
  tpl_rovaniemi_lapland_4d: "Rovaniemi Lapland snow Santa Claus Village",
  tpl_lapland_glass_igloo_5d: "Lapland glass igloo northern lights",
  tpl_tromso_aurora_4d: "Tromso northern lights aurora Norway",
  tpl_lofoten_5d: "Lofoten islands Reine Norway",
  tpl_bergen_fjords_5d: "Norway fjord Flam Naeroyfjord",
  tpl_lisbon_sintra_4d: "Lisbon tram Alfama Portugal",
  tpl_porto_douro_4d: "Porto Ribeira Douro river Portugal",
  tpl_algarve_5d: "Algarve Benagil cave Portugal beach",
  tpl_madeira_6d: "Madeira Funchal",
  tpl_azores_sao_miguel_6d: "Azores Sao Miguel Sete Cidades lake",
  tpl_madrid_toledo_4d: "Madrid Gran Via Spain",
  tpl_barcelona_4d: "Barcelona Sagrada Familia Spain",
  tpl_andalusia_5d: "Alhambra Granada Spain",
  tpl_mallorca_5d: "Mallorca Cala cove Spain",
  tpl_tenerife_5d: "Tenerife Teide volcano Canary Islands",
  tpl_ibiza_formentera_5d: "Ibiza cove turquoise Spain beach",
  tpl_paris_5d: "Paris Eiffel Tower",
  tpl_amsterdam_4d: "Amsterdam canals Netherlands",
  tpl_prague_4d: "Prague Charles Bridge Czech Republic",
  tpl_budapest_4d: "Budapest Parliament Danube Hungary",
  tpl_athens_santorini_6d: "Santorini Oia blue domes Greece",
  tpl_croatia_coast_6d: "Dubrovnik Croatia old town coast",
  tpl_edinburgh_highlands_5d: "Edinburgh Castle Scotland",
  tpl_iceland_ring_road_7d: "Iceland Skogafoss waterfall ring road",
};

async function searchUnsplash(key, query) {
  const url = `https://api.unsplash.com/search/photos?query=${encodeURIComponent(query)}&per_page=1&orientation=landscape`;
  const res = await fetch(url, { headers: { Authorization: `Client-ID ${key}` } });
  if (!res.ok) throw new Error(`Unsplash ${res.status} for "${query}": ${await res.text()}`);
  const data = await res.json();
  return data.results?.[0]?.urls?.regular ?? null;
}

async function main() {
  const key = readUnsplashKey();
  const templates = JSON.parse(fs.readFileSync(TEMPLATES_PATH, "utf8"));

  for (const template of templates) {
    // Already has a cover — don't re-hit the API (and burn rate limit) for it.
    if (template.coverImageUrl) continue;
    const query = QUERIES[template.id];
    if (!query) {
      console.warn(`No query mapped for ${template.id}, skipping`);
      continue;
    }
    const url = await searchUnsplash(key, query);
    if (url) {
      template.coverImageUrl = url;
      console.log(`${template.id}: ${url}`);
    } else {
      console.warn(`${template.id}: no result for "${query}"`);
    }
    // Be polite to the API rather than firing dozens of requests at once.
    await new Promise((r) => setTimeout(r, 300));
  }

  fs.writeFileSync(TEMPLATES_PATH, JSON.stringify(templates, null, 2) + "\n");
  console.log("Updated trip_templates.json with cover images.");
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
