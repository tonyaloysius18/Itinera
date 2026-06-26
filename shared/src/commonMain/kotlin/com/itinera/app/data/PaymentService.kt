package com.itinera.app.data

import com.itinera.app.model.Payment
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reads and writes repayments under trips/{tripId}/payments/{payId}.
 * A Payment records that one traveller paid another back to settle a debt.
 * Each carries memberIds (denormalized from its parent trip) so a single
 * collection-group query streams all payments across a user's trips.
 *
 * Mirrors ExpenseService — same sharing model, same collection-group flow.
 */
class PaymentService {
    private val db = Firebase.firestore

    private fun paymentsRef(tripId: String) =
        db.collection("trips").document(tripId).collection("payments")

    /** Save (create or overwrite) a single payment under its trip. */
    suspend fun savePayment(payment: Payment) {
        paymentsRef(payment.tripId).document(payment.id).set(payment)
    }

    /** Remove a payment under its trip. */
    suspend fun deletePayment(tripId: String, paymentId: String) {
        paymentsRef(tripId).document(paymentId).delete()
    }

    /** Live stream of all payments across trips this user is a member of. */
    fun paymentsFlow(uid: String): Flow<List<Payment>> =
        db.collectionGroup("payments")
            .where { "memberIds" contains uid }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data(Payment.serializer()) }
            }
}