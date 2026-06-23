package com.itinera.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Expense
import com.itinera.app.model.ExpenseShare
import com.itinera.app.model.Trip
import com.itinera.app.ui.components.TopBar
import kotlin.math.abs
import kotlin.math.round
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private fun parseAmount(text: String): Double =
    text.replace(',', '.').toDoubleOrNull() ?: 0.0

/** Equal split that sums exactly to the total (remainder cents go to the first people). */
private fun equalShares(total: Double, ids: List<String>): List<ExpenseShare> {
    if (ids.isEmpty()) return emptyList()
    val totalCents = round(total * 100).toLong()
    val base = totalCents / ids.size
    val rem = (totalCents % ids.size).toInt()
    return ids.mapIndexed { i, id ->
        val cents = base + if (i < rem) 1 else 0
        ExpenseShare(id, cents / 100.0)
    }
}

@OptIn(ExperimentalTime::class)
private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    trip: Trip,
    existing: Expense?,
    onBack: () -> Unit,
    onSave: (Expense) -> Unit,
) {
    val s = LocalStrings.current
    val travellers = trip.travellers

    var description by remember { mutableStateOf(existing?.description ?: "") }
    var amountText by remember { mutableStateOf(existing?.amount?.let { if (it == 0.0) "" else twoDecimalsPlain(it) } ?: "") }
    var paidById by remember {
        mutableStateOf(existing?.paidByTravellerId ?: travellers.firstOrNull { it.isOwner }?.id ?: travellers.firstOrNull()?.id ?: "")
    }
    var paidMenuOpen by remember { mutableStateOf(false) }

    // involved travellers: default all (or those in the existing expense)
    val involved = remember {
        mutableStateMapOf<String, Boolean>().apply {
            travellers.forEach { t ->
                put(t.id, existing?.shares?.any { it.travellerId == t.id } ?: true)
            }
        }
    }

    var customMode by remember { mutableStateOf(existing != null) }   // editing starts in custom to preserve amounts
    val customText = remember {
        mutableStateMapOf<String, String>().apply {
            travellers.forEach { t ->
                val sh = existing?.shares?.firstOrNull { it.travellerId == t.id }
                put(t.id, sh?.amount?.let { twoDecimalsPlain(it) } ?: "")
            }
        }
    }

    val amount = parseAmount(amountText)
    val involvedIds = travellers.map { it.id }.filter { involved[it] == true }

    val customSum = involvedIds.sumOf { parseAmount(customText[it] ?: "") }
    val customMatches = abs(customSum - amount) < 0.01

    val canSave = description.isNotBlank() &&
            amount > 0.0 &&
            paidById.isNotBlank() &&
            involvedIds.isNotEmpty() &&
            (!customMode || customMatches)

    fun buildShares(): List<ExpenseShare> =
        if (customMode) {
            involvedIds.map { ExpenseShare(it, parseAmount(customText[it] ?: "")) }
        } else {
            equalShares(amount, involvedIds)
        }

    fun save() {
        val expense = Expense(
            id = existing?.id ?: "exp_${Random.nextLong()}",
            tripId = trip.id,
            description = description.trim(),
            amount = amount,
            paidByTravellerId = paidById,
            shares = buildShares(),
            createdAt = existing?.createdAt ?: nowMillis(),
        )
        onSave(expense)
    }

    fun nameOf(id: String) = travellers.firstOrNull { it.id == id }?.firstName ?: "?"

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }

    // local toast pill (positioned just above the nav pill area, only on this screen)
    var pillText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(customMode, customMatches, amount) {
        if (customMode && customMatches && amount > 0.0) {
            pillText = s.splitMatches
            kotlinx.coroutines.delay(2000)
            pillText = null
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().imePadding()) {
            TopBar(
                title = if (existing == null) s.addExpense else s.editExpense,
                onBack = onBack,
            )

            // ── Fixed top section (does NOT scroll) ──────────────────────
            Column(Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.toTitleCase() },
                    label = { Text(s.description) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(s.amount) },
                    prefix = { Text(currencySymbolOrCode(trip.currencyCode)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(12.dp))

                // Paid by
                ExposedDropdownMenuBox(expanded = paidMenuOpen, onExpandedChange = { paidMenuOpen = it }) {
                    OutlinedTextField(
                        value = nameOf(paidById),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(s.paidByLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paidMenuOpen) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(expanded = paidMenuOpen, onDismissRequest = { paidMenuOpen = false }, shape = RoundedCornerShape(12.dp)) {
                        travellers.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.firstName) },
                                onClick = { paidById = t.id; paidMenuOpen = false },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))

                // Split mode
                Text(s.splitBetween, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !customMode, onClick = { customMode = false }, label = { Text(s.splitEqually) })
                    FilterChip(selected = customMode, onClick = { customMode = true }, label = { Text(s.splitCustom) })
                }
            }

            // ── Scrollable travellers list (ONLY this scrolls) ───────────
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(8.dp))
                travellers.forEach { t ->
                    val checked = involved[t.id] == true
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { involved[t.id] = !checked }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RoundCheckbox(checked = checked)
                        Spacer(Modifier.width(12.dp))
                        Text(t.firstName, Modifier.weight(1f))
                        if (checked) {
                            if (customMode) {
                                OutlinedTextField(
                                    value = customText[t.id] ?: "",
                                    onValueChange = { customText[t.id] = it },
                                    singleLine = true,
                                    placeholder = { Text("0", style = MaterialTheme.typography.bodySmall) },
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.width(90.dp).height(30.dp),
                                    shape = RoundedCornerShape(10.dp),
                                )
                            } else {
                                val per = if (involvedIds.isNotEmpty()) amount / involvedIds.size else 0.0
                                Text(formatMoney(per, trip.currencyCode), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ── Fixed bottom section (hint + Save, does NOT scroll) ───────
            Column(Modifier.padding(horizontal = 16.dp)) {
                // custom sum hint — only show remaining/over while it doesn't match
                if (customMode && amount > 0.0 && !customMatches) {
                    val diff = amount - customSum
                    val msg = if (diff > 0) "${s.remaining}: ${formatMoney(diff, trip.currencyCode)}"
                    else "${s.over}: ${formatMoney(-diff, trip.currencyCode)}"
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE8590C),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                Button(
                    onClick = { if (canSave) save() },
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 100.dp).padding(top = 8.dp, bottom = 32.dp).height(50.dp),
                ) { Text(s.save) }

                // clears the iPhone home indicator (Surface now bleeds to the bottom edge)
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }

        // local toast pill — sits just above the bottom area
        androidx.compose.animation.AnimatedVisibility(
            visible = pillText != null,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.DarkGray.copy(alpha = 0.6f),
                shadowElevation = 6.dp,
            ) {
                Text(
                    pillText ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        }
    }
}

private fun twoDecimalsPlain(v: Double): String {
    val cents = round(v * 100).toLong()
    val whole = cents / 100
    val frac = (kotlin.math.abs(cents % 100)).toInt()
    val fracStr = if (frac < 10) "0$frac" else "$frac"
    return "$whole.$fracStr"
}

/** Symbol prefix for the amount field, falling back to the code + space. */
private fun currencySymbolOrCode(code: String): String {
    val sym = currencySymbol(code)
    return if (sym.isNotEmpty()) "$sym " else "$code "
}

@Composable
private fun RoundCheckbox(checked: Boolean) {
    val border = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    Box(
        Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (checked) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(2.dp, border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}