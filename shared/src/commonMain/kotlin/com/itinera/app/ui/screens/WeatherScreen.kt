package com.itinera.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.itinera.app.data.DayForecast
import com.itinera.app.data.GeoPlace
import com.itinera.app.data.SavedCity
import com.itinera.app.data.WeatherResult
import com.itinera.app.data.WeatherService
import com.itinera.app.data.weatherEmoji
import com.itinera.app.data.weatherLabel
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.TopBar
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun WeatherScreen(
    cities: List<SavedCity>,
    onAddCity: (SavedCity) -> Unit,
    onRemoveCity: (SavedCity) -> Unit,
    onBack: () -> Unit,
) {
    val service = remember { WeatherService() }
    var showAdd by remember { mutableStateOf(false) }
    // which card is swiped open (one at a time)
    var openKey by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar("Weather", onBack = onBack)

            if (cities.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("🌤️", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No cities yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tap + to add a city and see its weather.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(cities, key = { it.key }) { city ->
                        SwipeableWeatherCard(
                            city = city,
                            service = service,
                            isOpen = openKey == city.key,
                            onOpenChange = { open -> openKey = if (open) city.key else null },
                            onDelete = { onRemoveCity(city); openKey = null },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd).offset(x = (-25).dp, y = 60.dp)
                .padding(end = 20.dp, bottom = 220.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add city")
        }
    }

    if (showAdd) {
        AddCityDialog(
            service = service,
            onPick = { place ->
                onAddCity(
                    SavedCity(
                        name = place.name,
                        country = place.country,
                        admin1 = place.admin1,
                        latitude = place.latitude,
                        longitude = place.longitude,
                    )
                )
                showAdd = false
            },
            onDismiss = { showAdd = false },
        )
    }
}

@Composable
private fun SwipeableWeatherCard(
    city: SavedCity,
    service: WeatherService,
    isOpen: Boolean,
    onOpenChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
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

    LaunchedEffect(isOpen) {
        if (!isOpen && offsetX.value != 0f) offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
    }

    fun animateOutThenDelete() {
        scope.launch {
            val slide = with(density) { (panelWidth + 600.dp).toPx() }
            exitOffsetX.animateTo(-slide, tween(durationMillis = 300))
            onDelete()
        }
    }

    // fetch weather for this city
    var weather by remember(city.key) { mutableStateOf<WeatherResult?>(null) }
    var loading by remember(city.key) { mutableStateOf(true) }
    var failed by remember(city.key) { mutableStateOf(false) }
    LaunchedEffect(city.key) {
        loading = true; failed = false
        val w = service.forecast(city.latitude, city.longitude, days = 5)
        if (w == null) failed = true else weather = w
        loading = false
    }

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier
            .fillMaxWidth()
            .offset { IntOffset(exitOffsetX.value.roundToInt(), 0) },
    ) {
        // Behind: delete action — only present while the card is swiped, so it
        // never peeks through during the expand/collapse content animation.
        if (offsetX.value != 0f) {
            Row(
                Modifier.matchParentSize().clip(CardShape),
                horizontalArrangement = Arrangement.End,
            ) {
                Column(Modifier.width(panelWidth).fillMaxHeight().padding(start = gap)) {
                    WeatherActionButton(
                        Icons.Filled.Delete, "Delete", Color(0xFFB23B3B), progress,
                        Modifier.weight(1f),
                    ) { animateOutThenDelete() }
                }
            }
        }

        // Front: the weather card
        Box(
            Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(city.key) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo((offsetX.value + dragAmount).coerceIn(-panelPx, 0f))
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value < -panelPx / 2) {
                                    offsetX.animateTo(-panelPx, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
                                    onOpenChange(true)
                                } else {
                                    offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
                                    onOpenChange(false)
                                }
                            }
                        },
                    )
                },
        ) {
            Surface(
                shape = CardShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().animateContentSize(),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (offsetX.value != 0f) {
                                scope.launch { offsetX.animateTo(0f, tween(250)); onOpenChange(false) }
                            } else if (weather != null) {
                                expanded = !expanded
                            }
                        }
                        .padding(16.dp),
                ) {
                    val where = buildString {
                        append(city.name)
                        if (city.country.isNotBlank()) append(" · ${city.country}")
                    }
                    // top row: place + current
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(where, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            when {
                                loading -> Text("Loading…", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                                failed -> Text("Couldn't load weather", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error)
                                weather != null -> Text(
                                    weatherLabel(weather!!.currentCode),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                )
                            }
                        }
                        if (weather != null) {
                            Text(weatherEmoji(weather!!.currentCode), style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "${weather!!.currentTemp.toInt()}°",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    // today's high/low (compact)
                    weather?.days?.firstOrNull()?.let { today ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "H ${today.maxTemp.toInt()}°  L ${today.minTemp.toInt()}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }

                    // expanded: 5-day forecast
                    if (expanded && weather != null) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(Modifier.height(8.dp))
                        weather!!.days.forEach { day ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(weatherEmoji(day.code), style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.width(12.dp))
                                Text(prettyDate(day.date), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${day.maxTemp.toInt()}° / ${day.minTemp.toInt()}°",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
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
private fun WeatherActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bg: Color,
    progress: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
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
                .graphicsLayer {
                    scaleX = progress
                    scaleY = progress
                    alpha = progress
                }
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun AddCityDialog(
    service: WeatherService,
    onPick: (GeoPlace) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<GeoPlace>>(emptyList()) }
    var searched by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Add city") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search city") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            loading = true; searched = true
                            results = service.geocodeMany(query)
                            loading = false
                        }
                    },
                    enabled = query.isNotBlank() && !loading,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 60.dp),
                ) { Text("Search") }

                Spacer(Modifier.height(8.dp))

                when {
                    loading -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    searched && results.isEmpty() -> Text(
                        "No matches. Try another spelling.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    else -> LazyColumn(Modifier.heightIn(max = 280.dp)) {
                        items(results) { p ->
                            val label = buildString {
                                append(p.name)
                                if (p.admin1.isNotBlank() && p.admin1 != p.name) append(", ${p.admin1}")
                                if (p.country.isNotBlank()) append(" · ${p.country}")
                            }
                            Row(
                                Modifier.fillMaxWidth().clickable { onPick(p) }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        },
    )
}

/** "2026-06-26" -> "Fri, Jun 26". */
private fun prettyDate(iso: String): String {
    val parts = iso.split("-")
    if (parts.size != 3) return iso
    val y = parts[0].toIntOrNull() ?: return iso
    val m = parts[1].toIntOrNull() ?: return iso
    val d = parts[2].toIntOrNull() ?: return iso
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    var mm = m; var yy = y
    if (mm < 3) { mm += 12; yy -= 1 }
    val k = yy % 100; val j = yy / 100
    val h = (d + (13 * (mm + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
    val dow = listOf("Sat","Sun","Mon","Tue","Wed","Thu","Fri")[h]
    val mon = months.getOrElse(m - 1) { "" }
    return "$dow, $mon $d"
}