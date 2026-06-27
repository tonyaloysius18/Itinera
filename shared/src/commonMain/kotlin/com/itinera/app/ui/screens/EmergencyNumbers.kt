package com.itinera.app.ui.screens

/**
 * Emergency phone numbers per country (ISO 3166-1 alpha-2 code).
 *
 * `general` is the single number to dial for any emergency where one exists
 * (e.g. 112 in the EU, 911 in North America). police/ambulance/fire are the
 * specific services; they're shown when they differ from the general number.
 *
 * Curated for major / commonly-visited countries. Not exhaustive — extend freely.
 * Source: public emergency-number references. Always verify locally; numbers can
 * change and this is a convenience, not an authoritative source.
 */
data class EmergencyInfo(
    val countryName: String,
    val general: String,        // "" if the country has no single general number
    val police: String,
    val ambulance: String,
    val fire: String,
)

val emergencyNumbers: Map<String, EmergencyInfo> = mapOf(
    // ── Europe (mostly 112 general) ──
    "FR" to EmergencyInfo("France", "112", "17", "15", "18"),
    "GB" to EmergencyInfo("United Kingdom", "999", "999", "999", "999"),
    "IE" to EmergencyInfo("Ireland", "112", "112", "112", "112"),
    "DE" to EmergencyInfo("Germany", "112", "110", "112", "112"),
    "ES" to EmergencyInfo("Spain", "112", "091", "061", "080"),
    "IT" to EmergencyInfo("Italy", "112", "113", "118", "115"),
    "PT" to EmergencyInfo("Portugal", "112", "112", "112", "112"),
    "NL" to EmergencyInfo("Netherlands", "112", "112", "112", "112"),
    "BE" to EmergencyInfo("Belgium", "112", "101", "112", "112"),
    "CH" to EmergencyInfo("Switzerland", "112", "117", "144", "118"),
    "AT" to EmergencyInfo("Austria", "112", "133", "144", "122"),
    "SE" to EmergencyInfo("Sweden", "112", "112", "112", "112"),
    "NO" to EmergencyInfo("Norway", "112", "112", "113", "110"),
    "DK" to EmergencyInfo("Denmark", "112", "114", "112", "112"),
    "FI" to EmergencyInfo("Finland", "112", "112", "112", "112"),
    "PL" to EmergencyInfo("Poland", "112", "997", "999", "998"),
    "CZ" to EmergencyInfo("Czechia", "112", "158", "155", "150"),
    "GR" to EmergencyInfo("Greece", "112", "100", "166", "199"),
    "HU" to EmergencyInfo("Hungary", "112", "107", "104", "105"),
    "RO" to EmergencyInfo("Romania", "112", "112", "112", "112"),
    "TR" to EmergencyInfo("Turkey", "112", "112", "112", "112"),
    "RU" to EmergencyInfo("Russia", "112", "102", "103", "101"),

    // ── North America (911) ──
    "US" to EmergencyInfo("United States", "911", "911", "911", "911"),
    "CA" to EmergencyInfo("Canada", "911", "911", "911", "911"),
    "MX" to EmergencyInfo("Mexico", "911", "911", "911", "911"),

    // ── Asia ──
    "IN" to EmergencyInfo("India", "112", "100", "102", "101"),
    "CN" to EmergencyInfo("China", "", "110", "120", "119"),
    "JP" to EmergencyInfo("Japan", "", "110", "119", "119"),
    "KR" to EmergencyInfo("South Korea", "112", "112", "119", "119"),
    "SG" to EmergencyInfo("Singapore", "999", "999", "995", "995"),
    "MY" to EmergencyInfo("Malaysia", "999", "999", "999", "994"),
    "TH" to EmergencyInfo("Thailand", "191", "191", "1669", "199"),
    "ID" to EmergencyInfo("Indonesia", "112", "110", "118", "113"),
    "PH" to EmergencyInfo("Philippines", "911", "911", "911", "911"),
    "VN" to EmergencyInfo("Vietnam", "", "113", "115", "114"),
    "AE" to EmergencyInfo("United Arab Emirates", "999", "999", "998", "997"),
    "SA" to EmergencyInfo("Saudi Arabia", "911", "999", "997", "998"),
    "IL" to EmergencyInfo("Israel", "112", "100", "101", "102"),
    "LK" to EmergencyInfo("Sri Lanka", "119", "119", "1990", "110"),
    "NP" to EmergencyInfo("Nepal", "112", "100", "102", "101"),
    "PK" to EmergencyInfo("Pakistan", "15", "15", "1122", "16"),
    "BD" to EmergencyInfo("Bangladesh", "999", "999", "999", "999"),
    "HK" to EmergencyInfo("Hong Kong", "999", "999", "999", "999"),
    "TW" to EmergencyInfo("Taiwan", "", "110", "119", "119"),

    // ── Oceania ──
    "AU" to EmergencyInfo("Australia", "000", "000", "000", "000"),
    "NZ" to EmergencyInfo("New Zealand", "111", "111", "111", "111"),

    // ── Africa ──
    "ZA" to EmergencyInfo("South Africa", "112", "10111", "10177", "10177"),
    "EG" to EmergencyInfo("Egypt", "", "122", "123", "180"),
    "MA" to EmergencyInfo("Morocco", "", "190", "150", "150"),
    "KE" to EmergencyInfo("Kenya", "999", "999", "999", "999"),
    "NG" to EmergencyInfo("Nigeria", "112", "112", "112", "112"),

    // ── South America ──
    "BR" to EmergencyInfo("Brazil", "190", "190", "192", "193"),
    "AR" to EmergencyInfo("Argentina", "911", "911", "107", "100"),
    "CL" to EmergencyInfo("Chile", "", "133", "131", "132"),
    "CO" to EmergencyInfo("Colombia", "123", "123", "123", "123"),
    "PE" to EmergencyInfo("Peru", "105", "105", "106", "116"),
)