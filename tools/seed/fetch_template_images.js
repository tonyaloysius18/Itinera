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
  tpl_tbilisi_kazbegi_5d: "Tbilisi Georgia old town Narikala",
  tpl_georgia_wine_batumi_6d: "Georgia Kakheti vineyard Caucasus",
  tpl_andorra_4d: "Andorra Pyrenees mountains Grandvalira",
  tpl_transylvania_6d: "Transylvania Bran Castle Romania",
  tpl_krakow_4d: "Krakow Main Market Square Poland",
  tpl_warsaw_gdansk_5d: "Gdansk Poland Long Market waterfront",
  tpl_zakopane_tatras_4d: "Zakopane Tatra mountains Poland",
  tpl_eger_balaton_4d: "Lake Balaton Hungary Tihany",
  tpl_croatia_summer_islands_7d: "Hvar Croatia harbour",
  tpl_istria_plitvice_5d: "Plitvice Lakes Croatia waterfalls",
  tpl_florence_tuscany_5d: "Florence Duomo Tuscany",
  tpl_cinque_terre_4d: "Cinque Terre Manarola Italy",
  tpl_puglia_6d: "Alberobello trulli Puglia Italy",
  tpl_sardinia_6d: "Sardinia Cala Goloritze beach Italy",
  tpl_bologna_emilia_4d: "Bologna Piazza Maggiore Italy food",
  tpl_albania_riviera_6d: "Albanian Riviera Ksamil beach",
  tpl_holland_tulips_4d: "Keukenhof tulips Netherlands",
  tpl_brussels_bruges_ghent_5d: "Bruges Belgium canal medieval",
  tpl_london_weekend_3d: "London Tower Bridge",
  tpl_manchester_liverpool_4d: "Liverpool Royal Albert Dock",
  tpl_bath_cotswolds_oxford_4d: "Cotswolds Bibury Arlington Row England",
  tpl_cornwall_5d: "Cornwall St Ives coast England",
  tpl_edinburgh_glasgow_4d: "Glasgow Kelvingrove Scotland",
  tpl_scotland_nc500_7d: "North Coast 500 Scotland Highlands road",
  tpl_dublin_wicklow_4d: "Dublin Ireland Temple Bar",
  tpl_wild_atlantic_way_6d: "Cliffs of Moher Ireland",
  tpl_belfast_causeway_3d: "Giants Causeway Northern Ireland",
  tpl_faroe_islands_5d: "Faroe Islands Gasadalur waterfall",
  tpl_greenland_ilulissat_6d: "Ilulissat icefjord Greenland iceberg",
  tpl_iceland_golden_circle_4d: "Gullfoss waterfall Iceland Golden Circle",
  tpl_iceland_south_coast_4d: "Skogafoss Iceland south coast",
  tpl_iceland_winter_aurora_5d: "Iceland northern lights ice cave winter",
  tpl_nyc_5d: "New York City Manhattan skyline",
  tpl_new_england_fall_6d: "New England fall foliage Vermont",
  tpl_dc_philly_4d: "Washington DC Lincoln Memorial",
  tpl_miami_keys_5d: "Miami South Beach Florida Keys",
  tpl_new_orleans_nashville_6d: "New Orleans French Quarter jazz",
  tpl_vegas_grand_canyon_5d: "Grand Canyon South Rim Arizona",
  tpl_utah_mighty_five_7d: "Arches National Park Utah",
  tpl_pacific_coast_highway_7d: "Big Sur Bixby Bridge California coast",
  tpl_hawaii_oahu_maui_7d: "Maui Hawaii Road to Hana beach",
  tpl_alaska_7d: "Alaska Denali glacier",
  tpl_yellowstone_tetons_6d: "Yellowstone Grand Prismatic Spring",
  tpl_vancouver_whistler_5d: "Vancouver Stanley Park skyline",
  tpl_toronto_niagara_4d: "Niagara Falls Canada",
  tpl_montreal_quebec_5d: "Quebec City Chateau Frontenac",
  tpl_nova_scotia_cabot_6d: "Peggys Cove lighthouse Nova Scotia",
  tpl_jasper_icefields_5d: "Jasper Maligne Lake Canada Rockies",
  tpl_mexico_city_5d: "Mexico City Zocalo cathedral",
  tpl_yucatan_riviera_maya_6d: "Tulum Mexico beach ruins",
  tpl_costa_rica_7d: "Arenal Volcano Costa Rica",
  tpl_buenos_aires_4d: "Buenos Aires Caminito La Boca",
  tpl_patagonia_8d: "Torres del Paine Patagonia Chile",
  tpl_rio_5d: "Rio de Janeiro Christ Redeemer Sugarloaf",
  tpl_colombia_8d: "Cartagena Colombia colourful old town",
  tpl_galapagos_quito_7d: "Galapagos sea lion tortoise Ecuador",
  tpl_chile_atacama_santiago_7d: "San Pedro de Atacama desert Chile",
  tpl_uyuni_salt_flats_4d: "Salar de Uyuni Bolivia mirror",
  tpl_iguazu_falls_3d: "Iguazu Falls waterfall Argentina Brazil",
  tpl_masai_mara_nairobi_6d: "Masai Mara lion safari Kenya",
  tpl_serengeti_ngorongoro_7d: "Serengeti Tanzania safari plains",
  tpl_zanzibar_6d: "Zanzibar beach turquoise Tanzania",
  tpl_cape_town_garden_route_8d: "Cape Town Table Mountain South Africa",
  tpl_kruger_safari_6d: "Kruger National Park elephant South Africa",
  tpl_victoria_falls_4d: "Victoria Falls Zimbabwe Zambia",
  tpl_cairo_nile_7d: "Pyramids of Giza Egypt Sphinx",
  tpl_marrakech_4d: "Marrakech medina souk Morocco",
  tpl_namibia_8d: "Sossusvlei dunes Namibia Deadvlei",
  tpl_botswana_okavango_6d: "Okavango Delta Botswana safari",
  tpl_uganda_gorillas_5d: "Mountain gorilla Bwindi Uganda",
  tpl_mauritius_6d: "Mauritius Le Morne beach lagoon",
  tpl_golden_triangle_5d: "Taj Mahal Agra India",
  tpl_kerala_backwaters_6d: "Kerala backwaters houseboat India",
  tpl_goa_5d: "Goa beach India palm",
  tpl_mumbai_4d: "Mumbai Gateway of India",
  tpl_varanasi_khajuraho_5d: "Varanasi Ganges ghats India",
  tpl_himachal_6d: "Manali Solang Valley Himachal India",
  tpl_ladakh_7d: "Pangong Lake Ladakh India",
  tpl_kashmir_5d: "Dal Lake Srinagar Kashmir shikara",
  tpl_rishikesh_haridwar_4d: "Rishikesh Laxman Jhula Ganges India",
  tpl_tamil_nadu_temples_7d: "Meenakshi Temple Madurai India",
  tpl_karnataka_hampi_coorg_7d: "Hampi ruins Karnataka India",
  tpl_darjeeling_sikkim_6d: "Darjeeling Kangchenjunga tea garden India",
  tpl_ooty_munnar_western_ghats_5d: "Ooty Nilgiri tea plantation India",
  tpl_sri_lanka_cultural_triangle_7d: "Sigiriya Lion Rock Sri Lanka",
  tpl_sri_lanka_south_coast_5d: "Galle Fort lighthouse Sri Lanka",
  tpl_ella_hill_country_4d: "Nine Arch Bridge Ella Sri Lanka train",
  tpl_andaman_6d: "Radhanagar Beach Havelock Andaman",
  tpl_lakshadweep_5d: "Lakshadweep Agatti lagoon India",
  tpl_bhutan_paro_thimphu_6d: "Tigers Nest Paro Taktsang Bhutan",
  tpl_kathmandu_pokhara_6d: "Kathmandu Boudhanath Stupa Nepal",
  tpl_everest_base_camp_12d: "Everest Base Camp trek Nepal Himalaya",
  tpl_laos_luang_prabang_6d: "Kuang Si Falls Luang Prabang Laos",
  tpl_siem_reap_angkor_4d: "Angkor Wat sunrise Cambodia",
  tpl_phnom_penh_kampot_5d: "Phnom Penh Royal Palace Cambodia",
  tpl_hanoi_halong_sapa_6d: "Ha Long Bay Vietnam karst cruise",
  tpl_central_vietnam_5d: "Hoi An lanterns Vietnam old town",
  tpl_saigon_mekong_4d: "Ho Chi Minh City Saigon skyline",
  tpl_bangkok_4d: "Bangkok Wat Arun temple Thailand",
  tpl_chiang_mai_chiang_rai_5d: "Chiang Mai Doi Suthep Thailand temple",
  tpl_phuket_phi_phi_6d: "Phi Phi Islands Maya Bay Thailand",
  tpl_koh_samui_phangan_tao_6d: "Koh Samui beach Thailand island",
  tpl_krabi_railay_5d: "Railay Beach Krabi Thailand",
  tpl_myanmar_bagan_inle_7d: "Bagan temples Myanmar balloons",
  tpl_kl_penang_5d: "Petronas Towers Kuala Lumpur Malaysia",
  tpl_langkawi_4d: "Langkawi Sky Bridge Malaysia",
  tpl_sabah_borneo_6d: "Mount Kinabalu Sabah Borneo",
  tpl_singapore_4d: "Gardens by the Bay Singapore Supertrees",
  tpl_palawan_el_nido_coron_7d: "El Nido Palawan Philippines lagoon",
  tpl_cebu_bohol_6d: "Chocolate Hills Bohol Philippines",
  tpl_java_yogyakarta_bromo_6d: "Mount Bromo Java Indonesia sunrise",
  tpl_lombok_gili_5d: "Gili Trawangan Lombok Indonesia",
  tpl_komodo_flores_5d: "Padar Island Komodo Indonesia",
  tpl_hong_kong_4d: "Hong Kong Victoria Harbour skyline",
  tpl_brunei_3d: "Sultan Omar Ali Saifuddien Mosque Brunei",
  tpl_macau_3d: "Macau Ruins of St Pauls",
  tpl_fiji_islands_7d: "Fiji Mamanuca beach turquoise",
  tpl_crete_7d: "Elafonisi pink beach Crete Greece",
  tpl_cyclades_mykonos_naxos_6d: "Mykonos windmills Little Venice Greece",
  tpl_corfu_ionian_5d: "Corfu Paleokastritsa Greece bay",
  tpl_meteora_delphi_4d: "Meteora monasteries Greece sunrise",
  tpl_peloponnese_5d: "Nafplio Palamidi Peloponnese Greece",
  tpl_oaxaca_5d: "Oaxaca Mexico Santo Domingo",
  tpl_san_miguel_guanajuato_5d: "Guanajuato colourful Mexico",
  tpl_los_cabos_baja_5d: "Cabo San Lucas El Arco Mexico",
  tpl_chiapas_palenque_6d: "Palenque Maya ruins Mexico",
  tpl_jamaica_6d: "Negril Seven Mile Beach Jamaica",
  tpl_dominican_republic_6d: "Punta Cana beach Dominican Republic palms",
  tpl_puerto_rico_5d: "Old San Juan Puerto Rico colourful",
  tpl_bahamas_exumas_5d: "Exuma swimming pigs Bahamas",
  tpl_st_lucia_6d: "Pitons Saint Lucia Caribbean",
  tpl_cuba_havana_vinales_6d: "Havana Cuba classic cars",
  tpl_dubai_abu_dhabi_5d: "Dubai Burj Khalifa skyline",
  tpl_istanbul_5d: "Istanbul Hagia Sophia Bosphorus",
  tpl_cappadocia_4d: "Cappadocia hot air balloons Goreme",
  tpl_jordan_petra_wadi_rum_5d: "Petra Treasury Jordan",
  tpl_oman_muscat_wahiba_6d: "Muscat Grand Mosque Oman",
  tpl_taiwan_taipei_6d: "Taipei 101 Taiwan skyline",
  tpl_okinawa_5d: "Okinawa Japan beach turquoise",
  tpl_uzbekistan_silk_road_7d: "Registan Samarkand Uzbekistan",
  tpl_armenia_yerevan_5d: "Khor Virap Ararat Armenia",
  tpl_swiss_alps_zermatt_lucerne_6d: "Matterhorn Zermatt Switzerland",
  tpl_stockholm_4d: "Stockholm Gamla Stan Sweden",
  tpl_copenhagen_4d: "Nyhavn Copenhagen Denmark",
  tpl_helsinki_tallinn_4d: "Tallinn Old Town Estonia rooftops",
  tpl_baltics_riga_vilnius_6d: "Riga Old Town Latvia",
  tpl_slovenia_ljubljana_bled_5d: "Lake Bled Slovenia island church",
  tpl_montenegro_kotor_4d: "Kotor Bay Montenegro",
  tpl_sarajevo_mostar_4d: "Mostar Old Bridge Bosnia",
  tpl_malta_5d: "Valletta Malta Grand Harbour",
  tpl_cyprus_5d: "Cyprus Aphrodite Rock Petra tou Romiou",
  tpl_provence_lavender_5d: "Lavender fields Provence Senanque Abbey",
  tpl_french_riviera_5d: "Nice Promenade des Anglais French Riviera",
  tpl_normandy_mont_saint_michel_4d: "Mont-Saint-Michel Normandy France",
  tpl_loire_valley_chateaux_4d: "Chateau Chambord Loire Valley",
  tpl_rhine_black_forest_5d: "Rhine Valley castle Germany Loreley",
  tpl_lake_district_4d: "Lake District Windermere Cumbria",
  tpl_wales_snowdonia_cardiff_4d: "Snowdonia Wales Snowdon",
  tpl_uluru_red_centre_5d: "Uluru Ayers Rock sunset Australia",
  tpl_tasmania_6d: "Wineglass Bay Tasmania Freycinet",
  tpl_los_angeles_5d: "Los Angeles Hollywood sign Griffith Observatory",
  tpl_san_francisco_yosemite_6d: "Yosemite Valley Tunnel View",
  tpl_chicago_4d: "Chicago skyline Cloud Gate",
  tpl_guatemala_antigua_tikal_6d: "Antigua Guatemala volcano arch",
  tpl_belize_5d: "Great Blue Hole Belize aerial",
  tpl_kilimanjaro_machame_8d: "Kilimanjaro summit Tanzania",
  tpl_lima_peru_food_4d: "Lima Miraflores Peru coastline",
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
    let url;
    try {
      url = await searchUnsplash(key, query);
    } catch (err) {
      // Unsplash's demo key allows ~50 requests/hour. Keep what we have so far; re-run later to resume.
      console.error(String(err.message).slice(0, 120));
      console.error("Stopping early — progress saved. Re-run after the rate limit resets.");
      break;
    }
    if (url) {
      template.coverImageUrl = url;
      console.log(`${template.id}: ${url.slice(0, 70)}`);
      fs.writeFileSync(TEMPLATES_PATH, JSON.stringify(templates, null, 2) + "\n");
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
