package com.itinera.app.data

import com.itinera.app.model.Invite
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore

/**
 * Manages trip invites in the top-level invites/{inviteId} collection, and the
 * client-side "join by code" flow.
 *
 * Join is two-phase to work around the read-before-join chicken-and-egg:
 *  1) a blind write adds the joiner to the trip's members/memberIds (no read needed),
 *     making them a member;
 *  2) now a member, they fan out their uid to the trip's documents/expenses.
 */
class InviteService {

    private val db = Firebase.firestore

    private fun invitesRef() = db.collection("invites")
    private fun tripsRef() = db.collection("trips")

    /** Create (or overwrite) an invite. */
    suspend fun createInvite(invite: Invite) {
        invitesRef().document(invite.id).set(invite)
    }

    /** Look up an active invite by its human code. Returns null if none/expired. */
    suspend fun findByCode(code: String): Invite? {
        val snap = invitesRef()
            .where { "code" equalTo code.trim().uppercase() }
            .where { "status" equalTo "active" }
            .get()
        return snap.documents.firstOrNull()?.data(Invite.serializer())
    }

    /** Revoke an invite so the code stops working. */
    suspend fun revokeInvite(inviteId: String) {
        invitesRef().document(inviteId).update("status" to "revoked")
    }

    /**
     * Phase 2: now a member, fan out the joiner's uid to every document and expense
     * under the trip so the collection-group listeners include them.
     */
    suspend fun fanOutToSubcollections(tripId: String, uid: String) {
        val docs = tripsRef().document(tripId).collection("documents").get()
        docs.documents.forEach { d ->
            tripsRef().document(tripId).collection("documents").document(d.id)
                .update("memberIds" to FieldValue.arrayUnion(uid))
        }
        val exps = tripsRef().document(tripId).collection("expenses").get()
        exps.documents.forEach { e ->
            tripsRef().document(tripId).collection("expenses").document(e.id)
                .update("memberIds" to FieldValue.arrayUnion(uid))
        }
        val acts = tripsRef().document(tripId).collection("activities").get()
        acts.documents.forEach { a ->
            tripsRef().document(tripId).collection("activities").document(a.id)
                .update("memberIds" to FieldValue.arrayUnion(uid))
        }
    }


    /**
     * Phase 1 of leaving: remove the user's uid from every document/expense/activity
     * under the trip. Must run BEFORE removing them from the trip (while still a member,
     * so the nested writes are still permitted).
     */
    suspend fun removeSelfFromSubcollections(tripId: String, uid: String) {
        val docs = tripsRef().document(tripId).collection("documents").get()
        docs.documents.forEach { d ->
            tripsRef().document(tripId).collection("documents").document(d.id)
                .update("memberIds" to FieldValue.arrayRemove(uid))
        }
        val exps = tripsRef().document(tripId).collection("expenses").get()
        exps.documents.forEach { e ->
            tripsRef().document(tripId).collection("expenses").document(e.id)
                .update("memberIds" to FieldValue.arrayRemove(uid))
        }
        val acts = tripsRef().document(tripId).collection("activities").get()
        acts.documents.forEach { a ->
            tripsRef().document(tripId).collection("activities").document(a.id)
                .update("memberIds" to FieldValue.arrayRemove(uid))
        }
    }

    /**
     * Phase 2 of leaving: remove the user from the trip's members map, memberIds list,
     * and memberInfo map. After this they're no longer a member.
     */
    suspend fun leaveTripRemoveSelf(tripId: String, uid: String) {
        tripsRef().document(tripId).update(
            "members.$uid" to FieldValue.delete,
            "memberInfo.$uid" to FieldValue.delete,
            "memberIds" to FieldValue.arrayRemove(uid),
        )
    }

    suspend fun joinTripAddSelf(tripId: String, uid: String, name: String, email: String) {
        tripsRef().document(tripId).update(
            "members.$uid" to "viewer",
            "memberIds" to FieldValue.arrayUnion(uid),
            "memberInfo.$uid" to mapOf("name" to name, "email" to email),
        )
    }
}