package com.itinera.app.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PassVisibilityTest {
    private val trip = Trip(
        id = "t1",
        title = "Barcelona",
        countriesCount = 1,
        dateRange = "",
        accent = TripAccent.BLUE,
        legs = emptyList(),
        ownerId = "uOwner",
        members = mapOf("uOwner" to "owner", "uJow" to "editor", "uSam" to "viewer"),
        travellers = listOf(
            Traveller(id = "owner_uOwner", firstName = "Tony", isOwner = true, userId = "uOwner"),
            Traveller(id = "m_uJow", firstName = "Jow", userId = "uJow"),
            Traveller(id = "m_uSam", firstName = "Sam", userId = "uSam"),
        ),
    )
    private fun pass(travellerId: String) =
        DocItem(id = "d_$travellerId", tripId = "t1", title = "Pass", legId = "leg1", travellerId = travellerId)

    @Test fun ownerSeesEveryonesPasses() {
        assertTrue(trip.canSeeDocument(pass("owner_uOwner"), "uOwner"))
        assertTrue(trip.canSeeDocument(pass("m_uJow"), "uOwner"))
        assertTrue(trip.canSeeDocument(pass("m_uSam"), "uOwner"))
    }

    @Test fun memberSeesOnlyTheirOwnPass() {
        assertTrue(trip.canSeeDocument(pass("m_uJow"), "uJow"))
        assertFalse(trip.canSeeDocument(pass("owner_uOwner"), "uJow"))
        assertFalse(trip.canSeeDocument(pass("m_uSam"), "uJow"))
    }

    @Test fun editorsAreNotOwners() {
        // Being able to edit the trip doesn't unlock other people's passes.
        assertFalse(trip.canSeeDocument(pass("m_uSam"), "uJow"))
    }

    @Test fun unassignedDocumentsStayShared() {
        assertTrue(trip.canSeeDocument(pass(""), "uSam"))
    }

    @Test fun assignedPassIsSharedWithOwnerAndHolderOnly() {
        val t = trip.copy(memberIds = listOf("uOwner", "uJow", "uSam"))
        assertEquals(DocumentAccess(listOf("uOwner", "uJow"), listOf("uJow")), t.documentAccess(listOf("m_uJow")))
        assertEquals(DocumentAccess(listOf("uOwner"), listOf("uOwner")), t.documentAccess(listOf("owner_uOwner")))
        assertEquals(DocumentAccess(listOf("uOwner", "uJow", "uSam"), emptyList()), t.documentAccess(emptyList()))
        // A traveller with no account: only the owner gets it.
        assertEquals(DocumentAccess(listOf("uOwner"), emptyList()), t.documentAccess(listOf("someone_offline")))
    }

    @Test fun onePassForTwoTravellers() {
        val t = trip.copy(memberIds = listOf("uOwner", "uJow", "uSam"))
        // One QR for the whole booking: the owner and both passengers get it, Sam doesn't.
        assertEquals(DocumentAccess(listOf("uOwner", "uJow"), listOf("uOwner", "uJow")),
            t.documentAccess(listOf("owner_uOwner", "m_uJow")))
        val shared = DocItem(id = "d", tripId = "t1", title = "FlixBus", legId = "leg1",
            travellerIds = listOf("owner_uOwner", "m_uJow"))
        assertTrue(t.canSeeDocument(shared, "uJow"))
        assertTrue(t.canSeeDocument(shared, "uOwner"))
        assertFalse(t.canSeeDocument(shared, "uSam"))
        assertEquals(listOf("owner_uOwner", "m_uJow"), shared.assignedTravellerIds)
    }

    @Test fun olderSingleTravellerPassesStillWork() {
        val old = DocItem(id = "d", tripId = "t1", title = "Pass", travellerId = "m_uJow")
        assertEquals(listOf("m_uJow"), old.assignedTravellerIds)
        assertTrue(trip.canSeeDocument(old, "uJow"))
        assertFalse(trip.canSeeDocument(old, "uSam"))
    }

    @Test fun travellerLookup() {
        assertEquals("m_uJow", trip.travellerIdFor("uJow"))
        assertEquals("owner_uOwner", trip.travellerIdFor("uOwner"))
        assertEquals("", trip.travellerIdFor("stranger"))
    }
}
