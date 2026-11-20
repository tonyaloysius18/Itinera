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
        val tripWithIds = trip.copy(memberIds = listOf("uOwner", "uJow", "uSam"))
        assertEquals(listOf("uOwner", "uJow") to "uJow", tripWithIds.documentAccess("m_uJow"))
        assertEquals(listOf("uOwner") to "uOwner", tripWithIds.documentAccess("owner_uOwner"))
        assertEquals(listOf("uOwner", "uJow", "uSam") to "", tripWithIds.documentAccess(""))
        // A traveller with no account: only the owner gets it.
        assertEquals(listOf("uOwner") to "", tripWithIds.documentAccess("someone_offline"))
    }

    @Test fun travellerLookup() {
        assertEquals("m_uJow", trip.travellerIdFor("uJow"))
        assertEquals("owner_uOwner", trip.travellerIdFor("uOwner"))
        assertEquals("", trip.travellerIdFor("stranger"))
    }
}
