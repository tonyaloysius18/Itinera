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
    // 1) most specific: last leg's destination
    val lastDest = trip.legs.lastOrNull()?.toCity?.takeIf { it.isNotBlank() && it != "—" }
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