package com.itinera.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Expense
import com.itinera.app.model.Trip
import com.itinera.app.model.computeBalances
import com.itinera.app.model.computePairwiseDebts
import com.itinera.app.model.computeSettlements
import com.itinera.app.model.isOwnedBy
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.EmptyState
import com.itinera.app.ui.components.TopBar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripExpensesScreen(
    trip: Trip,
    expenses: List<Expense>,
    payments: List<com.itinera.app.model.Payment> = emptyList(),   // ⬅ ADD
    onBack: () -> Unit,
    isLoading: Boolean = false,
    onAddExpense: () -> Unit,
    onEditExpense: (String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onSetCurrency: (String) -> Unit,
    canEdit: Boolean = true,
    currentUid: String = "",
    onSetSettled: (Boolean) -> Unit = {},
    onMarkPaid: (String, String, Double) -> Unit = { _, _, _ -> },  // ⬅ ADD (from, to, amount)
    onDeletePayment: (String) -> Unit = {},                          // ⬅ ADD
) {
    val s = LocalStrings.current
    var pendingDelete by remember { mutableStateOf<Expense?>(null) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var expandedId by remember { mutableStateOf<String?>(null) }

    fun nameOf(id: String): String =
        trip.travellers.firstOrNull { it.id == id }?.firstName ?: "?"

    val total = expenses.sumOf { it.amount }
    val travellerIds = trip.travellers.map { it.id }
    val balances = computeBalances(expenses, payments, travellerIds)
    val settlements = computeSettlements(balances)
    val pairwise = computePairwiseDebts(expenses)

    // ---- settle-up state ----
    val isOwner = trip.isOwnedBy(currentUid)
    val isSettled = trip.settledAt > 0L
    // the traveller linked to the current member (via reconciliation: traveller.userId == uid)
    val myTravellerId = trip.travellers.firstOrNull { it.userId == currentUid }?.id
    val iOwe = if (myTravellerId == null) 0.0
    else settlements.filter { it.fromTravellerId == myTravellerId }.sumOf { it.amount }
    val iAmOwed = if (myTravellerId == null) 0.0
    else settlements.filter { it.toTravellerId == myTravellerId }.sumOf { it.amount }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                title = trip.title,
                onBack = onBack,
            )

            if (expenses.isEmpty()) {
                // In empty state, we still want to show the currency card at the top
                Column(Modifier.fillMaxSize()) {
                    TotalCard(
                        total = total,
                        currencyCode = trip.currencyCode,
                        onShowCurrencyPicker = { showCurrencyPicker = true },
                        modifier = Modifier.padding(16.dp)
                    )
                    EmptyState(
                        icon = Icons.Filled.ReceiptLong,
                        title = s.noExpenses,
                        subtitle = s.noExpensesSubtitle,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // ---- Settled banner (non-owners, when the owner has settled) ----
                    if (isSettled && !isOwner) {
                        item {
                            val settledByName = trip.memberInfo[trip.settledBy]?.name?.takeIf { it.isNotBlank() }
                                ?: trip.travellers.firstOrNull { it.userId == trip.settledBy }?.firstName
                                ?: s.theOwner
                            SettledBanner(
                                settledByName = settledByName,
                                iOwe = iOwe,
                                iAmOwed = iAmOwed,
                                currencyCode = trip.currencyCode,
                            )
                        }
                    }

                    // ---- Total ----
                    item {
                        TotalCard(
                            total = total,
                            currencyCode = trip.currencyCode,
                            onShowCurrencyPicker = { showCurrencyPicker = true }
                        )
                    }

                    // ---- Settle up (minimised) ----
                    item {
                        Surface(shape = CardShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Text(s.settleUp, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(s.settleUpHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                                Spacer(Modifier.height(10.dp))
                                if (settlements.isEmpty()) {
                                    Text(s.allSettled, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                } else {
                                    settlements.forEach { st ->
                                        val iAmDebtor = trip.travellers
                                            .firstOrNull { it.id == st.fromTravellerId }?.userId == currentUid
                                        Row(
                                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                "${nameOf(st.fromTravellerId)} → ${nameOf(st.toTravellerId)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f),
                                            )
                                            Text(
                                                formatMoney(st.amount, trip.currencyCode),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                            if (iAmDebtor) {
                                                Spacer(Modifier.width(8.dp))
                                                TextButton(
                                                    onClick = { onMarkPaid(st.fromTravellerId, st.toTravellerId, st.amount) },
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                ) { Text(s.markPaid) }
                                            }
                                        }
                                    }
                                }

                                // Owner-only settle / un-settle control
                                if (isOwner) {
                                    Spacer(Modifier.height(14.dp))
                                    if (isSettled) {
                                        OutlinedButton(
                                            onClick = { onSetSettled(false) },
                                            modifier = Modifier.fillMaxWidth(),
                                        ) { Text(s.unsettleTrip) }
                                    } else {
                                        Button(
                                            onClick = { onSetSettled(true) },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = settlements.isNotEmpty(),
                                        ) { Text(s.settleUpTrip) }
                                    }
                                }
                            }
                        }
                    }
                    // ---- Payments history ----
                    if (payments.isNotEmpty()) {
                        item {
                            Surface(shape = CardShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text(s.payments, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        s.recordedRepayments,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    payments.sortedByDescending { it.createdAt }.forEach { p ->
                                        val iMadeIt = trip.travellers
                                            .firstOrNull { it.id == p.fromTravellerId }?.userId == currentUid
                                        Row(
                                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                "${nameOf(p.fromTravellerId)} → ${nameOf(p.toTravellerId)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f),
                                            )
                                            Text(
                                                formatMoney(p.amount, trip.currencyCode),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                            )
                                            // only the person who recorded it can undo it
                                            if (iMadeIt) {
                                                IconButton(onClick = { onDeletePayment(p.id) }) {
                                                    Icon(
                                                        Icons.Outlined.Delete,
                                                        contentDescription = s.undoPayment,
                                                        modifier = Modifier.size(18.dp),
                                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ---- Who owes whom (pairwise) ----
                    if (pairwise.isNotEmpty()) {
                        item {
                            Surface(shape = CardShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text(s.whoOwesWhom, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(s.whoOwesWhomHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                                    Spacer(Modifier.height(10.dp))
                                    pairwise.forEach { pd ->
                                        DebtRow(nameOf(pd.fromTravellerId), nameOf(pd.toTravellerId), formatMoney(pd.amount, trip.currencyCode))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            s.expensesLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }

                    // ---- Expense rows (tap to expand split detail) ----
                    items(expenses.sortedByDescending { it.createdAt }, key = { it.id }) { exp ->
                        val expanded = expandedId == exp.id
                        Surface(
                            modifier = Modifier.fillMaxWidth().animateContentSize(),
                            shape = CardShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        ) {
                            Column {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { expandedId = if (expanded) null else exp.id },
                                            onLongClick = { if (canEdit) pendingDelete = exp },
                                        )
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(exp.description, style = MaterialTheme.typography.bodyLarge)
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "${s.paidBy} ${nameOf(exp.paidByTravellerId)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        )
                                    }
                                    Text(
                                        formatMoney(exp.amount, trip.currencyCode),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }

                                if (expanded) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                    Column(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 12.dp)) {
                                        Text(s.splitLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        Spacer(Modifier.height(6.dp))
                                        // each non-payer owes the payer their share
                                        exp.shares
                                            .filter { it.travellerId != exp.paidByTravellerId && it.amount > 0.0 }
                                            .forEach { share ->
                                                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                                    Text(
                                                        "${nameOf(share.travellerId)} ${s.owes} ${nameOf(exp.paidByTravellerId)}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.weight(1f),
                                                    )
                                                    Text(
                                                        formatMoney(share.amount, trip.currencyCode),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium,
                                                    )
                                                }
                                            }
                                        Spacer(Modifier.height(6.dp))
                                        if (canEdit) {
                                            TextButton(onClick = { onEditExpense(exp.id) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                                                Icon(Icons.Filled.Edit, null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(s.edit)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }

        if (canEdit) {

            FloatingActionButton(
                onClick = onAddExpense,
                modifier = Modifier
                    .align(Alignment.BottomEnd).offset(x = (-25).dp, y = 60.dp)
                    .padding(end = 20.dp, bottom = 220.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)            //text = { Text(s.addExpense) },
            }
        }
    }


    if (showCurrencyPicker) {
        AlertDialog(
            onDismissRequest = { showCurrencyPicker = false },
            title = { Text(s.tripCurrency) },
            shape = RoundedCornerShape(16.dp),
            text = {
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(COMMON_CURRENCIES) { code ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSetCurrency(code); showCurrencyPicker = false }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(code, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            val sym = currencySymbol(code)
                            if (sym.isNotEmpty()) Text(sym, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            if (code == trip.currencyCode) {
                                Spacer(Modifier.width(8.dp))
                                Text("✓", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showCurrencyPicker = false }) { Text(s.cancel) } },
        )
    }

    pendingDelete?.let { exp ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(s.deleteExpenseQ) },
            text = { Text(exp.description) },
            confirmButton = {
                TextButton(onClick = { onDeleteExpense(exp.id); pendingDelete = null }) {
                    Text(s.delete, color = Color(0xFFE03131))
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(s.cancel) } },
        )
    }
}

@Composable
private fun SettledBanner(
    settledByName: String,
    iOwe: Double,
    iAmOwed: Double,
    currencyCode: String,
) {
    val s = LocalStrings.current
    val owes = iOwe > 0.01
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = if (owes) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("💰", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "$settledByName ${s.markedAsSettled}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    when {
                        owes -> s.youOweBanner.replace("%s", formatMoney(iOwe, currencyCode))
                        iAmOwed > 0.01 -> s.youAreOwedBanner.replace("%s", formatMoney(iAmOwed, currencyCode))
                        else -> s.allSquareBanner
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                )
            }
        }
    }
}

@Composable
private fun TotalCard(
    total: Double,
    currencyCode: String,
    onShowCurrencyPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(s.totalSpent, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(4.dp))
                Text(formatMoney(total, currencyCode), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.clickable { onShowCurrencyPicker() },
            ) {
                Row(
                    Modifier.padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(currencyCode, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                    Icon(Icons.Filled.ArrowDropDown, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun DebtRow(from: String, to: String, amount: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$from → $to", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
    }
}