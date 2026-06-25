package com.itinera.app.data

import com.itinera.app.model.Expense
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reads and writes expenses under trips/{tripId}/expenses/{expId}.
 * Each Expense carries memberIds (denormalized from its parent trip) so a single
 * collection-group query can stream all expenses across the trips a user belongs to.
 *
 * (Previously expenses lived under users/{uid}/expenses. Moved under trips for sharing.)
 */
class ExpenseService {

    private val db = Firebase.firestore

    private fun expensesRef(tripId: String) =
        db.collection("trips").document(tripId).collection("expenses")

    /** Save (create or overwrite) a single expense under its trip. */
    suspend fun saveExpense(expense: Expense) {
        expensesRef(expense.tripId).document(expense.id).set(expense)
    }

    /** Remove an expense under its trip. */
    suspend fun deleteExpense(tripId: String, expenseId: String) {
        expensesRef(tripId).document(expenseId).delete()
    }

    /** Live stream of all expenses across trips this user is a member of. */
    fun expensesFlow(uid: String): Flow<List<Expense>> =
        db.collectionGroup("expenses")
            .where { "memberIds" contains uid }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data(Expense.serializer()) }
            }

    // ── one-off migration helper: reads the OLD users/{uid}/expenses location ──
    suspend fun loadLegacyExpenses(uid: String): List<Expense> {
        val snapshot = db.collection("users").document(uid).collection("expenses").get()
        return snapshot.documents.map { it.data(Expense.serializer()) }
    }
}