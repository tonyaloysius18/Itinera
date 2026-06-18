package com.itinera.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.itinera.app.model.ChecklistItem
import com.itinera.app.model.DocItem
import com.itinera.app.model.Leg
import com.itinera.app.model.Trip
import com.itinera.app.model.UserProfile
import com.itinera.app.model.Activity
import com.itinera.app.model.TripAccent
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate


/**
 * Repository holding app data as observable Compose state.
 *
 * Trips now persist to Firestore (via TripService): every trip mutation mirrors a
 * write to the cloud, and loadTrips() pulls the user's data on login. Documents,
 * checklist, and activities remain in-memory for now (next persistence increment).
 */
class TripRepository {

    val trips = mutableStateListOf<Trip>()
    val documents = mutableStateListOf<DocItem>().apply { addAll(SampleData.documents) }
    val checklist = mutableStateListOf<ChecklistItem>()

    val unsplashApi = UnsplashApi()
    val authService = AuthService()
    val profileService = ProfileService()
    val tripService = TripService()                                    // ⬅ ADD

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
        persist(trips[index])                                          // ⬅ ADD
    }

    fun updateLeg(tripId: String, leg: Leg) {
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        trips[index] = trip.copy(legs = trip.legs.map { if (it.id == leg.id) leg else it })
        persist(trips[index])                                          // ⬅ ADD
    }

    fun deleteLeg(tripId: String, legId: String) {
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        trips[index] = trip.copy(legs = trip.legs.filterNot { it.id == legId })
        persist(trips[index])                                          // ⬅ ADD
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
        trips.clear()
        documents.clear()
        checklist.clear()
        activities.clear()
        profile = UserProfile()
    }
}