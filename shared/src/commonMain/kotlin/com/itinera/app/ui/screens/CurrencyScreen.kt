package com.itinera.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.data.CurrencyApi
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.label
import com.itinera.app.ui.components.TopBar
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

/*
 * ─── New string keys (EN + FR) ──────────────────────────────────────────────
 *   low            "Low"              / "Min"
 *   high           "High"             / "Max"
 *   average        "Average"          / "Moyenne"
 *   fromYourTrips  "From your trips"  / "De vos voyages"
 *   range7d        "7D"               / "7J"
 *   range30d       "30D"              / "30J"
 *   range90d       "90D"              / "90J"
 *   range1y        "1Y"               / "1A"
 *   quickAmounts   "Quick amounts"    / "Montants rapides"
 *   recentHistory  "Recent history"   / "Historique récent"
 *
 * Reused: currency, swap, today, couldntFetch.
 * `s.days` is no longer used here — the range selector replaced it.
 */

private val currencies = listOf(
    // World majors
    "EUR", "USD", "GBP", "JPY", "CHF", "CAD", "AUD", "CNY", "NZD",
    // Europe (non-euro)
    "SEK", "NOK", "DKK", "PLN", "CZK", "HUF",
    // Asia
    "INR", "SGD", "HKD", "KRW", "IDR", "PHP", "THB", "MYR", "VND", "TWD",
    // Middle East / Gulf
    "AED", "SAR", "QAR", "KWD", "BHD", "OMR", "ILS", "TRY",
    // Africa
    "ZAR", "EGP", "NGN", "KES", "MAD",
    // South America
    "BRL", "ARS", "CLP", "COP", "MXN",
)

private val currencyFlags = mapOf(
    "EUR" to "\uD83C\uDDEA\uD83C\uDDFA", "USD" to "\uD83C\uDDFA\uD83C\uDDF8",
    "GBP" to "\uD83C\uDDEC\uD83C\uDDE7", "JPY" to "\uD83C\uDDEF\uD83C\uDDF5",
    "CHF" to "\uD83C\uDDE8\uD83C\uDDED", "CAD" to "\uD83C\uDDE8\uD83C\uDDE6",
    "AUD" to "\uD83C\uDDE6\uD83C\uDDFA", "CNY" to "\uD83C\uDDE8\uD83C\uDDF3",
    "NZD" to "\uD83C\uDDF3\uD83C\uDDFF", "SEK" to "\uD83C\uDDF8\uD83C\uDDEA",
    "NOK" to "\uD83C\uDDF3\uD83C\uDDF4", "DKK" to "\uD83C\uDDE9\uD83C\uDDF0",
    "PLN" to "\uD83C\uDDF5\uD83C\uDDF1", "CZK" to "\uD83C\uDDE8\uD83C\uDDFF",
    "HUF" to "\uD83C\uDDED\uD83C\uDDFA", "INR" to "\uD83C\uDDEE\uD83C\uDDF3",
    "SGD" to "\uD83C\uDDF8\uD83C\uDDEC", "HKD" to "\uD83C\uDDED\uD83C\uDDF0",
    "KRW" to "\uD83C\uDDF0\uD83C\uDDF7", "IDR" to "\uD83C\uDDEE\uD83C\uDDE9",
    "PHP" to "\uD83C\uDDF5\uD83C\uDDED", "THB" to "\uD83C\uDDF9\uD83C\uDDED",
    "MYR" to "\uD83C\uDDF2\uD83C\uDDFE", "VND" to "\uD83C\uDDFB\uD83C\uDDF3",
    "TWD" to "\uD83C\uDDF9\uD83C\uDDFC", "AED" to "\uD83C\uDDE6\uD83C\uDDEA",
    "SAR" to "\uD83C\uDDF8\uD83C\uDDE6", "QAR" to "\uD83C\uDDF6\uD83C\uDDE6",
    "KWD" to "\uD83C\uDDF0\uD83C\uDDFC", "BHD" to "\uD83C\uDDE7\uD83C\uDDED",
    "OMR" to "\uD83C\uDDF4\uD83C\uDDF2", "ILS" to "\uD83C\uDDEE\uD83C\uDDF1",
    "TRY" to "\uD83C\uDDF9\uD83C\uDDF7", "ZAR" to "\uD83C\uDDFF\uD83C\uDDE6",
    "EGP" to "\uD83C\uDDEA\uD83C\uDDEC", "NGN" to "\uD83C\uDDF3\uD83C\uDDEC",
    "KES" to "\uD83C\uDDF0\uD83C\uDDEA", "MAD" to "\uD83C\uDDF2\uD83C\uDDE6",
    "BRL" to "\uD83C\uDDE7\uD83C\uDDF7", "ARS" to "\uD83C\uDDE6\uD83C\uDDF7",
    "CLP" to "\uD83C\uDDE8\uD83C\uDDF1", "COP" to "\uD83C\uDDE8\uD83C\uDDF4",
    "MXN" to "\uD83C\uDDF2\uD83C\uDDFD",
)

private fun flagFor(code: String) = currencyFlags[code] ?: "\uD83C\uDFF3\uFE0F"

private fun fmt(value: Double, decimals: Int): String {
    val f = 10.0.pow(decimals)
    val r = round(value * f) / f
    return if (r == r.toLong().toDouble()) r.toLong().toString() else r.toString()
}

/** Digits plus one separator, max two decimals — "1.2.3" used to parse to 0.0. */
private fun sanitizeAmount(old: String, new: String): String {
    val cleaned = new.replace(',', '.').filter { it.isDigit() || it == '.' }
    if (cleaned.count { it == '.' } > 1) return old
    val parts = cleaned.split('.')
    if (parts.size == 2 && parts[1].length > 2) return old
    return cleaned
}

private val RANGES = listOf(7, 30, 90, 365)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CurrencyScreen(
    onMessage: (String) -> Unit,
    /**
     * Currency codes drawn from the user's trips, as code to place label
     * (e.g. "JPY" to "Japan"). Optional, so existing call sites still compile —
     * pass something and the shortcut row appears.
     */
    tripCurrencies: List<Pair<String, String>> = emptyList(),
) {
    val api = remember { CurrencyApi() }
    val s = LocalStrings.current

    // Not re-keyed to anything — this deliberately survives leaving and
    // coming back to the tab, so a conversion you made is still here later.
    // rememberSaveable, not remember: App.kt tears this screen's composition
    // down and rebuilds it when you switch tabs and come back (see the
    // SaveableStateProvider comment there) — rememberSaveable is what actually
    // survives that, so the last amount you converted is still here.
    var from by rememberSaveable { mutableStateOf("EUR") }
    var to by rememberSaveable { mutableStateOf("USD") }
    var fromAmount by rememberSaveable { mutableStateOf("") }
    var toAmount by rememberSaveable { mutableStateOf("") }
    // The rest of the screen (rate, chart, quick amounts, history, trip
    // shortcuts) only appears once the user has actually typed something.
    val hasAmount = (fromAmount.toDoubleOrNull() ?: 0.0) > 0.0

    var rate by remember { mutableStateOf<Double?>(null) }
    var rateDate by remember { mutableStateOf<String?>(null) }
    var history by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
    var rangeDays by remember { mutableStateOf(30) }
    var loadingRate by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var rotated by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val slidePx = with(density) { 58.dp.toPx() }
    var swapTrigger by remember { mutableStateOf(0) }
    val topSlide = remember { Animatable(0f) }
    val bottomSlide = remember { Animatable(0f) }

    val iconRotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(400),
        label = "swapIconRotation",
    )

    LaunchedEffect(swapTrigger) {
        if (swapTrigger == 0) return@LaunchedEffect
        topSlide.snapTo(slidePx)
        bottomSlide.snapTo(-slidePx)
        launch { topSlide.animateTo(0f, tween(400)) }
        bottomSlide.animateTo(0f, tween(400))
    }

    // ⬅ CHANGED — the rate is a property of the pair, not of the amount. It loads
    // on open and on every pair change, so the screen is never blank and typing
    // never touches the network.
    LaunchedEffect(from, to) {
        if (from == to) {
            rate = 1.0; rateDate = null; error = null
            return@LaunchedEffect
        }
        loadingRate = true
        error = null
        try {
            val (r, date) = api.fetchRate(from, to)
            rate = r
            rateDate = date
        } catch (_: Exception) {
            error = s.couldntFetch
            rate = null
        } finally {
            loadingRate = false
        }
    }

    LaunchedEffect(from, to, rangeDays) {
        if (from == to) {
            history = emptyList()
            return@LaunchedEffect
        }
        history = try {
            api.fetchHistory(from, to, rangeDays)
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ⬅ CHANGED — conversion is now local arithmetic against the cached rate.
    // It used to fire a network call per keystroke behind a 400ms debounce, and
    // the "to" branch called fetchRate twice per edit.
    fun recomputeFrom(source: String) {
        val r = rate ?: return
        if (source == "from") {
            val v = fromAmount.toDoubleOrNull()
            toAmount = if (v == null) "" else fmt(v * r, 2)
        } else {
            val v = toAmount.toDoubleOrNull()
            fromAmount = if (v == null) "" else fmt(v / r, 2)
        }
    }

    LaunchedEffect(rate) { recomputeFrom("from") }

    val values = history.map { it.second }
    val trendPct = if (values.size >= 2 && values.first() != 0.0) {
        (values.last() - values.first()) / values.first() * 100.0
    } else null

    Column(Modifier.fillMaxSize()) {
        TopBar(s.currency)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            ConverterCard(
                from = from,
                to = to,
                fromAmount = fromAmount,
                toAmount = toAmount,
                onFromAmountChange = {
                    fromAmount = sanitizeAmount(fromAmount, it)
                    recomputeFrom("from")
                },
                onToAmountChange = {
                    toAmount = sanitizeAmount(toAmount, it)
                    recomputeFrom("to")
                },
                onFromSelected = { from = it },
                onToSelected = { to = it },
                topOffset = { topSlide.value.roundToInt() },
                bottomOffset = { bottomSlide.value.roundToInt() },
                iconRotation = iconRotation,
                onSwap = {
                    val tc = from; from = to; to = tc
                    val ta = fromAmount; fromAmount = toAmount; toAmount = ta
                    // Invert optimistically so the figures stay right during the
                    // refetch; LaunchedEffect(from, to) confirms it a moment later.
                    rate = rate?.takeIf { it != 0.0 }?.let { 1.0 / it }
                    rotated = !rotated
                    swapTrigger++
                },
            )

            if (hasAmount) {
                Spacer(Modifier.height(14.dp))

                RateStrip(
                    from = from,
                    to = to,
                    rate = rate,
                    rateDate = rateDate,
                    trendPct = trendPct,
                    loading = loadingRate,
                    error = error,
                )

                if (history.size >= 2) {
                    Spacer(Modifier.height(14.dp))
                    RangeSelector(selected = rangeDays, onSelect = { rangeDays = it })
                    Spacer(Modifier.height(14.dp))
                    RateChart(points = history)
                    Spacer(Modifier.height(12.dp))
                    StatsRow(values = values)
                }

                rate?.let { r ->
                    Spacer(Modifier.height(22.dp))
                    QuickAmounts(
                        from = from,
                        to = to,
                        rate = r,
                        onPick = { picked ->
                            fromAmount = picked
                            recomputeFrom("from")
                        },
                    )
                }

                if (history.size >= 3) {
                    Spacer(Modifier.height(22.dp))
                    RecentHistory(points = history)
                }

                if (tripCurrencies.isNotEmpty()) {
                    Spacer(Modifier.height(22.dp))
                    Text(
                        s.fromYourTrips,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        tripCurrencies.filter { it.first != to }.forEach { (code, place) ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Transparent,
                                border = BorderStroke(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                ),
                                modifier = Modifier.clickable { to = code },
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(code, style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        place,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Converter
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConverterCard(
    from: String,
    to: String,
    fromAmount: String,
    toAmount: String,
    onFromAmountChange: (String) -> Unit,
    onToAmountChange: (String) -> Unit,
    onFromSelected: (String) -> Unit,
    onToSelected: (String) -> Unit,
    topOffset: () -> Int,
    bottomOffset: () -> Int,
    iconRotation: Float,
    onSwap: () -> Unit,
) {
    val s = LocalStrings.current
    // ⬅ CHANGED — was two Surfaces with a floating circle bridging the gap. One
    // card with a hairline means the swap button sits on a real edge instead of
    // faking a cutout with a 4.dp background-coloured border.
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                CurrencyRow(
                    currency = from,
                    amount = fromAmount,
                    onAmountChange = onFromAmountChange,
                    onCurrencySelected = onFromSelected,
                    emphasised = false,
                    modifier = Modifier.offset { IntOffset(0, topOffset()) },
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                CurrencyRow(
                    currency = to,
                    amount = toAmount,
                    onAmountChange = onToAmountChange,
                    onCurrencySelected = onToSelected,
                    emphasised = true,
                    modifier = Modifier.offset { IntOffset(0, bottomOffset()) },
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(40.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onSwap() },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.SwapVert,
                        contentDescription = s.swap,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp).rotate(iconRotation),
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrencyRow(
    currency: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
    emphasised: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { expanded = true },
            ) {
                Text(flagFor(currency), fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    currency,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.heightIn(max = 280.dp).width(150.dp),
            ) {
                currencies.forEach { code ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(flagFor(code), fontSize = 18.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    code,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (code == currency) FontWeight.SemiBold
                                    else FontWeight.Normal,
                                    color = if (code == currency)
                                        MaterialTheme.colorScheme.primary else onSurface,
                                )
                            }
                        },
                        onClick = { onCurrencySelected(code); expanded = false },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        // ⬅ CHANGED — end padding reserves the swap button's column. Without it
        // the right-aligned amount rendered underneath the button.
        Box(Modifier.weight(1f).padding(end = 52.dp)) {
            val style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = if (emphasised) MaterialTheme.colorScheme.primary else onSurface,
                textAlign = TextAlign.End,
            )
            BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
                textStyle = style,
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            if (amount.isEmpty()) {
                Text(
                    "0",
                    style = style.copy(color = onSurface.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Rate strip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RateStrip(
    from: String,
    to: String,
    rate: Double?,
    rateDate: String?,
    trendPct: Double?,
    loading: Boolean,
    error: String?,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            when {
                error != null -> Text(
                    error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                rate != null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "1 $from = ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        Text(
                            fmt(rate, 4),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            " $to",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                    rateDate?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        )
                    }
                }
            }
        }

        // ⬅ ADD — there's a network round-trip here and nothing used to move.
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        } else if (trendPct != null) {
            val up = trendPct >= 0
            val tint = if (up) PositiveGreen else MaterialTheme.colorScheme.error
            Surface(shape = RoundedCornerShape(20.dp), color = tint.copy(alpha = 0.14f)) {
                Text(
                    (if (up) "+" else "-") + fmt(abs(trendPct), 2) + "%",
                    style = MaterialTheme.typography.labelMedium,
                    color = tint,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Range, chart, stats
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RangeSelector(selected: Int, onSelect: (Int) -> Unit) {
    val s = LocalStrings.current
    val labels = mapOf(7 to s.range7d, 30 to s.range30d, 90 to s.range90d, 365 to s.range1y)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
    ) {
        Row(Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            RANGES.forEach { days ->
                val active = days == selected
                Surface(
                    modifier = Modifier.weight(1f).clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onSelect(days) },
                    shape = RoundedCornerShape(9.dp),
                    color = if (active) MaterialTheme.colorScheme.surface else Color.Transparent,
                ) {
                    Box(Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                        Text(
                            labels[days].orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
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

private val PositiveGreen = Color(0xFF1D9E75)

/**
 * Scrubbable rate chart. Press or drag to inspect any day in the range.
 *
 * Gesture handling is hand-rolled rather than `detectHorizontalDragGestures`
 * because this sits inside a `verticalScroll`: the gesture is only consumed
 * once it's clearly horizontal, so a vertical swipe on the chart still scrolls
 * the page instead of being swallowed.
 */
@Composable
private fun RateChart(points: List<Pair<String, Double>>) {
    val values = points.map { it.second }
    val up = values.last() >= values.first()
    val accent = if (up) PositiveGreen else MaterialTheme.colorScheme.error
    val onSurface = MaterialTheme.colorScheme.onSurface
    val guideColor = onSurface.copy(alpha = 0.3f)
    val density = LocalDensity.current

    var selected by remember(points) { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxWidth()) {
        // Fixed height, so selecting a point doesn't shift the chart down.
        Box(Modifier.fillMaxWidth().height(34.dp)) {
            val idx = selected
            if (idx != null) {
                val (date, value) = points[idx]
                Column {
                    Text(
                        fmt(value, 4),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        date,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        }

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(points) {
                    val slop = with(density) { 10.dp.toPx() }
                    val usableW = size.width - with(density) { 8.dp.toPx() }
                    val stepX = usableW / (values.size - 1).coerceAtLeast(1)

                    fun indexAt(x: Float): Int =
                        (x / stepX).roundToInt().coerceIn(0, values.lastIndex)

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        selected = indexAt(down.position.x)
                        var claimed = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break

                            val dx = change.position.x - down.position.x
                            val dy = change.position.y - down.position.y

                            if (!claimed) {
                                // Vertical first? Hand the gesture to the scroll.
                                if (abs(dy) > slop && abs(dy) > abs(dx)) {
                                    selected = null
                                    break
                                }
                                if (abs(dx) > slop) claimed = true
                            }
                            selected = indexAt(change.position.x)
                            if (claimed) change.consume()
                        }
                        selected = null
                    }
                },
        ) {
            val w = size.width
            val h = size.height
            val minV = values.min()
            val maxV = values.max()
            val range = (maxV - minV).takeIf { it != 0.0 } ?: 1.0
            val padY = 12.dp.toPx()
            val usableH = h - padY * 2
            val usableW = w - 8.dp.toPx()
            val stepX = usableW / (values.size - 1).coerceAtLeast(1)

            fun xAt(i: Int) = stepX * i
            fun yAt(v: Double) = padY + (usableH - ((v - minV) / range * usableH).toFloat())

            val line = Path().apply {
                values.forEachIndexed { i, v ->
                    if (i == 0) moveTo(xAt(i), yAt(v)) else lineTo(xAt(i), yAt(v))
                }
            }
            val area = Path().apply {
                addPath(line)
                lineTo(xAt(values.lastIndex), h)
                lineTo(0f, h)
                close()
            }

            drawPath(area, color = accent.copy(alpha = 0.12f), style = Fill)
            drawPath(line, color = accent, style = Stroke(width = 2.5.dp.toPx()))

            val idx = selected
            if (idx != null) {
                val sx = xAt(idx)
                val sy = yAt(values[idx])
                drawLine(
                    guideColor,
                    Offset(sx, 0f),
                    Offset(sx, h),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                )
                drawCircle(accent.copy(alpha = 0.22f), 11.dp.toPx(), Offset(sx, sy))
                drawCircle(accent, 5.dp.toPx(), Offset(sx, sy))
            } else {
                val lastX = xAt(values.lastIndex)
                val lastY = yAt(values.last())
                drawCircle(accent.copy(alpha = 0.25f), 10.dp.toPx(), Offset(lastX, lastY))
                drawCircle(accent, 4.5.dp.toPx(), Offset(lastX, lastY))
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(
                points.first().first,
                style = MaterialTheme.typography.labelSmall,
                color = onSurface.copy(alpha = 0.45f),
            )
            Spacer(Modifier.weight(1f))
            Text(
                LocalStrings.current.today,
                style = MaterialTheme.typography.labelSmall,
                color = onSurface.copy(alpha = 0.45f),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick amounts & recent history
// ─────────────────────────────────────────────────────────────────────────────

private val QUICK_STEPS = listOf(1, 10, 50, 100, 500)

/**
 * Tappable reference amounts. Tapping fills the converter, so this is a
 * shortcut rather than a read-only table — which is the difference between a
 * feature and decoration.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickAmounts(
    from: String,
    to: String,
    rate: Double,
    onPick: (String) -> Unit,
) {
    val s = LocalStrings.current
    Column(Modifier.fillMaxWidth()) {
        Text(
            s.quickAmounts,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            QUICK_STEPS.forEach { step ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    border = BorderStroke(
                        0.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    ),
                    modifier = Modifier.clickable { onPick(step.toString()) },
                ) {
                    Row(
                        Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "$step $from",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            fmt(step * rate, 2),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

/** ISO date from the API to your existing "14 Jul" format, falling back safely. */
private fun prettyDate(iso: String): String = try {
    LocalDate.parse(iso).label()
} catch (_: Exception) {
    iso
}

/**
 * Day-by-day table under the chart. The chart shows shape; this shows figures
 * you can read without scrubbing, plus each day's move against the one before.
 */
@Composable
private fun RecentHistory(points: List<Pair<String, Double>>, limit: Int = 7) {
    val s = LocalStrings.current
    val onSurface = MaterialTheme.colorScheme.onSurface

    // One extra point so the oldest visible row still has a previous day to
    // compare against.
    val window = points.takeLast(limit + 1)
    val rows = window.drop(1).mapIndexed { i, (date, value) ->
        val prev = window[i].second
        val pct = if (prev != 0.0) (value - prev) / prev * 100.0 else 0.0
        Triple(date, value, pct)
    }.reversed()

    if (rows.isEmpty()) return

    Column(Modifier.fillMaxWidth()) {
        Text(
            s.recentHistory,
            style = MaterialTheme.typography.labelLarge,
            color = onSurface.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
                rows.forEachIndexed { index, (date, value, pct) ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (index == 0) s.today else prettyDate(date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = onSurface.copy(alpha = if (index == 0) 0.9f else 0.65f),
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            fmt(value, 4),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.width(10.dp))
                        DeltaChip(pct)
                    }
                    if (index != rows.lastIndex) {
                        HorizontalDivider(color = onSurface.copy(alpha = 0.10f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeltaChip(pct: Double) {
    val flat = abs(pct) < 0.005
    val up = pct >= 0
    val tint = when {
        flat -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        up -> PositiveGreen
        else -> MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (flat) Color.Transparent else tint.copy(alpha = 0.13f),
        modifier = Modifier.width(66.dp),
    ) {
        Text(
            if (flat) "—" else (if (up) "+" else "-") + fmt(abs(pct), 2) + "%",
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 3.dp),
        )
    }
}

@Composable
private fun StatsRow(values: List<Double>) {
    val s = LocalStrings.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // ⬅ CHANGED — all three at 4 decimals. The old axis used fmt(minV, 2)
        // against fmt(maxV, 4), so the bottom label read "1.14" next to "1.1485".
        StatCell(s.low, fmt(values.min(), 4), Modifier.weight(1f))
        StatCell(s.high, fmt(values.max(), 4), Modifier.weight(1f))
        StatCell(s.average, fmt(values.average(), 4), Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}