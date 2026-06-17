package com.itinera.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.data.CurrencyApi
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.round

private val currencies = listOf(
    "EUR", "USD", "GBP", "JPY", "CHF", "CAD", "AUD",
    "INR", "CNY", "SEK", "NOK", "PLN", "CZK", "DKK",
)

// Currency code -> flag emoji
private val currencyFlags = mapOf(
    "EUR" to "\uD83C\uDDEA\uD83C\uDDFA", "USD" to "\uD83C\uDDFA\uD83C\uDDF8",
    "GBP" to "\uD83C\uDDEC\uD83C\uDDE7", "JPY" to "\uD83C\uDDEF\uD83C\uDDF5",
    "CHF" to "\uD83C\uDDE8\uD83C\uDDED", "CAD" to "\uD83C\uDDE8\uD83C\uDDE6",
    "AUD" to "\uD83C\uDDE6\uD83C\uDDFA", "INR" to "\uD83C\uDDEE\uD83C\uDDF3",
    "CNY" to "\uD83C\uDDE8\uD83C\uDDF3", "SEK" to "\uD83C\uDDF8\uD83C\uDDEA",
    "NOK" to "\uD83C\uDDF3\uD83C\uDDF4", "PLN" to "\uD83C\uDDF5\uD83C\uDDF1",
    "CZK" to "\uD83C\uDDE8\uD83C\uDDFF", "DKK" to "\uD83C\uDDE9\uD83C\uDDF0",
)

private fun flagFor(code: String) = currencyFlags[code] ?: "\uD83C\uDFF3\uFE0F"

/** Round to [decimals] places, trimming a trailing ".0". */
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
    val scope = rememberCoroutineScope()
    val s = LocalStrings.current

    val textFieldShape = RoundedCornerShape(12.dp)
    val dropdownShape = RoundedCornerShape(12.dp)

    var amount by remember { mutableStateOf("") }
    var from by remember { mutableStateOf("EUR") }
    var to by remember { mutableStateOf("USD") }

    var result by remember { mutableStateOf<String?>(null) }
    var rateInfo by remember { mutableStateOf<String?>(null) }
    var history by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var rotated by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (rotated) 270f else 90f, label = "swapRotation")

    var validPill by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(validPill) {
        if (validPill != null) { delay(2000); validPill = null }
    }

    fun convert() {
        val value = amount.toDoubleOrNull()
        if (value == null) { validPill = s.validAmount; return }
        loading = true; error = null; result = null
        scope.launch {
            try {
                val (rate, date) = api.fetchRate(from, to)
                val converted = value * rate
                result = "${(converted * 100).toLong() / 100.0} $to"
                rateInfo = "1 $from = $rate $to \u00B7 $date"
                history = api.fetchHistory(from, to, 30)      // for the chart
            } catch (_: Exception) {
                error = s.couldntFetch
                history = emptyList()
            } finally {
                loading = false
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(s.currency)
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(s.amount) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                )

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CurrencyDropdown(
                        s.from, from, { from = it },
                        Modifier.weight(1f), shape = dropdownShape,
                    )
                    IconButton(onClick = {
                        val t = from; from = to; to = t
                        rotated = !rotated
                    }) {
                        Icon(
                            imageVector = Icons.Filled.SwapVert,
                            contentDescription = "Swap",
                            modifier = Modifier.rotate(rotation),
                        )
                    }
                    CurrencyDropdown(
                        s.to, to, { to = it },
                        Modifier.weight(1f), shape = dropdownShape,
                    )
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { convert() },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 120.dp),
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(s.convert)
                    }
                }

                Spacer(Modifier.height(24.dp))
                when {
                    error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                    result != null -> {
                        // ===== Result card (unchanged from your original) =====
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            tonalElevation = 2.dp,
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Text(
                                    "$amount $from =",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    result!!,
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

                        // ===== Chart BELOW the result card =====
                        if (history.size >= 2) {
                            Spacer(Modifier.height(16.dp))
                            RateChartCard(points = history, from = from, to = to)
                        }
                    }
                }
            }
        }

        // Invalid-amount pill
        AnimatedVisibility(
            visible = validPill != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(vertical = 150.dp)
                .statusBarsPadding()
                .padding(top = 16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.DarkGray.copy(alpha = 0.8f),
                shadowElevation = 6.dp,
            ) {
                Text(
                    validPill ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        }
    }
}

/** Trend chart: dashed gridlines, green line, end dot, axis labels. */
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

    val s = LocalStrings.current


    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "$from \u2192 $to (${s.days})",
                style = MaterialTheme.typography.bodySmall,
                color = labelColor,
            )
            Spacer(Modifier.height(12.dp))

            Row {
                Box(
                    Modifier
                        .weight(1f)
                        .height(150.dp),
                ) {
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
                Text(s.today, style = MaterialTheme.typography.bodySmall, color = labelColor)
            }
        }
    }
}

/** From/To dropdown - now shows the flag next to the code, in the field and the menu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    label: String,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier.padding(horizontal = 4.dp),
    shape: RoundedCornerShape = RoundedCornerShape(30.dp),
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = "${flagFor(selected)}  $selected",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            shape = shape,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp),
            shape = shape
        ) {
            currencies.forEach { code ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(flagFor(code), fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(code, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    onClick = {
                        onSelect(code)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                )
            }
        }
    }
}
