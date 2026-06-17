package com.itinera.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.data.CurrencyApi
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.math.round
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch

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
    // World majors
    "EUR" to "\uD83C\uDDEA\uD83C\uDDFA",  // 🇪🇺 Eurozone
    "USD" to "\uD83C\uDDFA\uD83C\uDDF8",  // 🇺🇸 United States
    "GBP" to "\uD83C\uDDEC\uD83C\uDDE7",  // 🇬🇧 United Kingdom
    "JPY" to "\uD83C\uDDEF\uD83C\uDDF5",  // 🇯🇵 Japan
    "CHF" to "\uD83C\uDDE8\uD83C\uDDED",  // 🇨🇭 Switzerland
    "CAD" to "\uD83C\uDDE8\uD83C\uDDE6",  // 🇨🇦 Canada
    "AUD" to "\uD83C\uDDE6\uD83C\uDDFA",  // 🇦🇺 Australia
    "CNY" to "\uD83C\uDDE8\uD83C\uDDF3",  // 🇨🇳 China
    "NZD" to "\uD83C\uDDF3\uD83C\uDDFF",  // 🇳🇿 New Zealand
    // Europe (non-euro)
    "SEK" to "\uD83C\uDDF8\uD83C\uDDEA",  // 🇸🇪 Sweden
    "NOK" to "\uD83C\uDDF3\uD83C\uDDF4",  // 🇳🇴 Norway
    "DKK" to "\uD83C\uDDE9\uD83C\uDDF0",  // 🇩🇰 Denmark
    "PLN" to "\uD83C\uDDF5\uD83C\uDDF1",  // 🇵🇱 Poland
    "CZK" to "\uD83C\uDDE8\uD83C\uDDFF",  // 🇨🇿 Czechia
    "HUF" to "\uD83C\uDDED\uD83C\uDDFA",  // 🇭🇺 Hungary
    // Asia
    "INR" to "\uD83C\uDDEE\uD83C\uDDF3",  // 🇮🇳 India
    "SGD" to "\uD83C\uDDF8\uD83C\uDDEC",  // 🇸🇬 Singapore
    "HKD" to "\uD83C\uDDED\uD83C\uDDF0",  // 🇭🇰 Hong Kong
    "KRW" to "\uD83C\uDDF0\uD83C\uDDF7",  // 🇰🇷 South Korea
    "IDR" to "\uD83C\uDDEE\uD83C\uDDE9",  // 🇮🇩 Indonesia
    "PHP" to "\uD83C\uDDF5\uD83C\uDDED",  // 🇵🇭 Philippines
    "THB" to "\uD83C\uDDF9\uD83C\uDDED",  // 🇹🇭 Thailand
    "MYR" to "\uD83C\uDDF2\uD83C\uDDFE",  // 🇲🇾 Malaysia
    "VND" to "\uD83C\uDDFB\uD83C\uDDF3",  // 🇻🇳 Vietnam
    "TWD" to "\uD83C\uDDF9\uD83C\uDDFC",  // 🇹🇼 Taiwan
    // Middle East / Gulf
    "AED" to "\uD83C\uDDE6\uD83C\uDDEA",  // 🇦🇪 UAE
    "SAR" to "\uD83C\uDDF8\uD83C\uDDE6",  // 🇸🇦 Saudi Arabia
    "QAR" to "\uD83C\uDDF6\uD83C\uDDE6",  // 🇶🇦 Qatar
    "KWD" to "\uD83C\uDDF0\uD83C\uDDFC",  // 🇰🇼 Kuwait
    "BHD" to "\uD83C\uDDE7\uD83C\uDDED",  // 🇧🇭 Bahrain
    "OMR" to "\uD83C\uDDF4\uD83C\uDDF2",  // 🇴🇲 Oman
    "ILS" to "\uD83C\uDDEE\uD83C\uDDF1",  // 🇮🇱 Israel
    "TRY" to "\uD83C\uDDF9\uD83C\uDDF7",  // 🇹🇷 Turkey
    // Africa
    "ZAR" to "\uD83C\uDDFF\uD83C\uDDE6",  // 🇿🇦 South Africa
    "EGP" to "\uD83C\uDDEA\uD83C\uDDEC",  // 🇪🇬 Egypt
    "NGN" to "\uD83C\uDDF3\uD83C\uDDEC",  // 🇳🇬 Nigeria
    "KES" to "\uD83C\uDDF0\uD83C\uDDEA",  // 🇰🇪 Kenya
    "MAD" to "\uD83C\uDDF2\uD83C\uDDE6",  // 🇲🇦 Morocco
    // South America
    "BRL" to "\uD83C\uDDE7\uD83C\uDDF7",  // 🇧🇷 Brazil
    "ARS" to "\uD83C\uDDE6\uD83C\uDDF7",  // 🇦🇷 Argentina
    "CLP" to "\uD83C\uDDE8\uD83C\uDDF1",  // 🇨🇱 Chile
    "COP" to "\uD83C\uDDE8\uD83C\uDDF4",  // 🇨🇴 Colombia
    "MXN" to "\uD83C\uDDF2\uD83C\uDDFD",  // 🇲🇽 Mexico
)

private fun flagFor(code: String) = currencyFlags[code] ?: "\uD83C\uDFF3\uFE0F"

private fun fmt(value: Double, decimals: Int): String {
    val f = 10.0.pow(decimals)
    val r = round(value * f) / f
    return if (r == r.toLong().toDouble()) r.toLong().toString() else r.toString()
}

@Composable
fun CurrencyScreen(
    onMessage: (String) -> Unit,
) {
    val api = remember { CurrencyApi() }
    val s = LocalStrings.current

    var fromAmount by remember { mutableStateOf("") }
    var toAmount by remember { mutableStateOf("") }
    var from by remember { mutableStateOf("EUR") }
    var to by remember { mutableStateOf("USD") }
    var editing by remember { mutableStateOf<String?>(null) }   // null initially

    var rateInfo by remember { mutableStateOf<String?>(null) }
    var history by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    var rotated by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val slidePx = with(density) { 82.dp.toPx() }    // approx card height + gap
    var swapTrigger by remember { mutableStateOf(0) }
    val topSlide = remember { Animatable(0f) }
    val bottomSlide = remember { Animatable(0f) }

    LaunchedEffect(swapTrigger) {
        if (swapTrigger == 0) return@LaunchedEffect
        topSlide.snapTo(slidePx)
        bottomSlide.snapTo(-slidePx)
        launch { topSlide.animateTo(0f, tween(400)) }
        bottomSlide.animateTo(0f, tween(400))
    }

    // Vertical swap animation: each card moves ~82dp (height 72 + spacer 10)
//    val cardOffset = 82.dp
//    val fromOffset by animateDpAsState(
//        targetValue = if (rotated) cardOffset else 0.dp,
//        animationSpec = tween(400)
//    )
//    val toOffset by animateDpAsState(
//        targetValue = if (rotated) -cardOffset else 0.dp,
//        animationSpec = tween(400)
//    )

    // Target values for heights: initially 72, then shrinks to 64 or enlarges to 84
    val fromHeight by animateDpAsState(
        when (editing) {
            "from" -> 64.dp
            "to" -> 84.dp
            else -> 72.dp
        }
    )
    val toHeight by animateDpAsState(
        when (editing) {
            "to" -> 64.dp
            "from" -> 84.dp
            else -> 72.dp
        }
    )

    val iconRotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(400),          // matches the card slide
        label = "swapIconRotation"
    )

    // ===== Live conversion: typing in FROM updates TO =====
    LaunchedEffect(fromAmount, from, to, editing) {
        if (editing != "from") return@LaunchedEffect
        val value = fromAmount.toDoubleOrNull()
        if (value == null) {
            toAmount = ""; rateInfo = null; history = emptyList()
            return@LaunchedEffect
        }
        delay(400)   // debounce
        error = null
        try {
            val (rate, date) = api.fetchRate(from, to)
            toAmount = fmt(value * rate, 2)
            rateInfo = "1 $from = ${fmt(rate, 4)} $to \u00B7 $date"
            history = api.fetchHistory(from, to, 30)
        } catch (_: Exception) {
            error = s.couldntFetch; history = emptyList()
        }
    }

    // ===== Live conversion: typing in TO updates FROM =====
    LaunchedEffect(toAmount, from, to, editing) {
        if (editing != "to") return@LaunchedEffect
        val value = toAmount.toDoubleOrNull()
        if (value == null) {
            fromAmount = ""; rateInfo = null; history = emptyList()
            return@LaunchedEffect
        }
        delay(400)
        error = null
        try {
            val (rate, date) = api.fetchRate(to, from)
            fromAmount = fmt(value * rate, 2)
            // keep the rate line oriented from -> to for consistency
            val (dispRate, dispDate) = api.fetchRate(from, to)
            rateInfo = "1 $from = ${fmt(dispRate, 4)} $to \u00B7 $dispDate"
            history = api.fetchHistory(from, to, 30)
        } catch (_: Exception) {
            error = s.couldntFetch; history = emptyList()
        }
    }

    val hasResult = fromAmount.toDoubleOrNull() != null && toAmount.isNotEmpty()

    Column(
        Modifier.fillMaxSize(),
    ) {
        TopBar(s.currency)
        
        Spacer(Modifier.height(16.dp))

        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

            // ===== Stacked cards with overlapping swap button =====
            Box(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth()) {
                    CurrencyCard(
                        currency = from,
                        amount = fromAmount,
                        onAmountChange = { 
                            val filtered = it.filter { c -> c.isDigit() || c == '.' }
                            fromAmount = filtered
                            editing = if (filtered.isEmpty()) null else "from"
                        },
                        currencyOptions = currencies,
                        onCurrencySelected = { from = it },
                        modifier = Modifier
                            .offset { IntOffset(0, topSlide.value.toInt()) }   // ⬅ slide
                            .height(fromHeight),
                        isEnlarged = editing == "to" // from enlarges if to is being edited
                    )
                    Spacer(Modifier.height(10.dp))
                    CurrencyCard(
                        currency = to,
                        amount = toAmount,
                        onAmountChange = { 
                            val filtered = it.filter { c -> c.isDigit() || c == '.' }
                            toAmount = filtered
                            editing = if (filtered.isEmpty()) null else "to"
                        },
                        currencyOptions = currencies,
                        onCurrencySelected = { to = it },
                        modifier = Modifier
                            .offset { IntOffset(0, bottomSlide.value.toInt()) } // ⬅ slide
                            .height(toHeight),
                        isEnlarged = editing == "from" // to enlarges if from is being edited
                    )
                }

                // Swap button — centered over the gap, blends with the dark screen
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(4.dp, MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp),
                ) {
                    IconButton(onClick = {
                        val tc = from; from = to; to = tc
                        val ta = fromAmount; fromAmount = toAmount; toAmount = ta
                        rotated = !rotated
                        editing = when (editing) {
                            "from" -> "to"
                            "to" -> "from"
                            else -> null
                        }
                        swapTrigger++              // ⬅ play the slide
                    }) {
                        Icon(
                            Icons.Filled.SwapVert,
                            contentDescription = "Swap",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.rotate(iconRotation),

                        )
                    }
                }
            }

            // ===== Result card + chart appear once a conversion exists =====
            if (error != null) {
                Spacer(Modifier.height(20.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            } else if (hasResult) {
                Spacer(Modifier.height(20.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 2.dp,
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "$fromAmount $from =",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$toAmount $to",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        rateInfo?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }

                if (history.size >= 2) {
                    Spacer(Modifier.height(16.dp))
                    RateChartCard(points = history, from = from, to = to)
                }
            }
        }
    }
}

/** A stacked currency card: flag + code + chevron on the left, amount on the right. */
@Composable
private fun CurrencyCard(
    currency: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    currencyOptions: List<String>,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isEnlarged: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    val fontSize by animateFloatAsState(if (isEnlarged) 30f else 22f)
    val alpha by animateFloatAsState(if (isEnlarged) 1f else 0.7f)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isEnlarged) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = if (isEnlarged) 4.dp else 0.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Currency selector
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { expanded = true },
                ) {
                    Text(flagFor(currency), fontSize = if (isEnlarged) 26.sp else 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        currency,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isEnlarged) FontWeight.Bold else FontWeight.Medium,
                        color = onSurface.copy(alpha = alpha),
                    )
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = onSurface.copy(alpha = alpha))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .heightIn(max = 280.dp)
                        .width(140.dp),
                ) {
                    currencyOptions.forEach { code ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(flagFor(code), fontSize = 20.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        code,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (code == currency) FontWeight.Bold else FontWeight.Normal,
                                        color = if (code == currency)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            },
                            onClick = { onCurrencySelected(code); expanded = false },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            // Amount (editable, right-aligned)
            Box(Modifier.weight(1f)) {
                val style = TextStyle(
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEnlarged) MaterialTheme.colorScheme.primary else onSurface,
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
}

@Composable
private fun RateChartCard(
    points: List<Pair<String, Double>>,
    from: String,
    to: String,
) {
    val accent = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    val values = points.map { it.second }
    val minV = values.min()
    val maxV = values.max()
    val midV = (minV + maxV) / 2.0
    val startLabel = points.first().first

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "$from \u2192 $to (30 days)",
                style = MaterialTheme.typography.bodySmall,
                color = labelColor,
            )
            Spacer(Modifier.height(12.dp))

            Row {
                Box(Modifier.weight(1f).height(150.dp)) {
                    Canvas(Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        listOf(0f, 0.5f, 1f).forEach { f ->
                            val y = h * f
                            drawLine(gridColor, Offset(0f, y), Offset(w, y), 1f, pathEffect = dash)
                        }
                        if (values.size >= 2) {
                            val range = (maxV - minV).takeIf { it != 0.0 } ?: 1.0
                            val usableW = w - 14.dp.toPx()
                            val stepX = usableW / (values.size - 1)
                            val path = Path()
                            values.forEachIndexed { i, v ->
                                val x = stepX * i
                                val y = h - ((v - minV) / range * h).toFloat()
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path, color = accent, style = Stroke(width = 3.dp.toPx()))
                            val lastX = stepX * (values.size - 1)
                            val lastY = h - ((values.last() - minV) / range * h).toFloat()
                            drawCircle(accent.copy(alpha = 0.25f), 11.dp.toPx(), Offset(lastX, lastY))
                            drawCircle(accent, 5.dp.toPx(), Offset(lastX, lastY))
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(
                    Modifier.height(150.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(fmt(maxV, 4), style = MaterialTheme.typography.bodySmall, color = labelColor)
                    Text(fmt(midV, 4), style = MaterialTheme.typography.bodySmall, color = labelColor)
                    Text(fmt(minV, 2), style = MaterialTheme.typography.bodySmall, color = labelColor)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(startLabel, style = MaterialTheme.typography.bodySmall, color = labelColor)
                Spacer(Modifier.weight(1f))
                Text("Today", style = MaterialTheme.typography.bodySmall, color = labelColor)
            }
        }
    }
}