package com.itinera.app.data

import com.itinera.app.model.Expense
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

/**
 * Reads and writes the user's expenses in Firestore under
 * users/{uid}/expenses/{expenseId}. Each carries a tripId field so the
 * repository can filter them per trip in memory.
 *
 * Mirrors DocService/TripService: explicit serializer on read, .set() on write.
 */
class ExpenseService {

    private val db = Firebase.firestore

    private fun expensesRef(uid: String) =
        db.collection("users").document(uid).collection("expenses")

    suspend fun loadExpenses(uid: String): List<Expense> {
        val snapshot = expensesRef(uid).get()
        return snapshot.documents.map { doc ->
            doc.data(Expense.serializer())
        }
    }

    suspend fun saveExpense(uid: String, expense: Expense) {
        expensesRef(uid).document(expense.id).set(expense)
    }

    suspend fun deleteExpense(uid: String, expenseId: String) {
        expensesRef(uid).document(expenseId).delete()
    }
}