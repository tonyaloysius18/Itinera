package com.itinera.app.model

/** A traveller's net position: positive = they are owed money, negative = they owe. */
data class TravellerBalance(
    val travellerId: String,
    val net: Double,
)

/** A suggested payment to settle up. */
data class Settlement(
    val fromTravellerId: String,
    val toTravellerId: String,
    val amount: Double,
)

/**
 * Net balance per traveller = (what they paid) - (sum of their shares).
 * Only travellers who appear in the trip are returned.
 */
fun computeBalances(expenses: List<Expense>, travellerIds: List<String>): List<TravellerBalance> {
    return travellerIds.map { id ->
        val paid = expenses.filter { it.paidByTravellerId == id }.sumOf { it.amount }
        val owed = expenses.sumOf { exp ->
            exp.shares.firstOrNull { it.travellerId == id }?.amount ?: 0.0
        }
        TravellerBalance(id, paid - owed)
    }
}

/**
 * Greedy minimal-transactions settle-up: repeatedly match the biggest debtor to
 * the biggest creditor. Produces the fewest payments to zero everyone out.
 */
fun computeSettlements(balances: List<TravellerBalance>): List<Settlement> {
    val epsilon = 0.01
    data class Bal(val id: String, var amt: Double)

    val creditors = balances.filter { it.net > epsilon }
        .map { Bal(it.travellerId, it.net) }
        .sortedByDescending { it.amt }.toMutableList()
    val debtors = balances.filter { it.net < -epsilon }
        .map { Bal(it.travellerId, -it.net) }
        .sortedByDescending { it.amt }.toMutableList()

    val result = mutableListOf<Settlement>()
    var i = 0
    var j = 0
    while (i < debtors.size && j < creditors.size) {
        val debtor = debtors[i]
        val creditor = creditors[j]
        val pay = minOf(debtor.amt, creditor.amt)
        if (pay > epsilon) {
            result.add(Settlement(debtor.id, creditor.id, pay))
        }
        debtor.amt -= pay
        creditor.amt -= pay
        if (debtor.amt <= epsilon) i++
        if (creditor.amt <= epsilon) j++
    }
    return result
}

/** A directed debt from one traveller to another. */
data class PairDebt(
    val fromTravellerId: String,
    val toTravellerId: String,
    val amount: Double,
)

/**
 * Pairwise net debts: for each expense, every non-payer owes the payer their share.
 * Opposing debts between the same two people are netted (if A owes B 50 and B owes A 20,
 * the result is A owes B 30). This is the "who owes whom" detail, before minimisation.
 */
fun computePairwiseDebts(expenses: List<Expense>): List<PairDebt> {
    val raw = HashMap<Pair<String, String>, Double>()   // (debtor, creditor) -> amount
    for (e in expenses) {
        for (share in e.shares) {
            if (share.travellerId == e.paidByTravellerId) continue
            val key = share.travellerId to e.paidByTravellerId
            raw[key] = (raw[key] ?: 0.0) + share.amount
        }
    }

    val result = mutableListOf<PairDebt>()
    val handled = HashSet<Pair<String, String>>()
    for ((key, amount) in raw) {
        val (a, b) = key
        if (key in handled || (b to a) in handled) continue
        handled.add(key); handled.add(b to a)
        val reverse = raw[b to a] ?: 0.0
        val net = amount - reverse
        when {
            net > 0.01 -> result.add(PairDebt(a, b, net))
            net < -0.01 -> result.add(PairDebt(b, a, -net))
        }
    }
    return result
}