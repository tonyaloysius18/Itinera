package com.itinera.app.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.serialization.Serializable

/**
 * A trip made public by link: only the *places and common factors* of a trip, with everything personal stripped.
 * Lives in the top-level `sharedItineraries/{id}` collection; anyone holding the unguessable id can read it (the
 * hosted preview page and the in-app "open a link" flow both do), but nobody can list the collection.
 *
 * What is deliberately NOT here: dates (days are relative, Day 1..N), travellers, names, emails, notes, expenses,
 * documents, booking references, operators, journey times, who owns it, membership, completion state.
 */
@Serializable
data class SharedItinerary(
    val id: String,
    val title: String,
    val description: String = "",
    val countries: List<String> = emptyList(),
    val budgetTier: BudgetTier = BudgetTier.MID_RANGE,
    val paceTag: PaceTag = PaceTag.BALANCED,
    val coverImageUrl: String = "",
    val days: List<SharedDay> = emptyList(),
    val legs: List<SharedLeg> = emptyList(),
    val sharedAt: Long = 0L,
    // ── Community listing (opt-in) ──
    /** The owner asked for this to appear in the public Community feed. Link-only shares leave this false. */
    val listRequested: Boolean = false,
    /** Only set when listing, so people can report or block the author. No name or photo is ever stored. */
    val authorUid: String = "",
    // Moderator-controlled; clients can only ever create these at their defaults (see firestore.rules).
    /** "" = not reviewed yet, "approved", "rejected", "taken_down". */
    val status: String = "",
    /** True only while approved and listed; the feed queries on it. */
    val feed: Boolean = false,
    val approvedAt: Long = 0L,
    /** How many different travellers copied this into their trips (one per person; see firestore.rules). */
    val copyCount: Int = 0,
)

@Serializable
data class SharedDay(val dayNumber: Int, val stops: List<SharedStop> = emptyList())

@Serializable
data class SharedStop(
    val title: String,
    val time: String = "",
    val endTime: String = "",
    val location: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
)

/** A journey between two cities: the transport type and which day, nothing about tickets or times. */
@Serializable
data class SharedLeg(
    val dayNumber: Int,
    val fromCity: String,
    val toCity: String,
    val transport: TransportType,
)

/** The owner's private record of a link they created (under users/{uid}/sharedLinks), so they can find and revoke it. */
@Serializable
data class SharedLinkRecord(
    val id: String = "",
    val tripId: String = "",
    val title: String = "",
    val createdAt: Long = 0L,
)

/**
 * Recognises the owner's home city: the profile city when known, else the city a round trip starts and ends in.
 * Used to leave the outbound and return journeys (and the home country) out of what is shared.
 */
private fun homeMatcher(trip: Trip, homeCity: String): (String) -> Boolean {
    val sorted = trip.legs.sortedBy { it.date }
    val roundTripCity = sorted.firstOrNull()?.fromCity?.trim()
        ?.takeIf { it.isNotBlank() && it.equals(sorted.lastOrNull()?.toCity?.trim(), ignoreCase = true) }
    val homes = listOfNotNull(homeCity.trim().takeIf { it.isNotBlank() }, roundTripCity).map { it.lowercase() }
    return { city ->
        val c = city.trim().lowercase()
        homes.isNotEmpty() && c.length >= 3 && homes.any { h -> c == h || h.contains(c) || c.contains(h) }
    }
}

/** The trip's journeys minus the ones that start or end at the owner's home, so a shared itinerary never reveals where its owner lives. */
private fun tripLegsWithoutHome(trip: Trip, homeCity: String): List<Leg> {
    val isHome = homeMatcher(trip, homeCity)
    return trip.legs.filter { !isHome(it.fromCity) && !isHome(it.toCity) }
}

private val EMAIL = Regex("""[\w.+-]+@[\w-]+\.[\w.-]+""")
private val URL = Regex("""(https?://|www\.)\S+""", RegexOption.IGNORE_CASE)
private val PHONE = Regex("""\+?\d[\d\s().-]{7,}\d""")

/** Removes emails, web links and phone-number-like text from free text that is about to become public. */
fun scrubPersonal(text: String, maxLength: Int): String =
    text.replace(EMAIL, "").replace(URL, "").replace(PHONE, "")
        .replace(Regex("""\s{2,}"""), " ")
        .trim()
        .take(maxLength)

/**
 * Builds the shareable copy of a trip. [excluded] holds activity ids the owner removed in the preview.
 * Days are numbered relative to the first day with anything on it, so no real dates leave the device.
 */
fun buildSharedItinerary(
    id: String,
    trip: Trip,
    activities: List<Activity>,
    title: String,
    description: String,
    budgetTier: BudgetTier,
    paceTag: PaceTag,
    excluded: Set<String>,
    sharedAt: Long,
    homeCity: String = "",
    listRequested: Boolean = false,
    authorUid: String = "",
): SharedItinerary {
    val chosen = activities.filter { it.id !in excluded }
    val legs = tripLegsWithoutHome(trip, homeCity)
    val allDates = (chosen.map { it.date } + legs.map { it.date }).sorted()
    val first: LocalDate? = allDates.firstOrNull()
    fun dayOf(date: LocalDate): Int = first!!.daysUntil(date) + 1

    val stopsByDay = chosen.groupBy { dayOf(it.date) }
    val legsByDay = legs.map {
        SharedLeg(
            dayNumber = dayOf(it.date),
            fromCity = scrubPersonal(it.fromCity, 60),
            toCity = scrubPersonal(it.toCity, 60),
            transport = it.transport,
        )
    }
    val dayNumbers = (stopsByDay.keys + legsByDay.map { it.dayNumber }).distinct().sorted()

    val days = if (first == null) emptyList() else dayNumbers.take(60).map { n ->
        SharedDay(
            dayNumber = n,
            stops = (stopsByDay[n] ?: emptyList())
                .sortedBy { it.time.ifBlank { "99:99" } }
                .take(30)
                .map {
                    SharedStop(
                        title = scrubPersonal(it.title, 80),
                        time = it.time.take(5),
                        endTime = it.endTime.take(5),
                        location = scrubPersonal(it.location, 120),
                        lat = it.lat,
                        lng = it.lng,
                    )
                }
                .filter { it.title.isNotBlank() },
        )
    }

    // Countries come from every journey that does NOT end at home (so the destination of the outbound flight counts,
    // but the return flight's home country never does).
    val isHome = homeMatcher(trip, homeCity)
    val countries = (trip.destinationCountries + trip.legs.filter { !isHome(it.toCity) }.map { it.country })
        .map { scrubPersonal(it, 40) }.filter { it.isNotBlank() }.distinct().take(8)

    return SharedItinerary(
        id = id,
        title = scrubPersonal(title, 80).ifBlank { "Itinerary" },
        description = scrubPersonal(description, 300),
        countries = countries,
        budgetTier = budgetTier,
        paceTag = paceTag,
        // Only a stock cover is ever shared, never a user-uploaded photo.
        coverImageUrl = trip.imageUrl?.takeIf { it.startsWith("https://images.unsplash.com/") }.orEmpty(),
        days = days,
        legs = legsByDay.take(30),
        sharedAt = sharedAt,
        listRequested = listRequested,
        authorUid = if (listRequested) authorUid else "",
    )
}

/** Rough continent for the shared preview label, from the first stop with coordinates. */
private fun continentFor(lat: Double, lng: Double): Continent = when {
    lat < -10 && lng > 110 -> Continent.OCEANIA
    lat < -30 && lng > 165 -> Continent.OCEANIA
    lat < 13 && lng in -82.0..-34.0 -> Continent.SOUTH_AMERICA
    lng in -170.0..-50.0 -> Continent.NORTH_AMERICA
    lat in -35.0..37.0 && lng in -18.0..52.0 && !(lat > 12 && lng > 34) -> Continent.AFRICA
    lat > 35 && lng in -25.0..40.0 -> Continent.EUROPE
    lng > 25 -> Continent.ASIA
    else -> Continent.EUROPE
}

/** Lets the existing template detail screen (and "Use this template" / "Edit with Nera") work on a shared itinerary. */
fun SharedItinerary.toTemplate(): TripTemplate {
    val firstPoint = days.flatMap { it.stops }.firstOrNull { it.lat != 0.0 || it.lng != 0.0 }
    return TripTemplate(
        id = "shared_$id",
        title = title,
        shortDescription = description,
        coverImageUrl = coverImageUrl,
        country = countries.joinToString(", "),
        continent = firstPoint?.let { continentFor(it.lat, it.lng) } ?: Continent.EUROPE,
        durationDays = (days.maxOfOrNull { it.dayNumber } ?: 1),
        destinationTypes = emptyList(),
        budgetTier = budgetTier,
        paceTag = paceTag,
        tags = listOf("shared"),
        days = days.map { d ->
            TemplateDay(
                dayNumber = d.dayNumber,
                activities = d.stops.map {
                    TemplateActivity(it.title, it.time, it.endTime, it.location, "", it.lat, it.lng)
                },
            )
        },
        legs = legs.map { TemplateLeg(it.dayNumber, it.fromCity, it.toCity, it.transport) },
        published = true,
        usedCount = copyCount,
        authorUid = authorUid,
    )
}

/** Pulls the share id out of a pasted link (`.../s/<id>`, `itinera://s/<id>`) or a bare id; null if it doesn't look like one. */
fun parseShareId(input: String): String? {
    val cleaned = input.trim().substringBefore('?').substringBefore('#').trimEnd('/')
    val candidate = cleaned.substringAfterLast('/')
    return candidate.takeIf { Regex("""[A-Za-z0-9]{16,40}""").matches(it) }
}

/** Dates of the journeys that will be shared (home legs excluded), so the share preview numbers days like the published copy. */
fun sharedLegDates(trip: Trip, homeCity: String): List<LocalDate> = tripLegsWithoutHome(trip, homeCity).map { it.date }
