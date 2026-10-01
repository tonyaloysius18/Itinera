package com.itinera.app.data

import com.itinera.app.model.Trip

private val countryCapitals = mapOf(
    "france" to "Paris", "germany" to "Berlin", "italy" to "Rome", "spain" to "Madrid",
    "portugal" to "Lisbon", "japan" to "Tokyo", "china" to "Beijing", "india" to "New Delhi",
    "england" to "London", "uk" to "London", "united kingdom" to "London",
    "usa" to "Washington", "united states" to "Washington", "netherlands" to "Amsterdam",
    "greece" to "Athens", "egypt" to "Cairo", "turkey" to "Istanbul", "russia" to "Moscow",
    "austria" to "Vienna", "belgium" to "Brussels", "switzerland" to "Bern",
    "ireland" to "Dublin", "poland" to "Warsaw", "sweden" to "Stockholm",
    "norway" to "Oslo", "denmark" to "Copenhagen", "thailand" to "Bangkok",
    "brazil" to "Rio de Janeiro", "argentina" to "Buenos Aires", "mexico" to "Mexico City",
    "australia" to "Sydney", "canada" to "Toronto", "morocco" to "Marrakech",
    "croatia" to "Dubrovnik", "hungary" to "Budapest", "czechia" to "Prague",
    "czech republic" to "Prague",
)

private val fillerWords = setOf(
    "trip", "trips", "holiday", "holidays", "vacation", "tour", "loop", "by", "rail",
    "my", "the", "a", "to", "in", "and", "with", "getaway", "adventure", "road",
)

/** Decide the Unsplash search term for a trip. */
fun imageQueryForTrip(trip: Trip): String {
    // 1) most specific: last leg's destination, ignoring legs that head back to the starting city (the return
    //    journey home must not make the card show the origin instead of where the traveller is going)
    val home = trip.legs.firstOrNull()?.fromCity?.trim()
    val lastDest = trip.legs.map { it.toCity.trim() }
        .lastOrNull { it.isNotBlank() && it != "—" && !it.equals(home, ignoreCase = true) }
    if (lastDest != null) return lastDest

    // 2) clean the trip name of filler words
    val cleaned = trip.title
        .split(" ")
        .map { it.trim().lowercase() }
        .filter { it.isNotBlank() && it !in fillerWords }
        .joinToString(" ")
        .ifBlank { trip.title }   // if everything got stripped, fall back to raw title

    // 3) country → capital
    countryCapitals[cleaned]?.let { return it }

    // 4) otherwise the cleaned name itself
    return cleaned
}

private val greetingFillerWords = fillerWords + setOf("day", "days", "weekend", "week", "weeks", "at", "on", "&")

/**
 * A personalized-Nera-greeting destination that matches whatever place name is actually shown
 * on this trip's card — the trip title is the card's heading, so whatever's mentioned there
 * (a city, a region, a country — "London", "Rajasthan", "Italy") is what's returned, exactly as
 * written. Only falls back to leg/country data when the title yields nothing usable (e.g. a
 * blank or fully-generic title).
 *
 * A title often has more than the place in it ("London Summer", "Paris Anniversary Trip"). When
 * more than one word survives the filler-word strip, [activityLocations] (each place's "Area,
 * City" string) and the trip's own leg cities are used to pick out just the word(s) that are
 * actually a real place mentioned elsewhere on the trip, dropping modifiers like "Summer" that
 * aren't filler words but also aren't part of the destination name.
 */
fun greetingDestinationForTrip(trip: Trip, activityLocations: List<String> = emptyList()): String {
    val cleanedTokens = trip.title
        .split(" ")
        .map { it.trim().trim(':', ',', '&') }
        .filter { it.isNotBlank() && !it.first().isDigit() && it.lowercase() !in greetingFillerWords }

    if (cleanedTokens.size > 1) {
        val knownPlaces = buildSet {
            trip.legs.forEach { leg ->
                add(leg.fromCity.trim().lowercase())
                add(leg.toCity.trim().lowercase())
                leg.stops.forEach { add(it.city.trim().lowercase()) }
            }
            activityLocations.forEach { loc ->
                loc.split(",").forEach { part -> add(part.trim().lowercase()) }
            }
        }
        val matched = cleanedTokens.filter { it.lowercase() in knownPlaces }
        if (matched.isNotEmpty()) return matched.joinToString(" ")
    }
    if (cleanedTokens.isNotEmpty()) return cleanedTokens.joinToString(" ")

    val home = trip.legs.firstOrNull()?.fromCity?.trim()
    trip.legs.lastOrNull { it.toCity.trim().isNotBlank() && it.toCity.trim() != "—" && !it.toCity.trim().equals(home, ignoreCase = true) }
        ?.toCity?.trim()?.takeIf { it.isNotBlank() }?.let { return it }

    return trip.destinationCountries.firstOrNull { it.isNotBlank() }?.trim().orEmpty()
}