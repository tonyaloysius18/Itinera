package com.itinera.app.data

import com.itinera.app.model.Activity
import com.itinera.app.model.Expense
import com.itinera.app.model.Trip
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Turns trips into a shareable text summary or JSON.
 * Pure commonMain Kotlin — no platform APIs — so it works on Android + iOS.
 *
 * The screen collects the data (trip + its activities + its expenses) and
 * passes it in; this object only does formatting.
 */
object TripExporter {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    /** Everything needed to export one trip. */
    data class TripBundle(
        val trip: Trip,
        val activities: List<Activity>,
        val expenses: List<Expense>,
    )

    // ──────────────────────────────────────────────────────────────
    // TEXT  (human-readable .txt)
    // ──────────────────────────────────────────────────────────────
    fun toText(bundles: List<TripBundle>): String = buildString {
        appendLine("ITINERA — Trip Export")
        appendLine("=".repeat(40))
        appendLine()
        bundles.forEachIndexed { i, b ->
            appendTrip(b)
            if (i != bundles.lastIndex) {
                appendLine()
                appendLine("-".repeat(40))
                appendLine()
            }
        }
    }

    private fun StringBuilder.appendTrip(b: TripBundle) {
        val trip = b.trip
        appendLine("TRIP: ${trip.title}")

        val legDates = trip.legs.map { it.date }.sorted()
        if (legDates.isNotEmpty()) {
            val first = legDates.first()
            val last = legDates.last()
            val range = if (first == last) first.toString() else "$first  to  $last"
            appendLine("Dates: $range")
        }

        // Travellers
        if (trip.travellers.isNotEmpty()) {
            appendLine("Travellers: " + trip.travellers.joinToString(", ") { it.firstName })
        }

        // Legs (transport)
        if (trip.legs.isNotEmpty()) {
            appendLine()
            appendLine("Journeys:")
            trip.legs.sortedBy { it.date }.forEach { leg ->
                val time = listOf(leg.timeLabel, leg.endTimeLabel)
                    .filter { it.isNotBlank() }
                    .joinToString(" – ")
                val timePart = if (time.isNotBlank()) "  ($time)" else ""
                val opPart = if (leg.operator.isNotBlank()) "  · ${leg.operator}" else ""
                appendLine("  • ${leg.date}  ${leg.fromCity} → ${leg.toCity}  [${leg.transport.name.lowercase()}]$timePart$opPart")
            }
        }

        // Places / activities
        if (b.activities.isNotEmpty()) {
            appendLine()
            appendLine("Places & activities:")
            b.activities.sortedWith(compareBy({ it.date }, { it.time })).forEach { act ->
                val timePart = if (act.time.isNotBlank()) "  ${act.time}" else ""
                val locPart = if (act.location.isNotBlank()) "  @ ${act.location}" else ""
                appendLine("  • ${act.date}$timePart  ${act.title}$locPart")
            }
        }

        // Expenses
        if (b.expenses.isNotEmpty()) {
            val total = b.expenses.sumOf { it.amount }
            appendLine()
            appendLine("Expenses (${trip.currencyCode}):")
            b.expenses.sortedByDescending { it.createdAt }.forEach { e ->
                val payer = trip.travellers.firstOrNull { it.id == e.paidByTravellerId }?.firstName ?: "?"
                appendLine("  • ${e.description}  —  ${money(e.amount)} ${trip.currencyCode}  (paid by $payer)")
            }
            appendLine("  Total: ${money(total)} ${trip.currencyCode}")
        }
    }

    private fun money(v: Double): String {
        val cents = kotlin.math.round(v * 100).toLong()
        val whole = cents / 100
        val frac = (kotlin.math.abs(cents % 100)).toInt()
        val fracStr = if (frac < 10) "0$frac" else "$frac"
        return "$whole.$fracStr"
    }

    // ──────────────────────────────────────────────────────────────
    // JSON  (machine-readable .json — reuses your @Serializable models)
    // ──────────────────────────────────────────────────────────────
    fun toJson(bundles: List<TripBundle>): String {
        val payload = bundles.map { b ->
            ExportTrip(
                trip = b.trip,
                activities = b.activities,
                expenses = b.expenses,
            )
        }
        return json.encodeToString(ExportPayload(trips = payload))
    }

    @Serializable
    private data class ExportPayload(
        val app: String = "Itinera",
        val version: Int = 1,
        val trips: List<ExportTrip>,
    )

    @Serializable
    private data class ExportTrip(
        val trip: Trip,
        val activities: List<Activity>,
        val expenses: List<Expense>,
    )
}