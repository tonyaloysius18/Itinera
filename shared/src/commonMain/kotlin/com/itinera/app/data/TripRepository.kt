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
import kotlinx.datetime.LocalDate


/**
 * In-memory repository holding all app data as observable Compose state.
 *
 * This is the single seam to replace when you add persistence/sync:
 *  - For local persistence, back these lists with SQLDelight or Room (KMP).
 *  - For cloud sync, mirror writes to Firestore behind the same function names.
 * The UI never talks to storage directly, so swapping this out touches no screens.
 */
class TripRepository {

    val trips = mutableStateListOf<Trip>().apply { addAll(SampleData.trips) }
    val documents = mutableStateListOf<DocItem>().apply { addAll(SampleData.documents) }
    val checklist = mutableStateListOf<ChecklistItem>().apply { addAll(SampleData.checklist) }

    val unsplashApi = UnsplashApi()

    val authService = AuthService()

    fun addTrip(title: String): String {                                  // ⬅ no accent param
        val id = "trip_${kotlin.random.Random.nextLong()}"
        trips.add(
            Trip(
                id = id,
                title = title.trim(),
                countriesCount = 0,
                dateRange = "",
                accent = TripAccent.values().random(),                     // ⬅ random accent
                legs = emptyList(),
            )
        )
        return id
    }

    fun updateTripImage(id: String, url: String) {
        val i = trips.indexOfFirst { it.id == id }
        if (i >= 0) trips[i] = trips[i].copy(imageUrl = url)
    }

    fun updateTrip(id: String, title: String) {                           // ⬅ no accent param
        val i = trips.indexOfFirst { it.id == id }
        if (i >= 0) trips[i] = trips[i].copy(title = title.trim())         // ⬅ keeps existing accent
    }

    fun deleteTrip(id: String) {
        trips.removeAll { it.id == id }
    }

    fun togglePin(id: String) {
        val i = trips.indexOfFirst { it.id == id }
        if (i >= 0) trips[i] = trips[i].copy(pinned = !trips[i].pinned)
    }

    fun toggleArchive(id: String) {
        val i = trips.indexOfFirst { it.id == id }
        if (i >= 0) trips[i] = trips[i].copy(archived = !trips[i].archived)
    }

    // trips for the main list: not archived, pinned ones first
    fun activeTrips(): List<Trip> =
        trips.filter { !it.archived }
            .sortedByDescending { it.pinned }   // pinned = true sorts first; stable otherwise

    // trips for the archived screen
    fun archivedTrips(): List<Trip> =
        trips.filter { it.archived }

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


    fun addLeg(tripId: String, leg: com.itinera.app.model.Leg) {
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        trips[index] = trip.copy(legs = trip.legs + leg)
    }

    fun updateLeg(tripId: String, leg: Leg) {
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        trips[index] = trip.copy(legs = trip.legs.map { if (it.id == leg.id) leg else it })
    }

    fun deleteLeg(tripId: String, legId: String) {
        val index = trips.indexOfFirst { it.id == tripId }
        if (index < 0) return
        val trip = trips[index]
        trips[index] = trip.copy(legs = trip.legs.filterNot { it.id == legId })
    }

    val activities = mutableStateListOf<Activity>().apply { addAll(SampleData.activities) }

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

    /** Day number (1-based) for a date within a trip; earliest distinct date = Day 1. */
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

    var profile by mutableStateOf(
        UserProfile(
            name = "Tony",
            surname = "Aloysius",
            email = "tony.aloysius@gmail.com",
            dob = "01/01/1990",
            street = "",
            city = "",
            postalCode = "",
        )
    )
        private set

    fun updateProfile(updated: UserProfile) { profile = updated }
}
