package com.itinera.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Expense
import com.itinera.app.model.ExpenseCategory
import com.itinera.app.model.ExpenseShare
import com.itinera.app.model.Traveller
import com.itinera.app.model.Trip
import com.itinera.app.model.inferExpenseCategory
import com.itinera.app.ui.components.TopBar
import com.itinera.app.ui.components.color
import com.itinera.app.ui.components.icon
import com.itinera.app.ui.components.label
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.round
import kotlin.random.Random
import kotlin.time.Clock
import androidx.compose.material.icons.filled.ArrowDropDown
import com.itinera.app.model.isForeign
import kotlinx.datetime.toLocalDateTime
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

/** Keeps the amount field to digits + one separator + two decimals. */
private fun sanitizeAmount(old: String, new: String): String {
    val cleaned = new.replace(',', '.').filter { it.isDigit() || it == '.' }
    if (cleaned.count { it == '.' } > 1) return old
    val parts = cleaned.split('.')
    if (parts.size == 2 && parts[1].length > 2) return old
    return cleaned
}

@OptIn(ExperimentalTime::class)
private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    trip: Trip,
    existing: Expense?,
    onBack: () -> Unit,
    onSave: (Expense) -> Unit,
    defaultCurrency: String = trip.currencyCode,   // currency a new expense starts in
    fetchRate: suspend (from: String, to: String, isoDate: String) -> Pair<Double, String> = { _, _, d -> 1.0 to d },
) {
    val s = LocalStrings.current
    val travellers = trip.travellers
    val tripCurrency = trip.currencyCode

    // The amount and split are entered in [inputCurrency]; on save they're converted into
    // the trip's currency at the rate for the expense's day. Editing a foreign expense
    // shows what was actually paid (15000 HUF), not the converted figure.
    val existingForeign = existing?.isForeign == true
    var inputCurrency by remember(existing?.id) {
        mutableStateOf(if (existingForeign) existing!!.originalCurrency else if (existing != null) tripCurrency else defaultCurrency)
    }
    // Trip currency per 1 unit of inputCurrency; null while unknown.
    var rate by remember(existing?.id) { mutableStateOf<Double?>(if (existingForeign) existing!!.fxRate else null) }
    var rateDate by remember(existing?.id) { mutableStateOf(if (existingForeign) existing!!.fxDate else "") }
    var rateLoading by remember { mutableStateOf(false) }
    var rateFailed by remember { mutableStateOf(false) }
    var manualRateText by remember { mutableStateOf("") }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    val isForeignInput = inputCurrency != tripCurrency
    val expenseDay = remember(existing?.id) { isoDay(existing?.createdAt ?: nowMillis()) }

    LaunchedEffect(inputCurrency) {
        rateFailed = false
        manualRateText = ""
        when {
            !isForeignInput -> { rate = 1.0; rateDate = "" }
            existingForeign && inputCurrency == existing!!.originalCurrency -> {
                rate = existing.fxRate; rateDate = existing.fxDate   // keep the locked-in rate
            }
            else -> {
                rate = null
                rateLoading = true
                runCatching { fetchRate(inputCurrency, tripCurrency, expenseDay) }
                    .onSuccess { (r, d) -> rate = r; rateDate = d }
                    .onFailure { rateFailed = true }
                rateLoading = false
            }
        }
    }
    // Offline fallback: the person types "1 EUR = x HUF".
    val effectiveRate: Double? = rate ?: parseAmount(manualRateText).takeIf { it > 0.0 }?.let { 1.0 / it }

    fun toTrip(v: Double): Double = if (!isForeignInput) v else round(v * (effectiveRate ?: 0.0) * 100) / 100

    var amountText by remember(existing?.id) {
        val start = if (existingForeign) existing!!.originalAmount else existing?.amount ?: 0.0
        mutableStateOf(if (start == 0.0) "" else twoDecimalsPlain(start))
    }
    var description by remember(existing?.id) { mutableStateOf(existing?.description ?: "") }
    var paidById by remember(existing?.id) {
        mutableStateOf(
            existing?.paidByTravellerId
                ?: travellers.firstOrNull { it.isOwner }?.id
                ?: travellers.firstOrNull()?.id
                ?: ""
        )
    }
    var pickedCategoryName by remember(existing?.id) { mutableStateOf(existing?.category?.name) }
    var customMode by remember(existing?.id) { mutableStateOf(existing != null) }
    var splitExpanded by remember(existing?.id) { mutableStateOf(existing != null) }

    val involvedIds = remember(existing?.id, travellers) {
        travellers
            .filter { t -> existing?.shares?.any { it.travellerId == t.id } ?: true }
            .map { it.id }
            .toMutableStateList()
    }

    val customText = remember(existing?.id, travellers) {
        mutableStateMapOf<String, String>().apply {
            travellers.forEach { t ->
                val share = existing?.shares?.firstOrNull { it.travellerId == t.id }
                // Shares are stored in the trip currency; show them in what was paid.
                val shown = share?.amount?.let { a -> if (existingForeign && existing!!.fxRate > 0) a / existing.fxRate else a }
                put(t.id, shown?.let { twoDecimalsPlain(it) } ?: "")
            }
        }
    }

    val amount = parseAmount(amountText)
    val involved = travellers.map { it.id }.filter { it in involvedIds }
    val customSum = involved.sumOf { parseAmount(customText[it] ?: "") }
    val customMatches = abs(customSum - amount) < 0.01
    val perPerson = if (involved.isNotEmpty()) amount / involved.size else 0.0

    val suggested = remember(description) { inferExpenseCategory(description) }
    val picked = pickedCategoryName?.let { name ->
        ExpenseCategory.entries.firstOrNull { it.name == name }
    }
    val effectiveCategory = picked ?: suggested ?: ExpenseCategory.OTHER
    val showingSuggestion = picked == null && suggested != null

    val blocker: String? = when {
        amount <= 0.0 -> s.enterAmount
        isForeignInput && effectiveRate == null -> if (rateLoading) s.rateLoading else s.rateUnavailable
        description.isBlank() -> s.enterDescription
        paidById.isBlank() || involved.isEmpty() -> s.selectSomeone
        customMode && !customMatches -> {
            val diff = amount - customSum
            if (diff > 0) s.stillToAssign.replace("%s", formatMoney(diff, inputCurrency))
            else s.overAssigned.replace("%s", formatMoney(-diff, inputCurrency))
        }
        else -> null
    }
    val canSave = blocker == null

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() }
        }

    /** Shares in the trip currency, summing exactly to [total]. */
    fun buildShares(total: Double): List<ExpenseShare> {
        if (!customMode) return equalShares(total, involved)
        val converted = involved.map { ExpenseShare(it, toTrip(parseAmount(customText[it] ?: ""))) }
        // Converting each share rounds separately; put any leftover cent on the largest.
        val diff = round((total - converted.sumOf { it.amount }) * 100) / 100
        if (diff == 0.0 || converted.isEmpty()) return converted
        val biggest = converted.indices.maxBy { converted[it].amount }
        return converted.mapIndexed { i, sh -> if (i == biggest) sh.copy(amount = round((sh.amount + diff) * 100) / 100) else sh }
    }

    fun save() {
        val total = toTrip(amount)
        onSave(
            Expense(
                id = existing?.id ?: "exp_${Random.nextLong()}",
                tripId = trip.id,
                description = description.trim().toTitleCase(),
                amount = total,
                paidByTravellerId = paidById,
                shares = buildShares(total),
                createdAt = existing?.createdAt ?: nowMillis(),
                category = effectiveCategory,
                createdBy = existing?.createdBy ?: "",
                originalAmount = if (isForeignInput) amount else 0.0,
                originalCurrency = if (isForeignInput) inputCurrency else "",
                fxRate = if (isForeignInput) effectiveRate ?: 1.0 else 1.0,
                fxDate = if (isForeignInput) rateDate.ifBlank { expenseDay } else "",
            )
        )
    }

    var pillText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(pillText) {
        if (pillText != null) {
            delay(2200)
            pillText = null
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().imePadding()) {
            TopBar(
                title = if (existing == null) s.addExpense else s.editExpense,
                onBack = onBack,
            )

            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                AmountField(
                    value = amountText,
                    onValueChange = { amountText = sanitizeAmount(amountText, it) },
                    currencyCode = inputCurrency,
                    onPickCurrency = { showCurrencyPicker = true },
                )

                if (isForeignInput) {
                    ConversionLine(
                        tripCurrency = tripCurrency,
                        inputCurrency = inputCurrency,
                        converted = if (amount > 0.0 && effectiveRate != null) toTrip(amount) else null,
                        rate = effectiveRate,
                        rateDate = rateDate,
                        loading = rateLoading,
                        failed = rateFailed && rate == null,
                        manualRateText = manualRateText,
                        onManualRateChange = { manualRateText = sanitizeAmount(manualRateText, it) },
                    )
                }

                Spacer(Modifier.height(20.dp))

                CategorySection(
                    selected = effectiveCategory,
                    isSuggestion = showingSuggestion,
                    onPick = { pickedCategoryName = it.name },
                )

                Spacer(Modifier.height(20.dp))

                UnderlineField(
                    label = s.description,
                    value = description.toTitleCase(),
                    onValueChange = { description = it },
                    placeholder = s.descriptionHint,
                )

                Spacer(Modifier.height(22.dp))

                Text(
                    s.paidByLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(10.dp))
                PayerRow(
                    travellers = travellers,
                    selectedId = paidById,
                    onSelect = { paidById = it },
                )

                Spacer(Modifier.height(22.dp))

                SplitCard(
                    travellers = travellers,
                    involvedIds = involvedIds,
                    customText = customText,
                    customMode = customMode,
                    onCustomModeChange = { customMode = it },
                    expanded = splitExpanded,
                    onToggleExpanded = { splitExpanded = !splitExpanded },
                    amount = amount,
                    customSum = customSum,
                    perPerson = perPerson,
                    currencyCode = inputCurrency,
                    onSplitEvenly = {
                        equalShares(amount, involved).forEach { share ->
                            customText[share.travellerId] = twoDecimalsPlain(share.amount)
                        }
                    },
                    canSave = canSave,
                    onSave = { save() },
                    onBlocked = { pillText = blocker },
                )

                Spacer(Modifier.height(24.dp))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }

        AnimatedVisibility(
            visible = pillText != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                // ⬅ CHANGED — was Color.DarkGray, which didn't adapt to dark mode.
                color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 6.dp,
            ) {
                Text(
                    pillText.orEmpty(),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            current = inputCurrency,
            title = s.currency,
            onPick = { inputCurrency = it; showCurrencyPicker = false },
            onDismiss = { showCurrencyPicker = false },
        )
    }
}

/**
 * Under a foreign amount: "≈ €38.42 · 1 EUR = 390.40 HUF · 2026-10-05", or a loading
 * line, or — when the rate couldn't be fetched — a field to type "1 EUR = x HUF".
 */
@Composable
private fun ConversionLine(
    tripCurrency: String,
    inputCurrency: String,
    converted: Double?,
    rate: Double?,
    rateDate: String,
    loading: Boolean,
    failed: Boolean,
    manualRateText: String,
    onManualRateChange: (String) -> Unit,
) {
    val s = LocalStrings.current
    val muted = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    Column(
        Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            loading -> Text(s.rateLoading, style = MaterialTheme.typography.bodySmall, color = muted)
            failed -> {
                Text(s.rateUnavailable, style = MaterialTheme.typography.bodySmall, color = muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("1 $tripCurrency = ", style = MaterialTheme.typography.bodyLarge)
                    ShareAmountField(value = manualRateText, onValueChange = onManualRateChange)
                    Text(" $inputCurrency", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        if (converted != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                "≈ ${formatMoney(converted, tripCurrency)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (rate != null && rate > 0.0 && !failed) {
            Text(
                listOf("1 $tripCurrency = ${rateText(1.0 / rate)} $inputCurrency", rateDate)
                    .filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = muted,
            )
        }
    }
}

/** Enough digits to be useful for both 390.40 and 0.8567. */
private fun rateText(v: Double): String {
    val decimals = if (v >= 100) 2 else 4
    var factor = 1.0
    repeat(decimals) { factor *= 10 }
    val scaled = round(v * factor).toLong()
    val whole = scaled / factor.toLong()
    val frac = (scaled % factor.toLong()).toString().padStart(decimals, '0')
    return "$whole.$frac"
}

private fun isoDay(epochMillis: Long): String =
    kotlinx.datetime.Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.toString()

@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    currencyCode: String,
    onPickCurrency: () -> Unit,
) {
    val amountTextStyle = MaterialTheme.typography.displaySmall.copy(
        fontSize = 44.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Start,
    )

    // ⬅ CHANGED — measure the real glyph width instead of guessing (was a fixed
    // 26.dp/char estimate, then an unbounded widthIn — both left the field either
    // clipping digits or stretching to fill the row and losing centering). Sizing
    // to the exact measured width keeps the whole "currency + amount" cluster
    // tight, so the surrounding Column can center it truly, with a small cursor
    // buffer so the caret after the last digit is never cropped either.
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val fieldWidth = remember(value, amountTextStyle) {
        val measured = textMeasurer.measure(
            text = value.ifEmpty { "0" },
            style = amountTextStyle,
        )
        with(density) { measured.size.width.toDp() + 16.dp }
    }

    Column(
        Modifier.fillMaxWidth().padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                currencySymbolOrCode(currencyCode).trim(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                modifier = Modifier.padding(bottom = 6.dp, end = 4.dp),
            )
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        "0",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = amountTextStyle,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(fieldWidth),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        // Tap to pay in another currency (converted to the trip's on save).
        Surface(
            onClick = onPickCurrency,
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            ) {
                Text(
                    currencyCode,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                )
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = LocalStrings.current.currency,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(
    selected: ExpenseCategory,
    isSuggestion: Boolean,
    onPick: (ExpenseCategory) -> Unit,
) {
    val s = LocalStrings.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            s.category,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        // Says the guess is a guess. Without this the chip looks like a choice
        // the user already made, and a wrong one gets saved unnoticed.
        AnimatedVisibility(visible = isSuggestion, enter = fadeIn(), exit = fadeOut()) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text(
                    s.suggested,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ExpenseCategory.entries.forEach { cat ->
            val active = cat == selected
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (active) cat.color.copy(alpha = 0.14f) else Color.Transparent,
                border = BorderStroke(
                    if (active) 1.dp else 0.5.dp,
                    if (active) cat.color.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                ),
                modifier = Modifier.clickable { onPick(cat) },
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        cat.icon,
                        contentDescription = null,
                        tint = if (active) cat.color
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        cat.label(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (active) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
private fun UnderlineField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(6.dp))
        Box(contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }
}

@Composable
private fun PayerRow(
    travellers: List<Traveller>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    // ⬅ CHANGED — was an ExposedDropdownMenuBox: two taps and a menu animation
    // for what is usually a choice between two or three people.
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        travellers.forEach { t ->
            val active = t.id == selectedId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onSelect(t.id) },
            ) {
                Box(
                    Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        )
                        .border(
                            width = if (active) 2.dp else 0.dp,
                            color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        t.firstName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    t.firstName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }
        }
    }
}

@Composable
private fun SplitCard(
    travellers: List<Traveller>,
    involvedIds: SnapshotStateList<String>,
    customText: SnapshotStateMap<String, String>,
    customMode: Boolean,
    onCustomModeChange: (Boolean) -> Unit,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    amount: Double,
    customSum: Double,
    perPerson: Double,
    currencyCode: String,
    onSplitEvenly: () -> Unit,
    canSave: Boolean,
    onSave: () -> Unit,
    onBlocked: () -> Unit,
) {
    val s = LocalStrings.current
    val count = involvedIds.size

    Surface(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(Modifier.fillMaxWidth()) {
            // Collapsed summary: for most expenses this is the whole story.
            Row(
                Modifier.fillMaxWidth().clickable { onToggleExpanded() }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Groups,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (customMode) s.splitCustomShort else s.splitEquallyShort,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        if (customMode) s.nPeople.replace("%s", "$count")
                        else s.nPeopleEach
                            .replace("%1\$s", "$count")
                            .replace("%2\$s", formatMoney(perPerson, currencyCode)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                Column(Modifier.fillMaxWidth().padding(14.dp)) {

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        listOf(false to s.splitEqually, true to s.splitCustom).forEach { (mode, label) ->
                            val active = mode == customMode
                            Surface(
                                modifier = Modifier.weight(1f).clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { onCustomModeChange(mode) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (active) MaterialTheme.colorScheme.surface else Color.Transparent,
                            ) {
                                Box(Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (active) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    travellers.forEach { t ->
                        val checked = t.id in involvedIds
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (checked) involvedIds.remove(t.id) else involvedIds.add(t.id)
                                }
                                .padding(vertical = 8.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RoundCheckbox(checked = checked)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                t.firstName,
                                Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (checked) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                            if (checked) {
                                if (customMode) {
                                    ShareAmountField(
                                        value = customText[t.id] ?: "",
                                        onValueChange = {
                                            customText[t.id] = sanitizeAmount(customText[t.id] ?: "", it)
                                        },
                                    )
                                } else {
                                    Text(
                                        formatMoney(perPerson, currencyCode),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }

                    if (customMode && amount > 0.0) {
                        Spacer(Modifier.height(10.dp))
                        val fraction by animateFloatAsState(
                            targetValue = (customSum / amount).toFloat().coerceIn(0f, 1f),
                            animationSpec = tween(220),
                        )
                        val over = customSum > amount + 0.001
                        val done = abs(customSum - amount) < 0.01
                        Box(
                            Modifier.fillMaxWidth().height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
                        ) {
                            Box(
                                Modifier.fillMaxWidth(fraction).fillMaxHeight().background(
                                    when {
                                        over -> MaterialTheme.colorScheme.error
                                        done -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                                    }
                                ),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val diff = amount - customSum
                            Text(
                                when {
                                    done -> s.splitMatches
                                    diff > 0 -> s.stillToAssign.replace("%s", formatMoney(diff, currencyCode))
                                    else -> s.overAssigned.replace("%s", formatMoney(-diff, currencyCode))
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    done -> MaterialTheme.colorScheme.primary
                                    over -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                },
                                modifier = Modifier.weight(1f),
                            )
                            if (!done) {
                                TextButton(
                                    onClick = onSplitEvenly,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                ) { Text(s.splitEvenly, style = MaterialTheme.typography.labelMedium) }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Box(Modifier.fillMaxWidth().padding(14.dp), contentAlignment = Alignment.Center) {
                SaveExpenseButton(
                    enabled = canSave,
                    onSave = onSave,
                    onBlocked = onBlocked,
                )
            }
        }
    }
}

@Composable
private fun ShareAmountField(value: String, onValueChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier.width(86.dp),
    ) {
        Box(
            Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (value.isEmpty()) {
                Text(
                    "0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// Styled to match the Save Leg / Save Place buttons exactly: same ButtonDefaults
// colors, same size/shape. `enabled` stays true at the Button level (rather than
// using Button's real disabled state) so a tap while blocked still surfaces the
// "why can't I save" pill instead of doing nothing.
@Composable
private fun SaveExpenseButton(
    enabled: Boolean,
    onSave: () -> Unit,
    onBlocked: () -> Unit,
) {
    val s = LocalStrings.current
    val colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)

    val container by animateColorAsState(
        targetValue = if (enabled) colors.containerColor else colors.disabledContainerColor,
        animationSpec = tween(200),
    )
    val content by animateColorAsState(
        targetValue = if (enabled) colors.contentColor else colors.disabledContentColor,
        animationSpec = tween(200),
    )

    Button(
        onClick = { if (enabled) onSave() else onBlocked() },
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        modifier = Modifier.fillMaxWidth().height(50.dp),
    ) { Text(s.saveExpense) }
}

private fun twoDecimalsPlain(v: Double): String {
    val cents = round(v * 100).toLong()
    val whole = cents / 100
    val frac = abs(cents % 100).toInt()
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
    val border = if (checked) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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