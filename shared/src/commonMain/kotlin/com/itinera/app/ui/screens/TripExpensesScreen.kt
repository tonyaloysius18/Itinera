package com.itinera.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Expense
import com.itinera.app.model.ExpenseCategory
import com.itinera.app.model.PairDebt
import com.itinera.app.model.Payment
import com.itinera.app.model.Settlement
import com.itinera.app.model.Traveller
import com.itinera.app.model.Trip
import com.itinera.app.model.computeBalances
import com.itinera.app.model.computePairwiseDebts
import com.itinera.app.model.computeSettlements
import com.itinera.app.model.effectiveCategory
import com.itinera.app.model.isOwnedBy
import com.itinera.app.model.label
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.TopBar
import com.itinera.app.ui.components.color
import com.itinera.app.ui.components.icon
import com.itinera.app.ui.components.label
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt
private enum class ExpenseLens { Timeline, Categories, Balances }

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun TripExpensesScreen(
    trip: Trip,
    expenses: List<Expense>,
    payments: List<Payment> = emptyList(),
    onBack: () -> Unit,
    isLoading: Boolean = false,
    onAddExpense: () -> Unit,
    onEditExpense: (String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onSetCurrency: (String) -> Unit,
    canEdit: Boolean = true,
    currentUid: String = "",
    onSetSettled: (Boolean) -> Unit = {},
    onMarkPaid: (String, String, Double) -> Unit = { _, _, _ -> },
    onDeletePayment: (String) -> Unit = {},
    bottomBarInset: Dp = 0.dp,   // ⬅ CHANGED — replaces the FAB offset/padding magic numbers
) {
    val s = LocalStrings.current

    var lensOrdinal by rememberSaveable { mutableStateOf(ExpenseLens.Timeline.ordinal) }
    var selectedCategoryName by rememberSaveable { mutableStateOf<String?>(null) }
    var showCurrencyPicker by rememberSaveable { mutableStateOf(false) }
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    var openSwipeId by rememberSaveable { mutableStateOf<String?>(null) }

    val lens = ExpenseLens.entries[lensOrdinal]
    val selectedCategory = selectedCategoryName?.let { name ->
        ExpenseCategory.entries.firstOrNull { it.name == name }
    }

    val nameOf: (String) -> String = { id ->
        trip.travellers.firstOrNull { it.id == id }?.firstName ?: "?"
    }

    val travellerIds = remember(trip.travellers) { trip.travellers.map { it.id } }
    val total = remember(expenses) { expenses.sumOf { it.amount } }
    val balances = remember(expenses, payments, travellerIds) {
        computeBalances(expenses, payments, travellerIds)
    }
    val settlements = remember(balances) { computeSettlements(balances) }
    val pairwise = remember(expenses) { computePairwiseDebts(expenses) }
    val spendingByTraveller = remember(expenses, trip.travellers) {
        trip.travellers
            .map { t -> t to expenses.filter { it.paidByTravellerId == t.id }.sumOf { it.amount } }
            .sortedByDescending { it.second }
    }

    val byCategory = remember(expenses) {
        ExpenseCategory.entries
            .map { c -> c to expenses.filter { it.effectiveCategory == c }.sumOf { it.amount } }
            .filter { it.second > 0.0 }
            .sortedByDescending { it.second }
    }

    val visible = remember(expenses, selectedCategory) {
        if (selectedCategory == null) expenses else expenses.filter { it.effectiveCategory == selectedCategory }
    }
    val byDay = remember(visible) {
        visible.sortedByDescending { it.createdAt }.groupBy { dayOf(it.createdAt) }
    }
    val perDay = remember(expenses, total) {
        val days = expenses.map { dayOf(it.createdAt) }.distinct().size
        if (days > 0) total / days else 0.0
    }

    // ---- settle-up state ----
    val isOwner = trip.isOwnedBy(currentUid)
    val isSettled = trip.settledAt > 0L
    val myTravellerId = trip.travellers.firstOrNull { it.userId == currentUid }?.id
    val iOwe = if (myTravellerId == null) 0.0
    else settlements.filter { it.fromTravellerId == myTravellerId }.sumOf { it.amount }
    val iAmOwed = if (myTravellerId == null) 0.0
    else settlements.filter { it.toTravellerId == myTravellerId }.sumOf { it.amount }
    val myBalance = if (myTravellerId == null) null else iAmOwed - iOwe

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(title = trip.title, onBack = onBack)

            when {
                isLoading && expenses.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                    }
                }

                expenses.isEmpty() -> ExpensesEmptyState(
                    currencyCode = trip.currencyCode,
                    onShowCurrencyPicker = { showCurrencyPicker = true },
                )

                else -> {

                    Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
                        if (isSettled && !isOwner) {
                            val settledByName = trip.memberInfo[trip.settledBy]?.name?.takeIf { it.isNotBlank() }
                                ?: trip.travellers.firstOrNull { it.userId == trip.settledBy }?.firstName
                                ?: s.theOwner
                            SettledBanner(
                                settledByName = settledByName,
                                iOwe = iOwe,
                                iAmOwed = iAmOwed,
                                currencyCode = trip.currencyCode,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                            Spacer(Modifier.height(10.dp))
                        }

                        SummaryHero(
                            total = total,
                            budget = trip.budget.takeIf { it > 0.0 },
                            perDay = perDay,
                            myBalance = myBalance,
                            currencyCode = trip.currencyCode,
                            onShowCurrencyPicker = { showCurrencyPicker = true },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(Modifier.height(10.dp))

                        CategoryMeter(
                            slices = byCategory,
                            total = total,
                            selected = selectedCategory,
                            onSelect = { cat ->
                                selectedCategoryName = cat?.name
                                if (cat != null) lensOrdinal = ExpenseLens.Timeline.ordinal
                            },
                            currencyCode = trip.currencyCode,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(Modifier.height(10.dp))

                        LensSelector(
                            selected = lens,
                            onSelect = { lensOrdinal = it.ordinal },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    // ⬅ CHANGED — no horizontal padding here, so sticky day headers span
                    // edge to edge and rows scroll cleanly underneath them.
                    contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp + bottomBarInset),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    when (lens) {
                        ExpenseLens.Timeline -> {
                            byDay.forEach { (day, dayExpenses) ->
                                stickyHeader(key = "day-$day") {
                                    DayHeader(
                                        label = day.headerLabel(),
                                        total = dayExpenses.sumOf { it.amount },
                                        currencyCode = trip.currencyCode,
                                    )
                                }
                                items(dayExpenses, key = { "exp-${it.id}" }) { exp ->
                                    ExpenseRow(
                                        expense = exp,
                                        currencyCode = trip.currencyCode,
                                        nameOf = nameOf,
                                        canEdit = canEdit,
                                        expanded = expandedId == exp.id,
                                        onToggleExpand = {
                                            expandedId = if (expandedId == exp.id) null else exp.id
                                        },
                                        isSwipeOpen = openSwipeId == exp.id,
                                        onSwipeOpenChange = { open -> openSwipeId = if (open) exp.id else null },
                                        onDelete = { onDeleteExpense(exp.id); openSwipeId = null },
                                        onEdit = { onEditExpense(exp.id) },
                                        modifier = Modifier.animateItem().padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        }

                        ExpenseLens.Categories -> {
                            if (byCategory.isEmpty()) {
                                item(key = "cat-empty") {
                                    Text(
                                        s.noCategoryData,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                            items(byCategory, key = { "cat-${it.first.name}" }) { (cat, amount) ->
                                CategoryBreakdownRow(
                                    category = cat,
                                    amount = amount,
                                    fraction = if (total > 0) (amount / total).toFloat() else 0f,
                                    currencyCode = trip.currencyCode,
                                    onClick = {
                                        selectedCategoryName = cat.name
                                        lensOrdinal = ExpenseLens.Timeline.ordinal
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }

                        ExpenseLens.Balances -> balancesLens(
                            trip = trip,
                            settlements = settlements,
                            pairwise = pairwise,
                            spendingByTraveller = spendingByTraveller,
                            payments = payments,
                            currentUid = currentUid,
                            isOwner = isOwner,
                            isSettled = isSettled,
                            nameOf = nameOf,
                            onMarkPaid = onMarkPaid,
                            onDeletePayment = onDeletePayment,
                            onSetSettled = onSetSettled,
                        )
                    }
                }
                }
            }
        }

        if (canEdit) {
            FloatingActionButton(
                onClick = onAddExpense,
                // ⬅ CHANGED — was .offset(x = -25.dp, y = 60.dp).padding(end = 20.dp, bottom = 220.dp)
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp + bottomBarInset)
                    .offset(x = -10.dp, y = -120.dp),

                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            ) {
                Icon(Icons.Filled.Add, contentDescription = s.addExpense)   // ⬅ CHANGED — was null
            }
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            current = trip.currencyCode,
            onPick = { onSetCurrency(it); showCurrencyPicker = false },
            onDismiss = { showCurrencyPicker = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryHero(
    total: Double,
    budget: Double?,
    perDay: Double,
    myBalance: Double?,
    currencyCode: String,
    onShowCurrencyPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        s.totalSpent,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            formatMoney(total, currencyCode),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (budget != null) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                s.ofBudget.replace("%s", formatMoney(budget, currencyCode)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 3.dp),
                            )
                        }
                    }
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
                        Icon(
                            Icons.Filled.ArrowDropDown, null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            if (budget != null) {
                Spacer(Modifier.height(12.dp))
                val fraction = (total / budget).toFloat().coerceIn(0f, 1f)
                val over = total > budget
                Box(
                    Modifier.fillMaxWidth().height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
                ) {
                    Box(
                        Modifier.fillMaxWidth(fraction).fillMaxHeight()
                            .background(
                                if (over) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            ),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // The number the viewer actually opened this screen for.
                Column(Modifier.weight(1f)) {
                    Text(
                        s.yourBalance,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                    Spacer(Modifier.height(2.dp))
                    val settledUp = myBalance == null || abs(myBalance) < 0.01
                    Text(
                        when {
                            settledUp -> s.allSquareShort
                            myBalance!! > 0 -> "${s.youAreOwedShort} ${formatMoney(myBalance, currencyCode)}"
                            else -> "${s.youOweShort} ${formatMoney(-myBalance, currencyCode)}"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            settledUp -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            myBalance!! > 0 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        },
                    )
                }

                Text(
                    s.perDayAverage.replace("%s", formatMoney(perDay, currencyCode)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category meter
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Single stacked bar + tappable legend. Tapping a slice or chip filters the
 * timeline; tapping the active one clears the filter.
 *
 * A stacked bar beats a donut here: it reads at a glance at phone width, needs
 * no labels inside it, and slice widths stay comparable across trips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryMeter(
    slices: List<Pair<ExpenseCategory, Double>>,
    total: Double,
    selected: ExpenseCategory?,
    onSelect: (ExpenseCategory?) -> Unit,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    if (slices.isEmpty() || total <= 0.0) return

    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            slices.forEach { (cat, amount) ->
                key(cat) {
                    // Floor at 2% so a tiny category stays tappable instead of
                    // collapsing to a hairline.
                    val target = (amount / total).toFloat().coerceAtLeast(0.02f)
                    val weight by animateFloatAsState(targetValue = target, animationSpec = tween(320))
                    val dimmed = selected != null && selected != cat
                    Box(
                        Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .background(cat.color.copy(alpha = if (dimmed) 0.25f else 1f))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { onSelect(if (selected == cat) null else cat) },
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            slices.forEach { (cat, amount) ->
                key(cat) {
                    val active = selected == cat
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (active) cat.color.copy(alpha = 0.14f) else Color.Transparent,
                        border = BorderStroke(
                            0.5.dp,
                            if (active) cat.color.copy(alpha = 0.55f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        ),
                        modifier = Modifier.clickable { onSelect(if (active) null else cat) },
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(cat.color))
                            Spacer(Modifier.width(6.dp))
                            Text(cat.label(), style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                formatMoney(amount, currencyCode),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(
    category: ExpenseCategory,
    amount: Double,
    fraction: Float,
    currencyCode: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryAvatar(category)
                Spacer(Modifier.width(12.dp))
                Text(category.label(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatMoney(amount, currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "${(fraction * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.fillMaxWidth().height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            ) {
                Box(
                    Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight()
                        .background(category.color),
                )
            }
        }
    }
}

@Composable
private fun CategoryAvatar(category: ExpenseCategory, size: Dp = 36.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(category.color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            category.icon,
            contentDescription = null,
            tint = category.color,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Lens selector
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LensSelector(
    selected: ExpenseLens,
    onSelect: (ExpenseLens) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val labels = mapOf(
        ExpenseLens.Timeline to s.lensTimeline,
        ExpenseLens.Categories to s.lensCategories,
        ExpenseLens.Balances to s.lensBalances,
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
    ) {
        Row(Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            ExpenseLens.entries.forEach { entry ->
                val active = entry == selected
                Surface(
                    modifier = Modifier.weight(1f).clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onSelect(entry) },
                    shape = RoundedCornerShape(9.dp),
                    color = if (active) MaterialTheme.colorScheme.surface else Color.Transparent,
                    border = if (active) {
                        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    } else null,
                ) {
                    Box(Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            labels[entry].orEmpty(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (active) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Timeline
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DayHeader(label: String, total: Double, currencyCode: String) {
    Row(
        Modifier
            .fillMaxWidth()
            // If this screen sits on a `surface`-coloured parent rather than
            // `background`, change this or rows will show a seam scrolling under.
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Text(
            formatMoney(total, currencyCode),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    currencyCode: String,
    nameOf: (String) -> String,
    canEdit: Boolean,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    isSwipeOpen: Boolean,
    onSwipeOpenChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    SwipeableExpenseCard(
        canEdit = canEdit,
        isSwipeOpen = isSwipeOpen,
        onSwipeOpenChange = onSwipeOpenChange,
        onDelete = onDelete,
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            shape = CardShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth().clickable { onToggleExpand() }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategoryAvatar(expense.effectiveCategory)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(expense.description, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${s.paidBy} ${nameOf(expense.paidByTravellerId)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                    Text(
                        formatMoney(expense.amount, currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (expanded) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Column(
                        Modifier.fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 12.dp),
                    ) {
                        Text(
                            s.splitLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.height(6.dp))
                        expense.shares
                            .filter { it.travellerId != expense.paidByTravellerId && it.amount > 0.0 }
                            .forEach { share ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                    Text(
                                        "${nameOf(share.travellerId)} ${s.owes} ${nameOf(expense.paidByTravellerId)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        formatMoney(share.amount, currencyCode),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        Spacer(Modifier.height(6.dp))
                        if (canEdit) {
                            TextButton(
                                onClick = onEdit,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            ) {
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
}

// ─────────────────────────────────────────────────────────────────────────────
// Balances lens — the four old accordions, flattened into one scroll
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("LongParameterList")
private fun LazyListScope.balancesLens(
    trip: Trip,
    settlements: List<Settlement>,
    pairwise: List<PairDebt>,
    spendingByTraveller: List<Pair<Traveller, Double>>,
    payments: List<Payment>,
    currentUid: String,
    isOwner: Boolean,
    isSettled: Boolean,
    nameOf: (String) -> String,
    onMarkPaid: (String, String, Double) -> Unit,
    onDeletePayment: (String) -> Unit,
    onSetSettled: (Boolean) -> Unit,
) {
    item(key = "bal-settle") {
        val s = LocalStrings.current
        Surface(
            shape = CardShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                SectionLabel(s.settleUp, s.settleUpHint)
                Spacer(Modifier.height(8.dp))
                if (settlements.isEmpty()) {
                    Text(
                        s.allSettled,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
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

                if (isOwner) {
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (isSettled) {
                            OutlinedButton(
                                onClick = { onSetSettled(false) },
                                contentPadding = PaddingValues(horizontal = 60.dp, vertical = 6.dp),
                            ) { Text(s.unsettleTrip) }
                        } else {
                            Button(
                                onClick = { onSetSettled(true) },
                                contentPadding = PaddingValues(horizontal = 60.dp, vertical = 6.dp),
                                enabled = settlements.isNotEmpty(),
                            ) { Text(s.settleUpTrip) }
                        }
                    }
                }
            }
        }
    }

    if (spendingByTraveller.isNotEmpty()) {
        item(key = "bal-spend") {
            val s = LocalStrings.current
            CollapsibleBalanceCard(
                title = s.totalSpendings,
                hint = s.eachTravellerPaid,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                spendingByTraveller.forEach { (traveller, amount) ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            traveller.firstName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            formatMoney(amount, trip.currencyCode),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }

    if (pairwise.isNotEmpty()) {
        item(key = "bal-pairwise") {
            val s = LocalStrings.current
            CollapsibleBalanceCard(
                title = s.whoOwesWhom,
                hint = s.whoOwesWhomHint,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                pairwise.forEach { pd ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${nameOf(pd.fromTravellerId)} → ${nameOf(pd.toTravellerId)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            formatMoney(pd.amount, trip.currencyCode),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }

    if (payments.isNotEmpty()) {
        item(key = "bal-payments") {
            val s = LocalStrings.current
            CollapsibleBalanceCard(
                title = s.payments,
                hint = s.recordedRepayments,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                payments.sortedByDescending { it.createdAt }.forEach { p ->
                    val iMadeIt = trip.travellers
                        .firstOrNull { it.id == p.fromTravellerId }?.userId == currentUid
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp),
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

@Composable
private fun SectionLabel(title: String, hint: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(
            hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
    }
}

/** Collapsible card used for the "Total spendings" / "Who owes whom" / "Payments" sections. */
@Composable
private fun CollapsibleBalanceCard(
    title: String,
    hint: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Surface(
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth().animateContentSize(),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionLabel(title, hint, modifier = Modifier.weight(1f))
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.graphicsLayer { rotationZ = if (expanded) 180f else 0f },
                    )
                }
            }
            if (expanded) {
                Spacer(Modifier.height(6.dp))
                content()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state, banner, currency picker
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExpensesEmptyState(
    currencyCode: String,
    onShowCurrencyPicker: () -> Unit,
) {
    val s = LocalStrings.current
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // ⬅ CHANGED — was the "💶" emoji, which doesn't tint or scale with the theme.
        Box(
            Modifier.size(64.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Add, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(s.noExpenses, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            s.noExpensesSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onShowCurrencyPicker) {
            Text("${s.tripCurrency}: $currencyCode")
        }
    }
}

@Composable
private fun SettledBanner(
    settledByName: String,
    iOwe: Double,
    iAmOwed: Double,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val owes = iOwe > 0.01
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = if (owes) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ⬅ CHANGED — was the "💰" emoji.
            Box(
                Modifier.size(34.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
            }
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
private fun CurrencyPickerDialog(
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.tripCurrency) },
        shape = RoundedCornerShape(16.dp),
        text = {
            LazyColumn(Modifier.heightIn(max = 360.dp)) {
                items(COMMON_CURRENCIES) { code ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onPick(code) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(code, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        val sym = currencySymbol(code)
                        if (sym.isNotEmpty()) {
                            Text(
                                sym,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                        if (code == current) {
                            Spacer(Modifier.width(8.dp))
                            // ⬅ CHANGED — was the "✓" text glyph.
                            Icon(
                                Icons.Filled.Check, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Swipe-to-delete (unchanged — matches the home/weather cards)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SwipeableExpenseCard(
    canEdit: Boolean,
    isSwipeOpen: Boolean,
    onSwipeOpenChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (!canEdit) {
        Box(modifier.fillMaxWidth()) { content() }
        return
    }

    val density = LocalDensity.current
    val actionWidth = 80.dp
    val gap = 15.dp
    val panelWidth = actionWidth + gap
    val panelPx = with(density) { panelWidth.toPx() }
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val exitOffsetX = remember { Animatable(0f) }

    val progress = ((-offsetX.value - with(density) { gap.toPx() }) /
            (panelPx - with(density) { gap.toPx() })).coerceIn(0f, 1f)

    LaunchedEffect(isSwipeOpen) {
        if (!isSwipeOpen && offsetX.value != 0f) {
            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
        }
    }

    fun animateOutThenDelete() {
        scope.launch {
            val slide = with(density) { (panelWidth + 600.dp).toPx() }
            exitOffsetX.animateTo(-slide, tween(durationMillis = 300))
            onDelete()
        }
    }

    Box(
        modifier
            .fillMaxWidth()
            .offset { IntOffset(exitOffsetX.value.roundToInt(), 0) },
    ) {
        if (offsetX.value != 0f) {
            Row(
                Modifier.matchParentSize().clip(CardShape),
                horizontalArrangement = Arrangement.End,
            ) {
                Column(Modifier.width(panelWidth).fillMaxHeight().padding(start = gap)) {
                    ExpenseDeleteButton(progress, Modifier.weight(1f)) { animateOutThenDelete() }
                }
            }
        }

        Box(
            Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo((offsetX.value + dragAmount).coerceIn(-panelPx, 0f))
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value < -panelPx / 2) {
                                    offsetX.animateTo(
                                        -panelPx,
                                        spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow,
                                        ),
                                    )
                                    onSwipeOpenChange(true)
                                } else {
                                    offsetX.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium,
                                        ),
                                    )
                                    onSwipeOpenChange(false)
                                }
                            }
                        },
                    )
                },
        ) {
            content()
        }
    }
}

@Composable
private fun ExpenseDeleteButton(progress: Float, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val s = LocalStrings.current
    Column(
        modifier.fillMaxHeight().clickable(
            onClick = onClick,
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(52.dp)
                .graphicsLayer { scaleX = progress; scaleY = progress; alpha = progress }
                .clip(CircleShape)
                .background(Color(0xFFB23B3B)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = s.delete,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            s.delete,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}


private fun dayOf(epochMillis: Long): LocalDate =
    Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

/** "Sat 14 Sep" — reuses LocalDate.label() from Models.kt for the day/month part. */
private fun LocalDate.headerLabel(): String {
    val dow = dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "$dow ${label()}"
}