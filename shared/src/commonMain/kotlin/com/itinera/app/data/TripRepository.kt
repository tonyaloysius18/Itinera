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
import kotlinx.coroutines.launch



/**
 * Repository holding app data as observable Compose state.
 *
 * Trips now persist to Firestore (via TripService): every trip mutation mirrors a
 * write to the cloud, and loadTrips() pulls the user's data on login. Documents,
 * checklist, and activities remain in-memory for now (next persistence increment).
 */
class TripRepository {

    val trips = mutableStateListOf<Trip>()
    val documents = mutableStateListOf<DocItem>()

    val docService = DocService()                        // ⬅ ADD

    val checklist = mutableStateListOf<ChecklistItem>()

    val unsplashApi = UnsplashApi()
    val authService = AuthService()
    val profileService = ProfileService()
    val tripService = TripService()

    val expenseService = ExpenseService()

    val expenses = mutableStateListOf<Expense>()

    val notificationScheduler = NotificationScheduler()

    private var tripsListener: Job? = null

    private val syncJobs = mutableListOf<Job>()

    var tripsSyncedOnce by mutableStateOf(false)
        private set

    var documentsSyncedOnce by mutableStateOf(false)
        private set
    var expensesSyncedOnce by mutableStateOf(false)
        private set


    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun nowMillis(): Long =
        kotlin.time.Clock.System.now().toEpochMilliseconds()

    // Schedule (or refresh) a reminder for one leg, based on the user's offset.
    private fun scheduleLegReminder(trip: Trip, leg: Leg) {
        val offset = profile.reminderOffsetMinutes
        // always cancel first so an edit replaces the old alarm
        notificationScheduler.cancel(leg.id)
        if (offset == ReminderOffset.OFF) return
        if (leg.completed) return
        val fireAt = legReminderFireTime(leg, offset, nowMillis()) ?: return
        val title = "${leg.fromCity} → ${leg.toCity}"
        val body = "Departing ${leg.timeLabel.ifBlank { "soon" }} · ${trip.title}"
        notificationScheduler.schedule(leg.id, title, body, fireAt)
    }

    private val uploadClient = HttpClient()

    // Background scope for fire-and-forget cloud writes (mutations stay synchronous for the UI).
    private val ioScope = CoroutineScope(Dispatchers.Default + SupervisorJob())   // ⬅ ADD

    // ===== cloud helpers =====
    private fun persist(trip: Trip) {
        val uid = authService.currentUid ?: run { println("ITINERA: PERSIST skipped — no uid"); return }
        ioScope.launch {
            try {
                tripService.saveTrip(uid, trip)
                println("ITINERA: PERSIST ok — ${trip.title}")
            } catch (e: Exception) {
                println("ITINERA: PERSIST FAILED — ${e.message}")
            }
        }
    }

    private fun removeRemote(tripId: String) {                         // ⬅ ADD
        val uid = authService.currentUid ?: return
        ioScope.launch { runCatching { tripService.deleteTrip(uid, tripId) } }
    }

    /** Replace the in-memory trips with the user's cloud data. Call on login. */
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
        val id = "trip_${kotlin.random.Random.nextLong()}"
        val trip = Trip(
            id = id,
            title = title.trim(),
            countriesCount = 0,
            dateRange = "",
            accent = TripAccent.values().random(),
            legs = emptyList(),
        )
        trips.add(trip)
        persist(trip)                                                  // ⬅ ADD
        return id
    }

    fun updateTripImage(id: String, url: String) {
        val i = trips.indexOfFirst { it.id == id }
        if (i >= 0) {
            trips[i] = trips[i].copy(imageUrl = url)
            persist(trips[i])                                          // ⬅ ADD
        }
    }

    fun updateTrip(id: String, title: String) {
        val i = trips.indexOfFirst { it.id == id }
        if (i >= 0) {
            trips[i] = trips[i].copy(title = title.trim())
            persist(trips[i])                                          // ⬅ ADD
        }
    }

    fun deleteTrip(id: String) {
        trips.removeAll { it.id == id }
        removeRemote(id)                                               // ⬅ ADD
    }

    fun togglePin(id: String) {
        val i = trips.indexOfFirst { it.id == id }
        if (i >= 0) {
            trips[i] = trips[i].copy(pinned = !trips[i].pinned)
            persist(trips[i])                                          // ⬅ ADD
        }
    }

    fun toggleArchive(id: String) {
        val i = trips.indexOfFirst { it.id == id }
        if (i >= 0) {
            trips[i] = trips[i].copy(archived = !trips[i].archived)
            persist(trips[i])                                          // ⬅ ADD
        }
    }

    fun activeTrips(): List<Trip> =
        trips.filter { !it.archived }.sortedByDescending { it.pinned }

    fun archivedTrips(): List<Trip> = trips.filter { it.archived }

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
        persist(trips[tripIndex])                                      // ⬅ ADD
    }

    fun toggleChecklistItem(itemId: String) {
        val index = checklist.indexOfFirst { it.id == itemId }
        if (index < 0) return
        val item = checklist[index]
        checklist[index] = item.copy(done = !item.done)
    }

    fun addChecklistItem(tripId: String, text: String, group: String) {
        checklist.add(
            ChecklistItem(
                id = "c_${kotlin.random.Random.nextLong()}",
                tripId = tripId,
                text = text.trim(),
                group = group,
            )
        )
    }

    fun addLeg(tripId: String, leg: Leg) {
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        trips[index] = trip.copy(legs = trip.legs + leg)
        persist(trips[index])
        scheduleLegReminder(trips[index], leg)    // ⬅ ADD
    }

    fun updateLeg(tripId: String, leg: Leg) {
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        trips[index] = trip.copy(legs = trip.legs.map { if (it.id == leg.id) leg else it })
        persist(trips[index])
        scheduleLegReminder(trips[index], leg)                 // ⬅ ADD

    }

    fun deleteLeg(tripId: String, legId: String) {
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        trips[index] = trip.copy(legs = trip.legs.filterNot { it.id == legId })
        persist(trips[index])
        notificationScheduler.cancel(legId)
    }

    fun markLegAddedToCalendar(tripId: String, legId: String) {        // ⬅ FIXED (was calling missing updateTripLegs)
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        val newLegs = trip.legs.map {
            if (it.id == legId) it.copy(addedToCalendar = true) else it
        }
        trips[index] = trip.copy(legs = newLegs)
        persist(trips[index])                                          // ⬅ ADD
    }

    fun rescheduleAllReminders() {
        trips.forEach { trip -> trip.legs.forEach { leg -> scheduleLegReminder(trip, leg) } }
    }

    // ===== activities (still in-memory) =====
    val activities = mutableStateListOf<Activity>()
    fun activitiesForTrip(tripId: String): List<Activity> =
        activities.filter { it.tripId == tripId }

    fun addActivity(tripId: String, date: LocalDate, title: String, time: String, location: String) {
        activities.add(
            Activity(
                id = "a_${kotlin.random.Random.nextLong()}",
                tripId = tripId,
                date = date,
                title = title.trim(),
                time = time.trim(),
                location = location.trim(),
            )
        )
    }

    fun updateActivity(id: String, date: LocalDate, title: String, time: String, location: String) {
        val i = activities.indexOfFirst { it.id == id }
        if (i >= 0) activities[i] = activities[i].copy(
            date = date, title = title.trim(), time = time.trim(), location = location.trim(),
        )
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

    fun toggleActivity(id: String) {
        val i = activities.indexOfFirst { it.id == id }
        if (i >= 0) activities[i] = activities[i].copy(completed = !activities[i].completed)
    }

    fun deleteActivity(id: String) {
        activities.removeAll { it.id == id }
    }

    // ===== profile =====
    var profile by mutableStateOf(UserProfile())
        private set

    fun updateProfile(updated: UserProfile) { profile = updated }

    suspend fun uploadProfilePhoto(uid: String, bytes: ByteArray): String {
        return uploadBytesToStorage(uploadClient, bytes)
    }

    fun clearLocal() {
        stopSync()
        trips.clear()
        documents.clear()
        checklist.clear()
        activities.clear()
        profile = UserProfile()
        expenses.clear()

    }

    fun addDocument(doc: DocItem) {
        documents.add(doc)
        val uid = authService.currentUid ?: return
        ioScope.launch { runCatching { docService.saveDocument(uid, doc) } }
    }

    fun deleteDocument(docId: String) {
        documents.removeAll { it.id == docId }
        val uid = authService.currentUid ?: return
        ioScope.launch { runCatching { docService.deleteDocument(uid, docId) } }
    }

    suspend fun loadDocuments(uid: String) {
        runCatching {
            val remote = docService.loadDocuments(uid)
            documents.clear()
            documents.addAll(remote)
        }
    }

    suspend fun addDocumentWithFile(
        tripId: String,
        title: String,
        category: String,
        file: PickedFile,
    ): Boolean {
        val uid = authService.currentUid ?: return false
        return try {
            val url = uploadFileToStorage(uploadClient, file.bytes, file.fileName, file.mimeType)
            val doc = DocItem(
                id = "doc_${kotlin.random.Random.nextLong()}",
                tripId = tripId,
                title = title,
                category = category,
                fileName = file.fileName,
                fileUrl = url,
                mimeType = file.mimeType,
            )
            documents.add(doc)
            docService.saveDocument(uid, doc)
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

    /** Ensures the owner (you) is present as a traveller; backfills older trips. */
    /** Ensures the owner (you) is present as a traveller; rebuilds a broken one. */
    /** Ensures the owner (you) exists and keeps their details in sync with your profile. */
    /** Ensures the owner (you) exists and keeps their details in sync with your profile. */
    fun ensureOwnerTraveller(tripId: String) {
        val idx = trips.indexOfFirst { it.id == tripId }
        if (idx < 0) return
        val trip = trips[idx]

        val existingOwner = trip.travellers.firstOrNull { it.isOwner }

        val owner = Traveller(
            id = existingOwner?.id ?: "owner_${authService.currentUid ?: "me"}",
            firstName = profile.name.ifBlank { "Me" },     // ⬅ profile.name
            surname = profile.surname,                      // ⬅ profile.surname
            email = profile.email,                          // ⬅ profile.email
            phone = profile.mobile,                         // ⬅ profile.mobile → Traveller.phone
            colorIndex = existingOwner?.colorIndex ?: 0,
            isOwner = true,
            userId = authService.currentUid ?: "",
        )

        // skip the write if nothing changed (avoids spamming Firestore on every open)
        if (existingOwner == owner) return

        val withoutOwner = trip.travellers.filterNot { it.isOwner }
        val updated = trip.copy(travellers = listOf(owner) + withoutOwner)
        trips[idx] = updated
        persist(updated)
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

    fun expensesForTrip(tripId: String): List<Expense> =
        expenses.filter { it.tripId == tripId }

    fun addExpense(expense: Expense) {
        expenses.add(expense)
        val uid = authService.currentUid ?: return
        ioScope.launch { runCatching { expenseService.saveExpense(uid, expense) } }
    }

    fun updateExpense(expense: Expense) {
        val i = expenses.indexOfFirst { it.id == expense.id }
        if (i >= 0) expenses[i] = expense
        val uid = authService.currentUid ?: return
        ioScope.launch { runCatching { expenseService.saveExpense(uid, expense) } }
    }

    fun deleteExpense(expenseId: String) {
        expenses.removeAll { it.id == expenseId }
        val uid = authService.currentUid ?: return
        ioScope.launch { runCatching { expenseService.deleteExpense(uid, expenseId) } }
    }

    suspend fun loadExpenses(uid: String) {
        runCatching {
            val remote = expenseService.loadExpenses(uid)
            expenses.clear()
            expenses.addAll(remote)
        }
    }

    fun setTripCurrency(tripId: String, code: String) {
        val i = trips.indexOfFirst { it.id == tripId }
        if (i >= 0) {
            trips[i] = trips[i].copy(currencyCode = code)
            persist(trips[i])
        }
    }

    /** Start live trip sync. Cancels any previous listener first. */
    fun startTripSync() {
        val uid = authService.currentUid ?: return
        tripsListener?.cancel()
        tripsListener = ioScope.launch {
            tripService.tripsFlow(uid)
                .catch { e -> println("ITINERA: trips sync error — ${e.message}") }
                .collect { remote ->
                    trips.clear()
                    trips.addAll(remote)
                    println("ITINERA: trips sync — ${remote.size} trips")
                }
        }
    }

    /** Stop live sync (call on logout). */
    fun stopTripSync() {
        tripsListener?.cancel()
        tripsListener = null
    }

    /** Start live sync for trips, documents, and expenses. Replaces one-time loads. */
    fun startSync() {
        val uid = authService.currentUid ?: return
        stopSync()
        syncJobs += ioScope.launch {
            tripService.tripsFlow(uid)
                .catch { e -> println("ITINERA: trips sync error — ${e.message}") }
                .collect { remote -> applyTrips(remote)
                    tripsSyncedOnce = true  }
        }
        syncJobs += ioScope.launch {
            docService.documentsFlow(uid)
                .catch { e -> println("ITINERA: docs sync error — ${e.message}") }
                //.collect { remote -> applyDocuments(remote) }
                .collect { applyDocuments(it); documentsSyncedOnce = true }

        }
        syncJobs += ioScope.launch {
            expenseService.expensesFlow(uid)
                .catch { e -> println("ITINERA: expenses sync error — ${e.message}") }
                //.collect { remote -> applyExpenses(remote) }
                .collect { applyExpenses(it); expensesSyncedOnce = true }

        }


    }

    /** Stop all live listeners (call on logout). */
    fun stopSync() {
        syncJobs.forEach { it.cancel() }
        syncJobs.clear()
        tripsSyncedOnce = false
        documentsSyncedOnce = false
        expensesSyncedOnce = false
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

}