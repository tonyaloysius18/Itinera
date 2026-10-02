package com.itinera.app.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommunityRulesTest {
    @Test fun ordinaryTextIsAcceptable() {
        assertTrue(CommunityRules.isAcceptable("Lisbon Weekend"))
        assertTrue(CommunityRules.isAcceptable("Two relaxed days by the sea"))
        assertTrue(CommunityRules.isAcceptable(""))
    }

    @Test fun blockedWordsAreRejectedEvenWithLookAlikes() {
        assertFalse(CommunityRules.isAcceptable("Free casino trip"))
        assertFalse(CommunityRules.isAcceptable("CASINO night"))
        assertFalse(CommunityRules.isAcceptable("Free p0rn trip"))
    }

    @Test fun linksAndContactDetailsAreRejected() {
        assertFalse(CommunityRules.isAcceptable("visit www.example.com"))
        assertFalse(CommunityRules.isAcceptable("see https://example.com"))
        assertFalse(CommunityRules.isAcceptable("call 555-123-4567"))
        assertFalse(CommunityRules.isAcceptable("dm me @someone"))
    }

    @Test fun placeNamesContainingAWordAreFine() {
        assertTrue(CommunityRules.isAcceptable("Sussex and Essex weekend"))
        assertTrue(CommunityRules.isAcceptable("Scunthorpe"))
    }
}
