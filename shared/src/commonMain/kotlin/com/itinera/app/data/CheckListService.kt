package com.itinera.app.data

import com.itinera.app.model.ChecklistItem
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Personal (per-user) checklist storage under users/{uid}/checklists/{itemId}.
 * Each user has their own packing/prep list per trip — not shared with other
 * members. Simple collection (no memberIds, no collection-group, no sharing).
 */
class ChecklistService {

    private val db = Firebase.firestore

    private fun checklistRef(uid: String) =
        db.collection("users").document(uid).collection("checklists")

    /** Save (create or overwrite) one checklist item for this user. */
    suspend fun saveItem(uid: String, item: ChecklistItem) {
        checklistRef(uid).document(item.id).set(item)
    }

    /** Delete one checklist item for this user. */
    suspend fun deleteItem(uid: String, itemId: String) {
        checklistRef(uid).document(itemId).delete()
    }

    /** Live stream of all this user's checklist items (across all their trips). */
    fun checklistFlow(uid: String): Flow<List<ChecklistItem>> =
        checklistRef(uid).snapshots.map { snapshot ->
            snapshot.documents.map { it.data(ChecklistItem.serializer()) }
        }
}