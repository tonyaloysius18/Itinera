package com.itinera.app.data

import com.itinera.app.model.Trip
import com.itinera.app.model.TransportType
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.daysUntil

/** A suggested packing item with the reason it's suggested (shown to the user). */
data class PackingSuggestion(
    val text: String,
    val group: String,     // localized group name, passed in
    val reason: String,     // short "why", e.g. "Rain expected in Munich"
)

/** Localized group labels, so the engine stays free of string resources. */
data class PackingGroups(
    val documents: String,
    val packing: String,
    val transport: String,
    val money: String,
    val gadget: String,
    val other: String,
)

/** Coarse weather signal for a destination over the trip dates. */
data class DestinationWeather(
    val city: String,
    val maxTempC: Double?,     // typical/forecast daytime high
    val minTempC: Double?,     // typical/forecast night low
    val rainLikely: Boolean,    // meaningful precipitation expected
    val isForecast: Boolean,    // true = real forecast, false = seasonal normal
)

/**
 * Rule-based packing suggestions from trip facts + optional weather.
 * Deterministic, no network here — the caller supplies weather (may be empty).
 * Returns suggestions NOT already present in [existingTexts] (case-insensitive).
 */
fun packingSuggestions(
    trip: Trip,
    weather: List<DestinationWeather>,
    groups: PackingGroups,
    existingTexts: Set<String>,
): List<PackingSuggestion> {
    val out = mutableListOf<PackingSuggestion>()
    val have = existingTexts.map { it.trim().lowercase() }.toSet()
    fun add(text: String, group: String, reason: String) {
        if (text.trim().lowercase() !in have) out += PackingSuggestion(text, group, reason)
    }

    val dates = trip.legs.map { it.date }.sorted()
    val days = if (dates.isEmpty()) 0 else dates.first().daysUntil(dates.last()) + 1
    val transports = trip.legs.map { it.transport }.toSet()
    val flying = TransportType.FLIGHT in transports

    // ── Essentials (always) ──
    add("Passport / ID", groups.documents, "Every trip")
    add("Phone charger", groups.gadget, "Every trip")
    add("Power bank", groups.gadget, "Long travel days")

    // ── International → adapter (any leg country differs from the first) ──
    val countries = trip.legs.mapNotNull { it.country.trim().ifBlank { null } }.distinct()
    if (countries.size > 1 || (countries.isNotEmpty() && flying)) {
        add("Travel plug adapter", groups.gadget, "Crossing countries")
        add("Local currency / card", groups.money, "Spending abroad")
    }

    // ── Trip length → clothing volume ──
    when {
        days >= 7 -> add("Outfits for ${days} days", groups.packing, "$days-day trip")
        days in 1..6 -> add("Outfits for ${days} days", groups.packing, "$days-day trip")
    }
    if (days >= 4) add("Laundry bag", groups.packing, "Longer trip")
    add("Toiletries", groups.packing, "Every trip")

    // ── Flight-specific ──
    if (flying) {
        add("Liquids ≤ 100ml (carry-on)", groups.transport, "Flight security limit")
        add("Boarding passes ready", groups.documents, "Flying")
    }

    // ── Weather-driven ──
    val anyRain = weather.any { it.rainLikely }
    if (anyRain) {
        val where = weather.firstOrNull { it.rainLikely }?.city
        add("Umbrella / rain jacket", groups.packing,
            if (where != null) "Rain expected in $where" else "Rain expected")
    }
    val coldest = weather.mapNotNull { it.minTempC }.minOrNull()
    val hottest = weather.mapNotNull { it.maxTempC }.maxOrNull()
    if (coldest != null && coldest < 8) {
        add("Warm layers / jacket", groups.packing, "Cold nights (${coldest.toInt()}°C)")
    }
    if (hottest != null && hottest >= 26) {
        add("Sunscreen", groups.packing, "Hot days (${hottest.toInt()}°C)")
        add("Sunglasses", groups.packing, "Sunny & hot")
        add("Reusable water bottle", groups.packing, "Staying hydrated")
    }

    return out
}

/** How far out the trip starts — used to pick forecast vs seasonal weather. */
fun tripStartsInDays(trip: Trip): Int {
    val first = trip.legs.map { it.date }.minOrNull() ?: return Int.MAX_VALUE
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return today.daysUntil(first)
}