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
@Serializable
enum class TransportType { FLIGHT, TRAIN, BUS, FERRY, CAR }

/** A stored travel document (ticket, reservation, etc.). */
@Serializable
enum class DocType { PDF, IMAGE }

/** Simple cover accent for a trip card. */
@Serializable
enum class TripAccent { BLUE, GREEN, CORAL, PURPLE }

/**
 * A single segment of a journey: from one place to another at a given time.
 * Dates/times are kept as display strings here to keep the skeleton dependency-free.
 * In production, swap these for kotlinx-datetime LocalDateTime values.
 */
@Serializable
data class Leg(
    val id: String,
    val fromCity: String,
    val toCity: String,
    val transport: TransportType,
    val date: LocalDate,            // ⬅ was dateLabel: String
    val timeLabel: String = "",
    val endTimeLabel: String = "",     // ⬅ ADD — end time (optional)
    val operator: String = "",
    val country: String = "",
    val bookingRef: String? = null,
    val completed: Boolean = false,
    val addedToCalendar: Boolean = false,
)

/** A trip groups together an ordered list of legs and its documents/checklist. */
@Serializable
data class Trip(
    val id: String,
    val title: String,
    val travellers: List<Traveller> = emptyList(),
    val countriesCount: Int,
    val dateRange: String,
    val accent: TripAccent,
    val legs: List<Leg>,
    val imageUrl: String? = null,
    val pinned: Boolean = false,        // ⬅ ADD
    val archived: Boolean = false,
    val currencyCode: String = "EUR",
    val ownerId: String = "",
    val members: Map<String, String> = emptyMap(),     // uid -> role (rules check this)
    val memberIds: List<String> = emptyList(),         // uid list (for querying)  ⬅ ADD
    val memberInfo: Map<String, MemberInfo> = emptyMap(),
)

@Serializable
data class MemberInfo(
    val name: String = "",
    val email: String = "",
)

@Serializable
enum class TripRole { OWNER, EDITOR, VIEWER }

/** The current user's role on a trip, or null if not a member. */
fun Trip.roleOf(uid: String): TripRole? = when (members[uid]) {
    "owner" -> TripRole.OWNER
    "editor" -> TripRole.EDITOR
    "viewer" -> TripRole.VIEWER
    else -> null
}

/** True if this user can edit the trip (owner or editor). */
fun Trip.canEdit(uid: String): Boolean =
    members[uid] == "owner" || members[uid] == "editor"

/** True if this user owns the trip. */
fun Trip.isOwnedBy(uid: String): Boolean = members[uid] == "owner"

/** A document attached either to a whole trip or to a specific leg. */
@Serializable
data class DocItem(
    val id: String,
    val tripId: String,
    val title: String,
    val fileName: String = "",
    val fileUrl: String = "",
    val mimeType: String = "",
    val category: String = "OTHER",    // ⬅ NEW — "TRANSPORT" | "ACCOMMODATION" | "OTHER"
    val legId: String = "",            // kept for back-compat (unused now)
    val type: DocType = DocType.IMAGE, // legacy
    val attachedToLabel: String = "",  // legacy
    val memberIds: List<String> = emptyList(),

    )

@Serializable
data class Activity(
    val id: String,
    val tripId: String,
    val date: LocalDate,
    val title: String,
    val time: String = "",
    val location: String = "",
    val note: String = "",
    val completed: Boolean = false,
    val memberIds: List<String> = emptyList(),   // ⬅ ADD
)

@Serializable
data class Traveller(
    val id: String,
    val firstName: String,
    val surname: String = "",
    val email: String = "",
    val phone: String = "",
    val colorIndex: Int = 0,
    val isOwner: Boolean = false,
    val userId: String = "",
)

val Traveller.fullName: String
    get() = listOf(firstName, surname).filter { it.isNotBlank() }.joinToString(" ")

/** A single preparation checklist entry. */
@Serializable
data class ChecklistItem(
    val id: String = "",
    val tripId: String = "",
    val text: String = "",
    val group: String = "",
    val done: Boolean = false,
)

@Serializable
data class ExpenseShare(
    val travellerId: String,
    val amount: Double,            // this person's share of the expense
)

@Serializable
data class Expense(
    val id: String,
    val tripId: String,
    val description: String,
    val amount: Double,            // total
    val paidByTravellerId: String,
    val shares: List<ExpenseShare> = emptyList(),   // sums to amount
    val createdAt: Long = 0L,
    val memberIds: List<String> = emptyList(),

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
    val reminderOffsetMinutes: Int = 0,
    val photoUrl: String = "",
    val migratedToShared: Boolean = false,
    val migratedDocsExpenses: Boolean = false,
    val pinnedTripIds: List<String> = emptyList(),      // ⬅ ADD
    val archivedTripIds: List<String> = emptyList(),
    @Transient val photoBytes: ByteArray? = null,   // ⬅ excluded from Firestore
)
{
    val fullName: String get() = "$name $surname".trim()
    val initials: String get() =
        ((name.firstOrNull()?.toString() ?: "") + (surname.firstOrNull()?.toString() ?: "")).uppercase()
}

@Serializable
data class Invite(
    val id: String,
    val tripId: String,
    val tripTitle: String = "",
    val code: String,
    val createdBy: String = "",
    val status: String = "active",   // "active" | "revoked"
    val createdAt: Long = 0L,
)
