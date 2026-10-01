package com.itinera.app.model

import com.itinera.app.data.NeraActivity
import com.itinera.app.data.NeraDay
import com.itinera.app.data.NeraItinerary
import com.itinera.app.data.NeraLeg
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.DatePeriod
import kotlinx.serialization.Serializable

/**
 * Where a template lives geographically. Kept separate from the free-text
 * [Trip.destinationCountries] country strings so "browse by continent" is a
 * reliable filter rather than a string match.
 */
@Serializable
enum class Continent { EUROPE, ASIA, AFRICA, NORTH_AMERICA, SOUTH_AMERICA, OCEANIA }

/**
 * What kind of destination a template is, for the Discover filter chips
 * (Beach / Mountain / Desert / ...). A template can carry more than one —
 * e.g. the Amalfi Coast is both BEACH and COUNTRYSIDE.
 *
 * ⚠️ Same coerceInputValues caveat as [ExpenseCategory]: adding a value later
 * is only safe if decoding falls back gracefully for older clients reading a
 * newer template document.
 */
@Serializable
enum class DestinationType { BEACH, MOUNTAIN, DESERT, CITY, COUNTRYSIDE, ISLAND, LAKE }

/** Spend level a template is built around — independent from [PaceTag]. */
@Serializable
enum class BudgetTier { BUDGET, MID_RANGE, LUXURY }

/** How packed a template's days are — independent from [BudgetTier] (a trip can be relaxed and expensive, or packed and cheap). */
@Serializable
enum class PaceTag { RELAXED, BALANCED, PACKED }

/** One planned stop within a template day. Mirrors [NeraActivity] so converting to a draft is a direct field copy. */
@Serializable
data class TemplateActivity(
    val title: String,
    val time: String = "",
    val endTime: String = "",
    val location: String = "",
    val note: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
)

/**
 * One day of a template, relative to the trip's start. [dayNumber] is 1-based
 * (day 1 = arrival day) rather than an absolute date, since a template has no
 * real dates until a traveller picks a start date for their copy.
 */
@Serializable
data class TemplateDay(
    val dayNumber: Int,
    val theme: String = "",
    val activities: List<TemplateActivity> = emptyList(),
)

/** A transport leg within a template, anchored to a day offset rather than an absolute date. */
@Serializable
data class TemplateLeg(
    val dayNumber: Int,
    val fromCity: String,
    val toCity: String,
    val transport: TransportType,
    val time: String = "",
    val endTime: String = "",
)

/**
 * A curated, read-only itinerary a traveller can add to their own trips (e.g.
 * "5 Days in London"). Lives in its own top-level `tripTemplates` Firestore
 * collection, authored/updated centrally — never mutated by the app.
 *
 * "Use this template" doesn't touch this model at runtime: it converts the
 * template to a [NeraItinerary] via [toNeraItinerary] and hands that to the
 * existing `TripRepository.createTripFromItinerary`, the same path Nera's own
 * drafts already go through.
 */
@Serializable
data class TripTemplate(
    val id: String,
    val title: String,
    val shortDescription: String = "",
    val coverImageUrl: String = "",
    val country: String,
    val continent: Continent,
    val durationDays: Int,
    val destinationTypes: List<DestinationType> = emptyList(),
    val budgetTier: BudgetTier = BudgetTier.MID_RANGE,
    val paceTag: PaceTag = PaceTag.BALANCED,
    val tags: List<String> = emptyList(),      // freeform, e.g. "romantic", "family-friendly", "first-timer"
    val days: List<TemplateDay> = emptyList(),
    val legs: List<TemplateLeg> = emptyList(),
    val published: Boolean = true,             // false = hidden from Discover while still being authored
    val sortOrder: Int = 0,
)

/**
 * Resolves this template's relative day numbers against a real [startDate], producing the same
 * shape Nera's own drafts use so it can go straight into `TripRepository.createTripFromItinerary`.
 */
fun TripTemplate.toNeraItinerary(startDate: LocalDate): NeraItinerary {
    fun dateFor(dayNumber: Int): LocalDate = startDate.plus(DatePeriod(days = dayNumber - 1))

    return NeraItinerary(
        title = title,
        destination = country,
        countries = listOf(country),
        startDate = startDate.toString(),
        days = days.map { day ->
            NeraDay(
                date = dateFor(day.dayNumber).toString(),
                theme = day.theme,
                activities = day.activities.map { a ->
                    NeraActivity(
                        title = a.title,
                        time = a.time,
                        endTime = a.endTime,
                        location = a.location,
                        note = a.note,
                        lat = a.lat,
                        lng = a.lng,
                    )
                },
            )
        },
        legs = legs.map { l ->
            NeraLeg(
                fromCity = l.fromCity,
                toCity = l.toCity,
                transport = l.transport.name.lowercase(),
                date = dateFor(l.dayNumber).toString(),
                time = l.time,
                endTime = l.endTime,
            )
        },
    )
}
