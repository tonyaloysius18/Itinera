package com.itinera.app.data

import com.itinera.app.model.Activity
import com.itinera.app.model.ChecklistItem
import com.itinera.app.model.DocItem
import com.itinera.app.model.DocType
import com.itinera.app.model.Leg
import com.itinera.app.model.Trip
import com.itinera.app.model.TripAccent
import com.itinera.app.model.TransportType
import kotlinx.datetime.LocalDate


/** Seed data so every screen has something to show on first run. */
object SampleData {

    val trips = listOf(
        Trip(
            id = "balkans",
            title = "Balkans loop",
            countriesCount = 4,
            dateRange = "12–24 Jun",
            accent = TripAccent.BLUE,
            legs = listOf(
                Leg(id = "l1", fromCity = "Toulouse", toCity = "Belgrade", transport = TransportType.FLIGHT, date = LocalDate(2026, 6, 12), timeLabel = "09:40", bookingRef = "AF-2210", completed = true),
                Leg(id = "l2", fromCity = "Belgrade", toCity = "Sarajevo", transport = TransportType.TRAIN, date = LocalDate(2026, 6, 15), timeLabel = "07:15", bookingRef = "RS-118", completed = true),
                Leg(id = "l3", fromCity = "Sarajevo", toCity = "Mostar", transport = TransportType.BUS, date = LocalDate(2026, 6, 18), timeLabel = "14:00", bookingRef = "MK-4471"),
                Leg(id = "l4", fromCity = "Mostar", toCity = "Dubrovnik", transport = TransportType.BUS, date = LocalDate(2026, 6, 21), timeLabel = "10:30"),
            ),
            // Balkans loop:
            imageUrl = "https://images.unsplash.com/photo-1565008447742-97f6f38c985c?w=800",   // Mostar bridge
        ),
        Trip(
            id = "iberia",
            title = "Iberia by rail",
            countriesCount = 2,
            dateRange = "3–9 Aug",
            accent = TripAccent.GREEN,
            legs = listOf(
                Leg(id = "i1", fromCity = "Lisbon", toCity = "Madrid", transport = TransportType.TRAIN, date = LocalDate(2026, 8, 3), timeLabel = "08:00"),
                Leg(id = "i2", fromCity = "Madrid", toCity = "Barcelona", transport = TransportType.TRAIN, date = LocalDate(2026, 8, 6), timeLabel = "12:30"),
            ),
            imageUrl = "https://images.unsplash.com/photo-1539037116277-4db20889f2d4?w=800",   // Barcelona
        ),
    )

    val documents = listOf(
        DocItem("d1", "balkans", "Flight BEG", DocType.PDF, "leg 1"),
        DocItem("d2", "balkans", "Train pass", DocType.IMAGE, "leg 2"),
        DocItem("d3", "balkans", "Hotel conf.", DocType.PDF, "trip"),
        DocItem("d4", "balkans", "Bus QR", DocType.IMAGE, "leg 3"),
    )

    val checklist = listOf(
        ChecklistItem("c1", "balkans", "Passport valid 6+ months", "Documents", done = true),
        ChecklistItem("c2", "balkans", "Travel insurance", "Documents", done = true),
        ChecklistItem("c3", "balkans", "Check visa for Serbia", "Documents"),
        ChecklistItem("c4", "balkans", "Sarajevo hostel", "Bookings", done = true),
        ChecklistItem("c5", "balkans", "Mostar bus tickets", "Bookings"),
    )

    val activities = listOf(
        Activity("a1", "balkans", date = LocalDate(2026, 6, 12), title = "Kalemegdan Fortress", time = "16:00", location = "Belgrade"),
        Activity("a2", "balkans", date = LocalDate(2026, 6, 15), title = "Baščaršija old bazaar", time = "11:00", location = "Sarajevo"),
        Activity("a3", "balkans", date = LocalDate(2026, 6, 18), title = "Stari Most bridge", time = "10:00", location = "Mostar"),
    )
}
