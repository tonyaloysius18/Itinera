package com.itinera.app.model

import kotlinx.datetime.LocalDate
import com.itinera.app.model.label
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


fun LocalDate.label(): String {
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    return "$dayOfMonth ${months[monthNumber - 1]}"
}
/** Mode of transport for a single leg of a journey. */
enum class TransportType { FLIGHT, TRAIN, BUS, FERRY, CAR }

/** A stored travel document (ticket, reservation, etc.). */
enum class DocType { PDF, IMAGE }

/** Simple cover accent for a trip card. */
enum class TripAccent { BLUE, GREEN, CORAL, PURPLE }

data class Activity(
    val id: String,
    val tripId: String,
    val date: LocalDate,
    val title: String,       // "Eiffel Tower"
    val time: String = "",   // "10:00" (optional)
    val location: String = "", // "Champ de Mars, Paris" (optional)
    val note: String = "",   // optional
    val completed: Boolean = false,
)

/**
 * A single segment of a journey: from one place to another at a given time.
 * Dates/times are kept as display strings here to keep the skeleton dependency-free.
 * In production, swap these for kotlinx-datetime LocalDateTime values.
 */
data class Leg(
    val id: String,
    val fromCity: String,
    val toCity: String,
    val transport: TransportType,
    val date: LocalDate,            // ⬅ was dateLabel: String
    val timeLabel: String = "",
    val bookingRef: String? = null,
    val completed: Boolean = false,
)

/** A trip groups together an ordered list of legs and its documents/checklist. */
data class Trip(
    val id: String,
    val title: String,
    val countriesCount: Int,
    val dateRange: String,
    val accent: TripAccent,
    val legs: List<Leg>,
    val imageUrl: String? = null,
    val pinned: Boolean = false,        // ⬅ ADD
    val archived: Boolean = false,
)

/** A document attached either to a whole trip or to a specific leg. */
data class DocItem(
    val id: String,
    val tripId: String,
    val title: String,
    val type: DocType,
    val attachedToLabel: String,
)

/** A single preparation checklist entry. */
data class ChecklistItem(
    val id: String,
    val tripId: String,
    val text: String,
    val group: String,
    val done: Boolean = false,
)


@Serializable                                   // ⬅ ADD
data class UserProfile(
    val name: String = "",                      // ⬅ defaults added (Firestore needs them)
    val surname: String = "",
    val email: String = "",
    val mobile: String = "",
    val dob: String = "",
    val street: String = "",
    val city: String = "",
    val postalCode: String = "",
    val photoUrl: String = "",
    @Transient val photoBytes: ByteArray? = null,   // ⬅ excluded from Firestore
) {
    val fullName: String get() = "$name $surname".trim()
    val initials: String get() =
        ((name.firstOrNull()?.toString() ?: "") + (surname.firstOrNull()?.toString() ?: "")).uppercase()
}