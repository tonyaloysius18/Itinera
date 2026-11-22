package com.itinera.app.model

import com.itinera.app.data.BarcodeExtraction
import kotlinx.datetime.LocalDate
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
    val fromLat: Double = 0.0,          // ⬅ ADD — geocoded once at save time
    val fromLng: Double = 0.0,          // ⬅ ADD
    val toLat: Double = 0.0,            // ⬅ ADD
    val toLng: Double = 0.0,            // ⬅ ADD
    val travellerIds: List<String> = emptyList(),
    val stops: List<LegStop> = emptyList(),
    /** Position in its day set by dragging; null = sorted by its time. See [dayOrderKey]. */
    val sortKey: Double? = null,
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
    val settledAt: Long = 0L,        // 0 = not settled; epoch millis when owner settled
    val settledBy: String = "",
    val frontHeartUrl: String = "",
    val frontRectUrl:  String = "",
    val backTopUrl:    String = "",
    val backBottomUrl: String = "",
    // Optional trip budget. Leave at 0.0 to hide the budget bar on the expenses
    // screen; set it and the hero draws spend-against-budget.  ⬅ ADD (optional)
    val budget: Double = 0.0,
    // Planned first/last day, for trips without transport legs (e.g. created by Nera from activities).
    // Absent on existing documents, so they decode to null. Legs win when present.
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    // Countries the trip visits, for trips without transport legs (Nera). Legs win when present.
    val destinationCountries: List<String> = emptyList(),
    // 0 = not deleted; otherwise the epoch millis when the owner moved the trip to "Recently deleted". It stays
    // restorable for 30 days, then it is removed for good.
    val deletedAt: Long = 0L,
)

/**
 * The dates that say when a trip happens: its transport legs, or, for a trip without legs, its planned
 * start/end. Sorted; empty when the trip has no dates at all.
 */
fun Trip.scheduleDates(): List<LocalDate> {
    val legDates = legs.map { it.date }.sorted()
    return legDates.ifEmpty { listOfNotNull(startDate, endDate).sorted() }
}

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

/** The traveller row that stands for [uid] on this trip ("" if they have none). */
fun Trip.travellerIdFor(uid: String): String =
    travellers.firstOrNull { uid.isNotBlank() && it.userId == uid }?.id
        ?: travellers.firstOrNull { uid.isNotBlank() && uid == ownerId && it.isOwner }?.id
        ?: ""

/**
 * The travellers a pass is for. A pass can cover several people (one QR for a whole
 * booking); older passes only have the single [DocItem.travellerId].
 */
val DocItem.assignedTravellerIds: List<String>
    get() = travellerIds.filter { it.isNotBlank() }.ifEmpty { listOfNotNull(travellerId.takeIf { it.isNotBlank() }) }

/**
 * Passes are personal: the trip owner sees every document, everyone else sees only
 * documents assigned to them (alone or together with others) plus unassigned ones (shared
 * bookings, group place tickets). Someone else's boarding pass is never shown to another member.
 */
fun Trip.canSeeDocument(doc: DocItem, uid: String): Boolean {
    if (isOwnedBy(uid)) return true
    val assigned = doc.assignedTravellerIds
    return assigned.isEmpty() || travellerIdFor(uid).let { it.isNotBlank() && it in assigned }
}

/** Who a pass for [travellerIds] goes to: the members it's shared with, and the holders' accounts. */
data class DocumentAccess(
    val memberIds: List<String>,
    /** Accounts (uids) of the assigned travellers who are trip members, in assignment order. */
    val holderUids: List<String>,
)

/**
 * Unassigned: every member. Assigned: only the owner and the assigned travellers' own
 * accounts, so no other member's device ever receives someone else's pass.
 * Must match the documents rule in firestore.rules.
 */
fun Trip.documentAccess(travellerIds: List<String>): DocumentAccess {
    val assigned = travellerIds.filter { it.isNotBlank() }
    if (assigned.isEmpty()) return DocumentAccess(memberIds, emptyList())
    val holders = assigned.mapNotNull { id ->
        travellers.firstOrNull { it.id == id }?.userId?.takeIf { it.isNotBlank() && it in memberIds }
    }.distinct()
    return DocumentAccess((listOf(ownerId) + holders).filter { it.isNotBlank() }.distinct(), holders)
}

/** A document attached to a whole trip, a specific leg, or a specific place (activity). */
@Serializable
data class DocItem(
    val id: String,
    val tripId: String,
    val title: String,
    val fileName: String = "",
    val fileUrl: String = "",
    val mimeType: String = "",
    val category: String = "OTHER",    // ⬅ NEW — "TRANSPORT" | "ACCOMMODATION" | "OTHER"
    val legId: String = "",            // leg this ticket belongs to ("" = none)
    val activityId: String = "",       // place/attraction this ticket belongs to ("" = none)
    val type: DocType = DocType.IMAGE, // legacy
    val attachedToLabel: String = "",  // legacy
    val memberIds: List<String> = emptyList(),
    val segmentIndex: Int = -1,
    val travellerId: String = "",
    // Account (uid) of the traveller this pass is assigned to, "" when unassigned or the
    // traveller has no account. The Firestore rules use it: an assigned pass is shared only
    // with the trip owner and this uid.
    val travellerUid: String = "",
    // A pass can cover several travellers (one QR for a whole booking). When set, these are
    // authoritative; travellerId/travellerUid still hold the first one, for older app versions.
    val travellerIds: List<String> = emptyList(),
    val travellerUids: List<String> = emptyList(),
    val traveller: String = "",
    val thumbUrl: String = "",
    val createdBy: String = "",        // uid of the member who added this — for block-filtering
)

@Serializable
data class Activity(
    val id: String,
    val tripId: String,
    val date: LocalDate,
    val title: String,
    val time: String = "",
    val endTime: String = "",
    val location: String = "",
    val note: String = "",
    val completed: Boolean = false,
    val memberIds: List<String> = emptyList(),
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val createdBy: String = "",        // uid of the member who added this — for block-filtering
    /** Position in its day set by dragging; null = sorted by its time. See [dayOrderKey]. */
    val sortKey: Double? = null,
)

/**
 * Where an item sits within its day on the trip page. Untouched items sort by clock time
 * (minutes since midnight, 9:00 when there's no time); once dragged, the stored key wins.
 * Keys share one scale with times so new items still slot in by time among moved ones.
 */
fun dayOrderKey(sortKey: Double?, timeLabel: String): Double =
    sortKey ?: com.itinera.app.parseHourMinute(timeLabel).let { (h, m) -> (h * 60 + m).toDouble() }

/** A leg or place moved on the trip page: its (possibly new) day and its order key there. */
data class DayMove(val date: LocalDate, val sortKey: Double)

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
    val avatarId: String = "",
)

@Serializable
data class LegStop(
    val city: String = "",
    val arrivalTime: String = "",     // ⬅ ADD
    val departureTime: String = "",   // ⬅ ADD
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val country: String = "",
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

/**
 * Coarse spend bucket for an expense — drives the category meter and the
 * Categories lens on TripExpensesScreen.
 *
 * Six is deliberate: past that, slices in the stacked bar stop being
 * distinguishable at phone width and the legend wraps to three rows.
 *
 * ⚠️ Adding a value later is only safe if the Json instance gitlive decodes
 * with has `coerceInputValues = true` — otherwise an older client reading a
 * document containing the new value throws instead of falling back to OTHER.
 * Set it before shipping categories; it can't be retrofitted onto installed apps.
 */
@Serializable
enum class ExpenseCategory { ACCOMMODATION, FOOD, TRANSPORT, SHOPPING, ACTIVITIES, OTHER }

@Serializable
data class Expense(
    val id: String,
    val tripId: String,
    val description: String,
    val amount: Double,            // total, always in the trip's currency
    val paidByTravellerId: String,
    val shares: List<ExpenseShare> = emptyList(),   // sums to amount
    val createdAt: Long = 0L,
    val memberIds: List<String> = emptyList(),
    // Absent on existing documents, so they decode to OTHER. No migration.  ⬅ ADD
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val createdBy: String = "",        // uid of the member who added this — for block-filtering
    // What was actually paid, when it wasn't in the trip's currency (e.g. 15000 HUF on a
    // EUR trip). `amount` holds the converted figure, so balances, older app versions and
    // everything else keep reading `amount`. Blank currency = paid in the trip's currency.
    val originalAmount: Double = 0.0,
    val originalCurrency: String = "",
    val fxRate: Double = 1.0,          // trip currency per 1 unit of originalCurrency
    val fxDate: String = "",           // ISO date the rate is from ("" = not converted)
)

/** True when this expense was paid in a currency other than the trip's. */
val Expense.isForeign: Boolean
    get() = originalCurrency.isNotBlank() && originalAmount > 0.0

@Serializable
data class Payment(
    val id: String,
    val tripId: String,
    val fromTravellerId: String,   // who paid back (the debtor)
    val toTravellerId: String,     // who received
    val amount: Double,
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
    val avatarId: String = "",
    val reminderOffsetMinutes: Int = 0,
    val photoUrl: String = "",
    val migratedToShared: Boolean = false,
    val migratedDocsExpenses: Boolean = false,
    val pinnedTripIds: List<String> = emptyList(),      // ⬅ ADD
    val archivedTripIds: List<String> = emptyList(),
    val blockedUserIds: List<String> = emptyList(),     // uids this user has blocked — their content is hidden
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

/**
 * An abuse/objectionable-content report a user files against another member of a
 * shared trip. Written to the top-level `reports` collection for admin review.
 * (Requires a Firestore rule allowing authenticated creates on `reports`.)
 */
@Serializable
data class Report(
    val id: String,
    val reporterUid: String,
    val reportedUid: String,
    val tripId: String = "",
    val reason: String,              // "spam" | "harassment" | "inappropriate" | "other"
    val details: String = "",
    val createdAt: Long = 0L,
    val status: String = "open",     // "open" | "reviewed"
)

data class WalletTicket(
    val extraction: BarcodeExtraction,
    val docId: String,
    val docTitle: String = "",
    val routeOverride: String = "",
    val timeOverride: String = "",
    /** Travellers this code is for (several when one QR covers a whole booking). */
    val travellerIds: List<String> = emptyList(),
    /** Their names, joined for display ("Tony Aloysius + Jow Robinson"). */
    val travellerName: String = "",
    /** The same people as a list of (first name, full name), for the compact pass label. */
    val travellerNames: List<Pair<String, String>> = emptyList(),
    val assignmentAmbiguous: Boolean = false,
    /** Left as "Anyone" on a leg: one ticket for every passenger on that leg. */
    val sharedWithAll: Boolean = false,
)
