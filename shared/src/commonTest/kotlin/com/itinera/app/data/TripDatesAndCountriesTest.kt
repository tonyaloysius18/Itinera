package com.itinera.app.data

import com.itinera.app.model.Leg
import com.itinera.app.model.TransportType
import com.itinera.app.model.Trip
import com.itinera.app.model.TripAccent
import com.itinera.app.model.scheduleDates
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class TripDatesAndCountriesTest {

    private fun trip(
        legs: List<Leg> = emptyList(),
        start: LocalDate? = null,
        end: LocalDate? = null,
        countries: List<String> = emptyList(),
    ) = Trip(
        id = "t", title = "Test", countriesCount = 0, dateRange = "", accent = TripAccent.BLUE, legs = legs,
        startDate = start, endDate = end, destinationCountries = countries,
    )

    private fun leg(date: String, country: String = "") = Leg(
        id = "l$date", fromCity = "A", toCity = "B", transport = TransportType.TRAIN,
        date = LocalDate.parse(date), country = country,
    )

    @Test
    fun legsWinOverPlannedDates() {
        val t = trip(legs = listOf(leg("2026-10-05"), leg("2026-10-03")), start = LocalDate.parse("2026-01-01"))
        assertEquals(listOf(LocalDate.parse("2026-10-03"), LocalDate.parse("2026-10-05")), t.scheduleDates())
    }

    @Test
    fun tripWithoutLegsUsesPlannedStartAndEnd() {
        val t = trip(start = LocalDate.parse("2027-01-06"), end = LocalDate.parse("2027-01-15"))
        assertEquals(listOf(LocalDate.parse("2027-01-06"), LocalDate.parse("2027-01-15")), t.scheduleDates())
    }

    @Test
    fun tripWithNoDatesAtAllIsEmpty() {
        assertEquals(emptyList(), trip().scheduleDates())
    }

    @Test
    fun countriesFromLegsAreCountedCaseInsensitively() {
        val t = trip(legs = listOf(leg("2026-10-03", "Hungary"), leg("2026-10-04", "hungary"), leg("2026-10-05", "Austria")))
        assertEquals(2, t.countriesCovered())
    }

    @Test
    fun legCountriesTakePriorityOverDestinationCountries() {
        val t = trip(legs = listOf(leg("2026-10-03", "Hungary")), countries = listOf("Austria", "Germany"))
        assertEquals(1, t.countriesCovered())
        assertEquals("Hungary", t.primaryCountry())
    }

    @Test
    fun tripWithoutLegsFallsBackToDestinationCountries() {
        val t = trip(countries = listOf("United Kingdom", " united kingdom ", "", "Ireland"))
        assertEquals(2, t.countriesCovered())
        assertEquals("United Kingdom", t.primaryCountry())
    }

    @Test
    fun tripWithNothingHasNoCountries() {
        assertEquals(0, trip().countriesCovered())
        assertEquals("", trip().primaryCountry())
    }
}
