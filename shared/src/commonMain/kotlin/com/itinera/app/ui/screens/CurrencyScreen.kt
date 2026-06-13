package com.itinera.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.itinera.app.data.CurrencyApi
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val currencies = listOf(
    "EUR", "USD", "GBP", "JPY", "CHF", "CAD", "AUD",
    "INR", "CNY", "SEK", "NOK", "PLN", "CZK", "DKK",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen() {
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
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var rotated by remember { mutableStateOf(false) }

    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(2000)        // kotlinx.coroutines.delay
            toast = null
        }
    }


    val rotation by animateFloatAsState(
        targetValue = if (rotated) 270f else 90f,
        label = "swapRotation"
    )

    fun convert() {
        val value = amount.toDoubleOrNull()
        if (value == null) { toast = s.validAmount; return }
        loading = true; error = null; result = null
        scope.launch {
            try {
                val (rate, date) = api.fetchRate(from, to)
                val converted = value * rate
                result = "${(converted * 100).toLong() / 100.0} $to"
                rateInfo = "1 $from = $rate $to · $date"
            } catch (_: Exception) {
                error = s.couldntFetch
            } finally {
                loading = false
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // ...your existing TopBar, fields, Convert button, result...
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 160.dp), // Increased from 80.dp
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.DarkGray.copy(alpha = 0.6f),
            )
            {
                Text(
                    toast ?: "",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(s.currency)
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

//            Spacer(Modifier.height(5.dp))

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
                CurrencyDropdown(s.from, from, { from = it }, Modifier.weight(1f),shape = dropdownShape)
                IconButton(onClick = {
                    val t = from
                    from = to
                    to = t
                    rotated = !rotated
                }) {
                    Icon(
                        imageVector = Icons.Filled.SwapVert,
                        contentDescription = "Swap",
                        modifier = Modifier.rotate(rotation)
                    )
                }
                CurrencyDropdown(s.to, to, { to = it }, Modifier.weight(1f), shape = dropdownShape)

            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { convert() },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 100.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(s.convert)
                }
            }


            Spacer(Modifier.height(24.dp))
            when {
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                result != null -> Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 2.dp,
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("$amount $from =", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(Modifier.height(4.dp))
                        Text(result!!, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        rateInfo?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    label: String,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            shape = shape, // <-- THIS IS THE IMPORTANT LINE
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { code ->
                DropdownMenuItem(
                    text = { Text(code) },
                    onClick = {
                        onSelect(code)
                        expanded = false
                    }
                )
            }
        }
    }
}