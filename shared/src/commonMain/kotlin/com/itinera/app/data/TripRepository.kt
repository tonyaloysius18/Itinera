package com.itinera.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.itinera.app.NotificationScheduler
import com.itinera.app.ReminderOffset
import com.itinera.app.legReminderFireTime
import com.itinera.app.model.ChecklistItem
import com.itinera.app.model.DocItem
import com.itinera.app.model.Leg
import com.itinera.app.model.Trip
import com.itinera.app.model.UserProfile
import com.itinera.app.model.Activity
import com.itinera.app.model.Expense
import com.itinera.app.model.Traveller
import com.itinera.app.model.TripAccent
import com.itinera.app.model.isOwnedBy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch



/**
 * Repository holding app data as observable Compose state.
 *
 * Trips, documents, and expenses persist to Firestore under the shared top-level
 * trips/{tripId} structure (with documents/ and expenses/ sub-collections). A trip is
 * visible to any user whose uid is in its memberIds; docs/expenses carry a denormalized
 * memberIds copy so a single collection-group query streams them per member.
 */
class TripRepository {

    val trips = mutableStateListOf<Trip>()
    val documents = mutableStateListOf<DocItem>()

    val docService = DocService()

    val checklist = mutableStateListOf<ChecklistItem>()

    val unsplashApi = UnsplashApi()
    val authService = AuthService()
    val profileService = ProfileService()
    val tripService = TripService()

    val activityService = ActivityService()

    val expenseService = ExpenseService()

    val expenses = mutableStateListOf<Expense>()

    val paymentService = PaymentService()

    val payments = mutableStateListOf<com.itinera.app.model.Payment>()

    val notificationScheduler = NotificationScheduler()

    val inviteService = InviteService()

    val checklistService = ChecklistService()

    val accountStore = AccountStore()

    val worldClockStore = WorldClockStore

    val weatherStore = WeatherStore

    val translator = Translator()

    val compass = Compass()


    private var tripsListener: Job? = null

    private val syncJobs = mutableListOf<Job>()

    var tripsSyncedOnce by mutableStateOf(false)
        private set
    var documentsSyncedOnce by mutableStateOf(false)
        private set
    var expensesSyncedOnce by mutableStateOf(false)
        private set

    var paymentsSyncedOnce by mutableStateOf(false)
        private set

    var activitiesSyncedOnce by mutableStateOf(false)
        private set

    var checklistSyncedOnce by mutableStateOf(false)
        private set



    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun nowMillis(): Long =
        kotlin.time.Clock.System.now().toEpochMilliseconds()

    /** Current memberIds for a trip (used to stamp docs/expenses for sharing). */
    private fun memberIdsForTrip(tripId: String): List<String> =
        trips.firstOrNull { it.id == tripId }?.memberIds ?: emptyList()

    // Schedule (or refresh) a reminder for one leg, based on the user's offset.
    private fun scheduleLegReminder(trip: Trip, leg: Leg) {
        val offset = profile.reminderOffsetMinutes
        notificationScheduler.cancel(leg.id)
        if (offset == ReminderOffset.OFF) return
        if (leg.completed) return
        val fireAt = legReminderFireTime(leg, offset, nowMillis()) ?: return
        val title = "${leg.fromCity} → ${leg.toCity}"
        val body = "Departing ${leg.timeLabel.ifBlank { "soon" }} · ${trip.title}"
        notificationScheduler.schedule(leg.id, title, body, fireAt, trip.id)    }

    private val uploadClient = HttpClient()

    // Background scope for fire-and-forget cloud writes.
    private val ioScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // ===== cloud helpers =====
    private fun persist(trip: Trip) {
        if (authService.currentUid == null) { println("ITINERA: PERSIST skipped — no uid"); return }
        ioScope.launch {
            try {
                tripService.saveTrip(trip)
                println("ITINERA: PERSIST ok — ${trip.title}")
            } catch (e: Exception) {
                println("ITINERA: PERSIST FAILED — ${e.message}")
            }
        }
    }

    private fun removeRemote(tripId: String) {
        ioScope.launch { runCatching { tripService.deleteTrip(tripId) } }
    }

    /** Replace the in-memory trips with the user's cloud data (trips they're a member of). */
    suspend fun loadTrips(uid: String) {
        try {
            val remote = tripService.loadTrips(uid)
            println("ITINERA: LOAD ok — ${remote.size} trips")
            trips.clear()
            trips.addAll(remote)
        } catch (e: Exception) {
            println("ITINERA: LOAD FAILED — ${e.message}")
        }
    }

    fun addTrip(title: String): String {
        val uid = authService.currentUid ?: ""
        val id = "trip_${kotlin.random.Random.nextLong()}"
        val trip = Trip(
            id = id,
            title = title.trim(),
            countriesCount = 0,
            dateRange = "",
            accent = TripAccent.values().random(),
            legs = emptyList(),
            ownerId = uid,
            members = mapOf(uid to "owner"),
            memberIds = listOf(uid),
            memberInfo = mapOf(uid to com.itinera.app.model.MemberInfo(
                name = listOf(profile.name, profile.surname).filter { it.isNotBlank() }.joinToString(" "),
                email = profile.email,
            )),
        )
        trips.add(trip)
        persist(trip)
        return id
    }

    fun updateTripImage(id: String, url: String) {
        val i = trips.indexOfFirst { it.id == id }
        if (i >= 0) {
            trips[i] = trips[i].copy(imageUrl = url)
            persist(trips[i])
        }
    }

    fun updateTrip(id: String, title: String) {
        val i = trips.indexOfFirst { it.id == id }
        if (i >= 0) {
            trips[i] = trips[i].copy(title = title.trim())
            persist(trips[i])
        }
    }

    fun deleteTrip(id: String) {
        trips.removeAll { it.id == id }
        removeRemote(id)
    }

    fun isPinned(tripId: String): Boolean = tripId in profile.pinnedTripIds
    fun isArchived(tripId: String): Boolean = tripId in profile.archivedTripIds

    fun togglePin(id: String) {
        val pinned = profile.pinnedTripIds.toMutableList()
        if (id in pinned) pinned.remove(id) else pinned.add(id)
        val updated = profile.copy(pinnedTripIds = pinned)
        profile = updated
        ioScope.launch {
            runCatching { authService.currentUid?.let { profileService.saveProfile(it, updated) } }
        }
    }

    fun toggleArchive(id: String) {
        val archived = profile.archivedTripIds.toMutableList()
        if (id in archived) archived.remove(id) else archived.add(id)
        val updated = profile.copy(archivedTripIds = archived)
        profile = updated
        ioScope.launch {
            runCatching { authService.currentUid?.let { profileService.saveProfile(it, updated) } }
        }
    }

    fun activeTrips(): List<Trip> =
        trips.filter { !isArchived(it.id) }.sortedByDescending { isPinned(it.id) }

    fun archivedTrips(): List<Trip> = trips.filter { isArchived(it.id) }

    fun tripById(id: String): Trip? = trips.firstOrNull { it.id == id }

    fun documentsForTrip(tripId: String): List<DocItem> =
        documents.filter { it.tripId == tripId }

    fun checklistForTrip(tripId: String): List<ChecklistItem> =
        checklist.filter { it.tripId == tripId }

    fun toggleLegCompleted(tripId: String, legId: String) {
        val tripIndex = trips.indexOfFirst { it.id == tripId }
        if (tripIndex < 0) return
        val trip = trips[tripIndex]
        val newLegs = trip.legs.map {
            if (it.id == legId) it.copy(completed = !it.completed) else it
        }
        trips[tripIndex] = trip.copy(legs = newLegs)
        persist(trips[tripIndex])
    }

    fun toggleChecklistItem(itemId: String) {
        val index = checklist.indexOfFirst { it.id == itemId }
        if (index < 0) return
        val updated = checklist[index].copy(done = !checklist[index].done)
        checklist[index] = updated
        val uid = authService.currentUid ?: return
        ioScope.launch { runCatching { checklistService.saveItem(uid, updated) } }
    }

    fun addChecklistItem(tripId: String, text: String, group: String) {
        val item = ChecklistItem(
            id = "c_${kotlin.random.Random.nextLong()}",
            tripId = tripId,
            text = text.trim(),
            group = group,
        )
        checklist.add(item)
        val uid = authService.currentUid ?: return
        ioScope.launch { runCatching { checklistService.saveItem(uid, item) } }
    }

    fun deleteChecklistItem(itemId: String) {
        checklist.removeAll { it.id == itemId }
        val uid = authService.currentUid ?: return
        ioScope.launch { runCatching { checklistService.deleteItem(uid, itemId) } }
    }

    fun addLeg(tripId: String, leg: Leg) {
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        trips[index] = trip.copy(legs = trip.legs + leg)
        persist(trips[index])
        scheduleLegReminder(trips[index], leg)
    }

    fun updateLeg(tripId: String, leg: Leg) {
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        trips[index] = trip.copy(legs = trip.legs.map { if (it.id == leg.id) leg else it })
        persist(trips[index])
        scheduleLegReminder(trips[index], leg)
    }

    fun deleteLeg(tripId: String, legId: String) {
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        trips[index] = trip.copy(legs = trip.legs.filterNot { it.id == legId })
        persist(trips[index])
        notificationScheduler.cancel(legId)
    }

    fun markLegAddedToCalendar(tripId: String, legId: String) {
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        val newLegs = trip.legs.map {
            if (it.id == legId) it.copy(addedToCalendar = true) else it
        }
        trips[index] = trip.copy(legs = newLegs)
        persist(trips[index])
    }

    fun rescheduleAllReminders() {
        trips.forEach { trip -> trip.legs.forEach { leg -> scheduleLegReminder(trip, leg) } }
    }

    // ===== activities (still in-memory) =====
    val activities = mutableStateListOf<Activity>()
    fun activitiesForTrip(tripId: String): List<Activity> =
        activities.filter { it.tripId == tripId }

    fun addActivity(tripId: String, date: LocalDate, title: String, time: String, location: String) {
        val act = Activity(
            id = "a_${kotlin.random.Random.nextLong()}",
            tripId = tripId,
            date = date,
            title = title.trim(),
            time = time.trim(),
            location = location.trim(),
            memberIds = memberIdsForTrip(tripId),
        )
        activities.add(act)
        ioScope.launch { runCatching { activityService.saveActivity(act) } }
    }

    fun updateActivity(id: String, date: LocalDate, title: String, time: String, location: String) {
        val i = activities.indexOfFirst { it.id == id }
        if (i < 0) return
        val updated = activities[i].copy(
            date = date, title = title.trim(), time = time.trim(), location = location.trim(),
            memberIds = memberIdsForTrip(activities[i].tripId),
        )
        activities[i] = updated
        ioScope.launch { runCatching { activityService.saveActivity(updated) } }
    }

    fun toggleActivity(id: String) {
        val i = activities.indexOfFirst { it.id == id }
        if (i < 0) return
        val updated = activities[i].copy(completed = !activities[i].completed)
        activities[i] = updated
        ioScope.launch { runCatching { activityService.saveActivity(updated) } }
    }

    fun deleteActivity(id: String) {
        val act = activities.firstOrNull { it.id == id }
        activities.removeAll { it.id == id }
        if (act != null) ioScope.launch { runCatching { activityService.deleteActivity(act.tripId, id) } }
    }

    fun tripDates(tripId: String): List<LocalDate> {
        val legDates = tripById(tripId)?.legs?.map { it.date } ?: emptyList()
        val actDates = activities.filter { it.tripId == tripId }.map { it.date }
        return (legDates + actDates).distinct().sorted()
    }

    fun dayNumberFor(tripId: String, date: LocalDate): Int {
        val rank = tripDates(tripId).indexOf(date)
        return if (rank >= 0) rank + 1 else 1
    }

    // ===== profile =====
    var profile by mutableStateOf(UserProfile())
        private set

    fun updateProfile(updated: UserProfile) { profile = updated }

    suspend fun uploadProfilePhoto(uid: String, bytes: ByteArray): String {
        return uploadBytesToStorage(uploadClient, bytes)
    }

    /** Uploads a cropped postcard photo to Cloudinary and saves its URL on the trip slot. */
    suspend fun uploadPostcardPhoto(tripId: String, slot: String, bytes: ByteArray): String {
        val url = uploadBytesToStorage(uploadClient, bytes)   // uploadClient is private — fine here
        val idx = trips.indexOfFirst { it.id == tripId }
        if (idx >= 0) {
            trips[idx] = when (slot) {
                "heart"      -> trips[idx].copy(frontHeartUrl = url)
                "rect"       -> trips[idx].copy(frontRectUrl  = url)
                "backTop"    -> trips[idx].copy(backTopUrl    = url)
                "backBottom" -> trips[idx].copy(backBottomUrl = url)
                else         -> trips[idx]
            }
            persist(trips[idx])   // writes to Firestore, same as your other updates
        }
        return url
    }

    fun removePostcardPhoto(tripId: String, slot: String) {
        val idx = trips.indexOfFirst { it.id == tripId }
        if (idx >= 0) {
            trips[idx] = when (slot) {
                "heart"      -> trips[idx].copy(frontHeartUrl = "")
                "rect"       -> trips[idx].copy(frontRectUrl  = "")
                "backTop"    -> trips[idx].copy(backTopUrl    = "")
                "backBottom" -> trips[idx].copy(backBottomUrl = "")
                else         -> trips[idx]
            }
            persist(trips[idx])
        }
    }

    fun clearLocal() {
        stopSync()
        trips.clear()
        documents.clear()
        checklist.clear()
        activities.clear()
        profile = UserProfile()
        expenses.clear()
        payments.clear()

    }

    // ===== documents (now under trips/{tripId}/documents) =====
    fun addDocument(doc: DocItem) {
        val stamped = doc.copy(memberIds = memberIdsForTrip(doc.tripId))
        documents.add(stamped)
        ioScope.launch { runCatching { docService.saveDocument(stamped) } }
    }

    fun deleteDocument(docId: String) {
        val doc = documents.firstOrNull { it.id == docId }
        documents.removeAll { it.id == docId }
        if (doc != null) ioScope.launch { runCatching { docService.deleteDocument(doc.tripId, docId) } }
    }

    /** No longer needed for live sync (collection-group flow handles it); kept as a no-op
     *  so the Backup "Sync Now" call site still compiles. */
    suspend fun loadDocuments(uid: String) { /* handled by live collection-group sync */ }

    suspend fun addDocumentWithFile(
        tripId: String,
        title: String,
        category: String,
        file: PickedFile,
        legId: String = "",
    ): Boolean {
        return try {
            val url = uploadFileToStorage(uploadClient, file.bytes, file.fileName, file.mimeType)
            val doc = DocItem(
                id = "doc_${kotlin.random.Random.nextLong()}",
                tripId = tripId,
                title = title,
                category = category,
                legId = legId,
                fileName = file.fileName,
                fileUrl = url,
                mimeType = file.mimeType,
                memberIds = memberIdsForTrip(tripId),
            )
            documents.add(doc)
            docService.saveDocument(doc)
            true
        } catch (e: Exception) {
            println("DOC UPLOAD FAILED: ${e.message}")
            false
        }
    }

    suspend fun loadBytes(url: String): ByteArray? {
        return try {
            uploadClient.get(url).bodyAsBytes()
        } catch (e: Exception) {
            println("ITINERA: LOAD BYTES FAILED — ${e.message}")
            null
        }
    }

    suspend fun downloadBytes(url: String): ByteArray? = try {
        val response = uploadClient.get(url)
        if (response.status.isSuccess()) {
            response.body<ByteArray>()
        } else {
            println("DOC DOWNLOAD HTTP ${response.status}: ${response.bodyAsText().take(200)}")
            null
        }
    } catch (e: Exception) {
        println("DOC DOWNLOAD FAILED: ${e.message}")
        null
    }

    /** Ensures the owner (you) exists and keeps their details in sync with your profile. */
    fun ensureOwnerTraveller(tripId: String) {
        val idx = trips.indexOfFirst { it.id == tripId }
        if (idx < 0) return
        val trip = trips[idx]

        if (trip.ownerId != authService.currentUid) return

        val existingOwner = trip.travellers.firstOrNull { it.isOwner }

        val owner = Traveller(
            id = existingOwner?.id ?: "owner_${authService.currentUid ?: "me"}",
            firstName = profile.name.ifBlank { "Me" },
            surname = profile.surname,
            email = profile.email,
            phone = profile.mobile,
            colorIndex = existingOwner?.colorIndex ?: 0,
            isOwner = true,
            userId = authService.currentUid ?: "",
        )

        if (existingOwner == owner) return

        val withoutOwner = trip.travellers.filterNot { it.isOwner }
        val updated = trip.copy(travellers = listOf(owner) + withoutOwner)
        trips[idx] = updated
        persist(updated)
    }

    /**
     * Keep the travellers list in sync with the trip's members: every member gets a
     * matching traveller (linked by userId) for expense-splitting, and auto-linked
     * travellers for members who left are removed. Manual travellers (blank userId,
     * e.g. non-app people) are never touched. Owner-only; runs on trip open.
     */
    fun reconcileMembersToTravellers(tripId: String) {
        val idx = trips.indexOfFirst { it.id == tripId }
        if (idx < 0) return
        val trip = trips[idx]
        // Only the actual owner maintains the shared travellers list.
        if (trip.ownerId != authService.currentUid) return

        val memberUids = trip.members.keys
        val covered = trip.travellers
            .mapNotNull { it.userId.takeIf { u -> u.isNotBlank() } }
            .toSet()

        // 1) Remove auto-linked travellers whose member has left.
        //    (Only ones we linked: userId set, not the owner row, not a current member.)
        var newTravellers = trip.travellers.filterNot { t ->
            t.userId.isNotBlank() && t.userId !in memberUids && !t.isOwner
        }

        // 2) Add a traveller for any member not yet represented (skip the owner —
        //    they already have their owner-traveller from ensureOwnerTraveller).
        var colorCursor = (newTravellers.maxOfOrNull { it.colorIndex } ?: -1) + 1
        for (uid in memberUids) {
            if (uid == trip.ownerId) continue
            if (uid in covered) continue

            val info = trip.memberInfo[uid]
            val fullName = info?.name?.takeIf { it.isNotBlank() }
                ?: info?.email?.takeIf { it.isNotBlank() }
                ?: uid.take(6)
            val parts = fullName.trim().split(" ")
            val first = parts.firstOrNull().orEmpty()
            val rest = parts.drop(1).joinToString(" ")

            newTravellers = newTravellers + com.itinera.app.model.Traveller(
                id = "m_$uid",                       // deterministic → no duplicates on re-run
                firstName = first,
                surname = rest,
                email = info?.email ?: "",
                colorIndex = colorCursor,
                isOwner = false,
                userId = uid,
            )
            colorCursor += 1
        }

        if (newTravellers != trip.travellers) {
            val updated = trip.copy(travellers = newTravellers)
            trips[idx] = updated
            persist(updated)
        }
    }

    fun addTraveller(tripId: String, traveller: Traveller) {
        val idx = trips.indexOfFirst { it.id == tripId }
        if (idx < 0) return
        val updated = trips[idx].copy(travellers = trips[idx].travellers + traveller)
        trips[idx] = updated
        persist(updated)
    }

    fun updateTraveller(tripId: String, traveller: Traveller) {
        val idx = trips.indexOfFirst { it.id == tripId }
        if (idx < 0) return
        val updated = trips[idx].copy(
            travellers = trips[idx].travellers.map { if (it.id == traveller.id) traveller else it },
        )
        trips[idx] = updated
        persist(updated)
    }

    fun removeTraveller(tripId: String, travellerId: String) {
        val idx = trips.indexOfFirst { it.id == tripId }
        if (idx < 0) return
        val trip = trips[idx]
        val target = trip.travellers.firstOrNull { it.id == travellerId } ?: return
        if (target.isOwner) return   // never remove the owner
        val updated = trip.copy(travellers = trip.travellers.filterNot { it.id == travellerId })
        trips[idx] = updated
        persist(updated)
    }

    // ===== expenses (now under trips/{tripId}/expenses) =====
    fun expensesForTrip(tripId: String): List<Expense> =
        expenses.filter { it.tripId == tripId }

    fun addExpense(expense: Expense) {
        val stamped = expense.copy(memberIds = memberIdsForTrip(expense.tripId))
        expenses.add(stamped)
        ioScope.launch { runCatching { expenseService.saveExpense(stamped) } }
    }

    fun updateExpense(expense: Expense) {
        val stamped = expense.copy(memberIds = memberIdsForTrip(expense.tripId))
        val i = expenses.indexOfFirst { it.id == expense.id }
        if (i >= 0) expenses[i] = stamped
        ioScope.launch { runCatching { expenseService.saveExpense(stamped) } }
    }

    fun deleteExpense(expenseId: String) {
        val exp = expenses.firstOrNull { it.id == expenseId }
        expenses.removeAll { it.id == expenseId }
        if (exp != null) ioScope.launch { runCatching { expenseService.deleteExpense(exp.tripId, expenseId) } }
    }

    /** No longer needed for live sync; kept as a no-op so Backup "Sync Now" compiles. */
    suspend fun loadExpenses(uid: String) { /* handled by live collection-group sync */ }

    fun setTripCurrency(tripId: String, code: String) {
        val i = trips.indexOfFirst { it.id == tripId }
        if (i >= 0) {
            trips[i] = trips[i].copy(currencyCode = code)
            persist(trips[i])
        }
    }

    fun setTripSettled(tripId: String, settled: Boolean) {
        val uid = authService.currentUid ?: return
        val i = trips.indexOfFirst { it.id == tripId }
        if (i < 0) return
        if (!trips[i].isOwnedBy(uid)) return          // owner only
        trips[i] = trips[i].copy(
            settledAt = if (settled) nowMillis() else 0L,
            settledBy = if (settled) uid else "",
        )
        persist(trips[i])
    }

    fun paymentsForTrip(tripId: String): List<com.itinera.app.model.Payment> =
        payments.filter { it.tripId == tripId }

    fun addPayment(tripId: String, fromTravellerId: String, toTravellerId: String, amount: Double) {
        val payment = com.itinera.app.model.Payment(
            id = "pay_${kotlin.random.Random.nextLong()}",
            tripId = tripId,
            fromTravellerId = fromTravellerId,
            toTravellerId = toTravellerId,
            amount = amount,
            createdAt = nowMillis(),
            memberIds = memberIdsForTrip(tripId),
        )
        payments.add(payment)
        ioScope.launch { runCatching { paymentService.savePayment(payment) } }
    }

    fun deletePayment(paymentId: String) {
        val p = payments.firstOrNull { it.id == paymentId }
        payments.removeAll { it.id == paymentId }
        if (p != null) ioScope.launch { runCatching { paymentService.deletePayment(p.tripId, paymentId) } }
    }

    // ===== one-time migrations =====

    /**
     * Copy trips from the old users/{uid}/trips into shared top-level trips/, stamping
     * the current user as owner. Runs once (gated by profile.migratedToShared). Does NOT
     * delete the old data.
     */
    suspend fun migrateToSharedIfNeeded(uid: String) {
        if (profile.migratedToShared) return
        try {
            val legacy = tripService.loadLegacyTrips(uid)
            println("ITINERA: TRIP MIGRATION — found ${legacy.size} legacy trips")
            for (old in legacy) {
                val migrated = old.copy(
                    ownerId = uid,
                    members = mapOf(uid to "owner"),
                    memberIds = listOf(uid),
                )
                tripService.saveTrip(migrated)
            }
            val updated = profile.copy(migratedToShared = true)
            profile = updated
            profileService.saveProfile(uid, updated)
            println("ITINERA: TRIP MIGRATION — done")
        } catch (e: Exception) {
            println("ITINERA: TRIP MIGRATION FAILED — ${e.message}")
        }
    }

    /**
     * Copy documents/expenses from old users/{uid}/... into trips/{tripId}/..., stamping
     * memberIds from their parent trip. Runs once (gated by profile.migratedDocsExpenses).
     * Must run AFTER migrateToSharedIfNeeded so memberIdsForTrip resolves.
     */
    suspend fun migrateDocsExpensesIfNeeded(uid: String) {
        if (profile.migratedDocsExpenses) return
        try {
            val legacyDocs = docService.loadLegacyDocuments(uid)
            for (d in legacyDocs) {
                val ids = memberIdsForTrip(d.tripId).ifEmpty { listOf(uid) }
                docService.saveDocument(d.copy(memberIds = ids))
            }
            val legacyExp = expenseService.loadLegacyExpenses(uid)
            for (e in legacyExp) {
                val ids = memberIdsForTrip(e.tripId).ifEmpty { listOf(uid) }
                expenseService.saveExpense(e.copy(memberIds = ids))
            }
            val updated = profile.copy(migratedDocsExpenses = true)
            profile = updated
            profileService.saveProfile(uid, updated)
            println("ITINERA: DOCS/EXP MIGRATION — ${legacyDocs.size} docs, ${legacyExp.size} expenses")
        } catch (e: Exception) {
            println("ITINERA: DOCS/EXP MIGRATION FAILED — ${e.message}")
        }
    }

    // ===== real-time sync =====

    /** Start live sync for trips, documents, and expenses (collection-group for the latter two). */
    fun startSync() {
        val uid = authService.currentUid ?: return
        stopSync()
        syncJobs += ioScope.launch {
            tripService.tripsFlow(uid)
                .catch { e -> println("ITINERA: trips sync error — ${e.message}") }
                .collect { remote -> applyTrips(remote); tripsSyncedOnce = true }
        }
        syncJobs += ioScope.launch {
            docService.documentsFlow(uid)
                .catch { e -> println("ITINERA: docs sync error — ${e.message}") }
                .collect { applyDocuments(it); documentsSyncedOnce = true }
        }
        syncJobs += ioScope.launch {
            expenseService.expensesFlow(uid)
                .catch { e -> println("ITINERA: expenses sync error — ${e.message}") }
                .collect { applyExpenses(it); expensesSyncedOnce = true }
        }

        syncJobs += ioScope.launch {
            activityService.activitiesFlow(uid)
                .catch { e -> println("ITINERA: activities sync error — ${e.message}") }
                .collect { applyActivities(it); activitiesSyncedOnce = true }
        }
        syncJobs += ioScope.launch {
            checklistService.checklistFlow(uid)
                .catch { e -> println("ITINERA: checklist sync error — ${e.message}") }
                .collect { applyChecklist(it); checklistSyncedOnce = true }
        }
        syncJobs += ioScope.launch {
            paymentService.paymentsFlow(uid)
                .catch { e -> println("ITINERA: payments sync error — ${e.message}") }
                .collect { applyPayments(it); paymentsSyncedOnce = true }
        }
    }

    /** Stop all live listeners (call on logout). */
    fun stopSync() {
        syncJobs.forEach { it.cancel() }
        syncJobs.clear()
        tripsListener?.cancel()
        tripsListener = null
        tripsSyncedOnce = false
        documentsSyncedOnce = false
        expensesSyncedOnce = false
        activitiesSyncedOnce = false
        checklistSyncedOnce = false
        paymentsSyncedOnce = false



    }

    // Diff-apply: update only what changed, so lists don't flicker or lose scroll.
    private fun applyTrips(remote: List<Trip>) {
        trips.removeAll { local -> remote.none { it.id == local.id } }
        remote.forEach { r ->
            val i = trips.indexOfFirst { it.id == r.id }
            if (i >= 0) { if (trips[i] != r) trips[i] = r } else trips.add(r)
        }
    }

    private fun applyDocuments(remote: List<DocItem>) {
        documents.removeAll { local -> remote.none { it.id == local.id } }
        remote.forEach { r ->
            val i = documents.indexOfFirst { it.id == r.id }
            if (i >= 0) { if (documents[i] != r) documents[i] = r } else documents.add(r)
        }
    }

    private fun applyExpenses(remote: List<Expense>) {
        expenses.removeAll { local -> remote.none { it.id == local.id } }
        remote.forEach { r ->
            val i = expenses.indexOfFirst { it.id == r.id }
            if (i >= 0) { if (expenses[i] != r) expenses[i] = r } else expenses.add(r)
        }
    }
    private fun applyPayments(remote: List<com.itinera.app.model.Payment>) {
        payments.removeAll { local -> remote.none { it.id == local.id } }
        remote.forEach { r ->
            val i = payments.indexOfFirst { it.id == r.id }
            if (i >= 0) { if (payments[i] != r) payments[i] = r } else payments.add(r)
        }
    }

    private fun applyActivities(remote: List<Activity>) {
        activities.removeAll { local -> remote.none { it.id == local.id } }
        remote.forEach { r ->
            val i = activities.indexOfFirst { it.id == r.id }
            if (i >= 0) { if (activities[i] != r) activities[i] = r } else activities.add(r)
        }
    }

    private fun applyChecklist(remote: List<ChecklistItem>) {
        checklist.removeAll { local -> remote.none { it.id == local.id } }
        remote.forEach { r ->
            val i = checklist.indexOfFirst { it.id == r.id }
            if (i >= 0) { if (checklist[i] != r) checklist[i] = r } else checklist.add(r)
        }
    }

    // ===== invites / sharing =====

    /** Generate a short human code like "ITIN-A4F9". */
    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"   // no ambiguous O/0/I/1
        val suffix = (1..4).map { chars.random() }.joinToString("")
        return "ITIN-$suffix"
    }

    /**
     * Owner creates an invite for a trip. Returns the code to share, or null on failure.
     */
    suspend fun createTripInvite(tripId: String): String? {
        val uid = authService.currentUid ?: return null
        val trip = tripById(tripId) ?: return null
        return try {
            val code = generateInviteCode()
            val invite = com.itinera.app.model.Invite(
                id = "inv_${kotlin.random.Random.nextLong()}",
                tripId = tripId,
                tripTitle = trip.title,
                code = code,
                createdBy = uid,
                status = "active",
                createdAt = nowMillis(),
            )
            inviteService.createInvite(invite)
            code
        } catch (e: Exception) {
            println("ITINERA: CREATE INVITE FAILED — ${e.message}")
            null
        }
    }

    /**
     * Invitee joins a trip by code. Two-phase: add self as viewer (blind write),
     * then fan out to the trip's docs/expenses. Returns the trip title on success, null otherwise.
     */
    suspend fun joinTripByCode(code: String): String? {
        val uid = authService.currentUid ?: return null
        return try {
            val invite = inviteService.findByCode(code) ?: run {
                println("ITINERA: JOIN — no active invite for code $code")
                return null
            }
            // Phase 1: add self as viewer (makes us a member)
            val joinerName = listOf(profile.name, profile.surname).filter { it.isNotBlank() }.joinToString(" ")
            inviteService.joinTripAddSelf(invite.tripId, uid, joinerName, profile.email)
            // Phase 2: now a member, fan out to docs/expenses
            inviteService.fanOutToSubcollections(invite.tripId, uid)
            println("ITINERA: JOIN — joined ${invite.tripTitle}")
            invite.tripTitle

        } catch (e: Exception) {
            println("ITINERA: JOIN FAILED — ${e.message}")
            null
        }
    }

    /**
     * Leave a shared trip: remove self from the trip's docs/expenses/activities first
     * (while still a member), then from the trip itself. Owners cannot leave.
     * Returns true on success.
     */
    suspend fun leaveTrip(tripId: String): Boolean {
        val uid = authService.currentUid ?: return false
        val trip = tripById(tripId) ?: return false
        if (trip.ownerId == uid) return false   // owner can't leave their own trip
        return try {
            // Phase 1: remove self from sub-items while still permitted
            inviteService.removeSelfFromSubcollections(tripId, uid)
            // Phase 2: remove self from the trip
            inviteService.leaveTripRemoveSelf(tripId, uid)
            // Local: drop the trip + its sub-items from memory immediately
            trips.removeAll { it.id == tripId }
            documents.removeAll { it.tripId == tripId }
            expenses.removeAll { it.tripId == tripId }
            activities.removeAll { it.tripId == tripId }
            println("ITINERA: LEFT trip ${trip.title}")
            true
        } catch (e: Exception) {
            println("ITINERA: LEAVE FAILED — ${e.message}")
            false
        }
    }

    /** Owner changes a member's role (editor/viewer) or the owner can remove a member. */
    fun setMemberRole(tripId: String, memberUid: String, role: String) {
        val idx = trips.indexOfFirst { it.id == tripId }
        if (idx < 0) return
        val trip = trips[idx]
        val newMembers = trip.members.toMutableMap().apply { put(memberUid, role) }
        val newIds = (trip.memberIds + memberUid).distinct()
        val updated = trip.copy(members = newMembers, memberIds = newIds)
        trips[idx] = updated
        persist(updated)
    }

    fun removeMember(tripId: String, memberUid: String) {
        val idx = trips.indexOfFirst { it.id == tripId }
        if (idx < 0) return
        val trip = trips[idx]
        if (trip.ownerId == memberUid) return
        val newMembers = trip.members.toMutableMap().apply { remove(memberUid) }
        val newIds = trip.memberIds.filterNot { it == memberUid }
        val newInfo = trip.memberInfo.toMutableMap().apply { remove(memberUid) }   // ⬅ drop info
        val updated = trip.copy(members = newMembers, memberIds = newIds, memberInfo = newInfo)
        trips[idx] = updated
        persist(updated)
    }
}