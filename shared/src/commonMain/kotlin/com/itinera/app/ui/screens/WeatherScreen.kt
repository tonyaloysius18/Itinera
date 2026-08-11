package com.itinera.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.data.GeoPlace
import com.itinera.app.data.SavedCity
import com.itinera.app.data.WeatherResult
import com.itinera.app.data.WeatherService
import com.itinera.app.data.weatherEmoji
import com.itinera.app.data.weatherLabel
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.TopBar
import com.itinera.app.ui.components.reorderableItem
import com.itinera.app.ui.components.rememberReorderableLazyListState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun WeatherScreen(
    cities: List<SavedCity>,
    onAddCity: (SavedCity) -> Unit,
    onRemoveCity: (SavedCity) -> Unit,
    onReorderCities: (List<SavedCity>) -> Unit = {},
    onBack: () -> Unit,
    /**
     * City names pulled from the user's trips, offered as shortcuts in the add
     * dialog. Optional, so existing call sites still compile.
     */
    tripCities: List<String> = emptyList(),
) {
    val s = LocalStrings.current
    val service = remember { WeatherService() }
    var showAdd by remember { mutableStateOf(false) }
    // which card is swiped open (one at a time)
    var openKey by remember { mutableStateOf<String?>(null) }
    var orderedCities by remember(cities) { mutableStateOf(cities) }
    val weatherListState = rememberLazyListState()

    fun moveCity(draggedKey: String, targetKey: String): List<SavedCity>? {
        val fromIndex = orderedCities.indexOfFirst { it.key == draggedKey }
        val toIndex = orderedCities.indexOfFirst { it.key == targetKey }
        if (fromIndex < 0 || toIndex < 0 || fromIndex == toIndex) return null
        return orderedCities.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    fun moveCityBy(key: String, delta: Int) {
        val fromIndex = orderedCities.indexOfFirst { it.key == key }
        val toIndex = fromIndex + delta
        if (fromIndex !in orderedCities.indices || toIndex !in orderedCities.indices) return
        val next = orderedCities.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        orderedCities = next
        onReorderCities(next)
    }

    val reorderState = rememberReorderableLazyListState(
        listState = weatherListState,
        onMove = { draggedKey, targetKey ->
            moveCity(draggedKey, targetKey)?.let {
                orderedCities = it
                openKey = null
            }
        },
        onDrop = { onReorderCities(orderedCities) },
    )

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(s.weather, onBack = onBack)

            if (orderedCities.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // ⬅ CHANGED — decoration, not data, so this one becomes an Icon.
                    // (Weather emoji stay on the cards: Open-Meteo returns 28 WMO
                    // codes and Material has about six weather glyphs.)
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.WbSunny,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        s.noCitiesYet,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        s.tapPlusAddCity,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    state = weatherListState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(orderedCities, key = { it.key }) { city ->
                        val index = orderedCities.indexOfFirst { it.key == city.key }
                        SwipeableWeatherCard(
                            city = city,
                            service = service,
                            isOpen = openKey == city.key,
                            onOpenChange = { open -> openKey = if (open) city.key else null },
                            onDelete = { onRemoveCity(city); openKey = null },
                            modifier = Modifier
                                .animateItem()
                                .reorderableItem(
                                    state = reorderState,
                                    key = city.key,
                                    itemLabel = city.name,
                                    canMoveUp = index > 0,
                                    canMoveDown = index in 0 until orderedCities.lastIndex,
                                    onMoveUp = { moveCityBy(city.key, -1) },
                                    onMoveDown = { moveCityBy(city.key, 1) },
                                ),
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd).offset(x = (-25).dp, y = (-90).dp)
                .padding(end = 20.dp, bottom = 24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
        ) {
            Icon(Icons.Filled.Add, contentDescription = s.addCity)
        }
    }

    if (showAdd) {
        AddCityDialog(
            service = service,
            tripCities = tripCities,
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

/**
 * Add-city dialog. Filters as you type — no Search button.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCityDialog(
    service: WeatherService,
    tripCities: List<String>,
    onPick: (GeoPlace) -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    val onSurface = MaterialTheme.colorScheme.onSurface

    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<GeoPlace>>(emptyList()) }
    var searched by remember { mutableStateOf(false) }

    // ⬅ CHANGED — was a Search button that had to be pressed before anything
    // happened. LaunchedEffect cancels and relaunches on every keystroke, so the
    // delay only completes once typing pauses — the debounce is free.
    LaunchedEffect(query) {
        val q = query.trim()
        if (q.length < 2) {
            // One letter matches hundreds of places; not worth a round trip.
            results = emptyList(); searched = false; loading = false
            return@LaunchedEffect
        }
        delay(350)
        loading = true
        results = runCatching { service.geocodeMany(q) }.getOrDefault(emptyList())
        loading = false
        searched = true
    }

    val focusRequester = remember { FocusRequester() }
    // The dialog exists to be typed into, so open the keyboard with it.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.close) } },
        title = { Text(s.addCity) },
        shape = RoundedCornerShape(20.dp),
        text = {
            Column {
                // Pill field, matching Documents and Trips.
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = onSurface.copy(alpha = 0.06f),
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Search, null,
                            tint = onSurface.copy(alpha = 0.45f),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    s.searchCity,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = onSurface.copy(alpha = 0.4f),
                                )
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = { query = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = onSurface),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            )
                        }
                        // Spinner sits in the field, so results don't jump while loading.
                        when {
                            loading -> CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp),
                                color = onSurface.copy(alpha = 0.45f),
                            )
                            query.isNotEmpty() -> Icon(
                                Icons.Filled.Close,
                                contentDescription = s.clearLabel,
                                tint = onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp).clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { query = "" },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (tripCities.isNotEmpty() && query.isBlank()) {
                    Text(
                        s.fromYourTrips,
                        style = MaterialTheme.typography.labelMedium,
                        color = onSurface.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        tripCities.forEach { name ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Transparent,
                                border = BorderStroke(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                ),
                                // Fills the box rather than adding directly: the name
                                // still has to be geocoded and disambiguated.
                                modifier = Modifier.clickable { query = name },
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Filled.Add, null,
                                        modifier = Modifier.size(13.dp),
                                        tint = onSurface.copy(alpha = 0.6f),
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(name, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                if (searched && results.isEmpty() && !loading) {
                    Text(
                        s.noCityMatches,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 10.dp),
                    )
                }

                LazyColumn(Modifier.heightIn(max = 300.dp)) {
                    items(results, key = { "${it.latitude},${it.longitude},${it.name}" }) { p ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onPick(p) }
                                .padding(vertical = 11.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Place,
                                contentDescription = null,
                                tint = onSurface.copy(alpha = 0.45f),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    p.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                // ⬅ CHANGED — region and country were concatenated into
                                // the name, so two "Munich" results were
                                // indistinguishable until you read to the end.
                                val where = listOf(p.admin1.takeIf { it != p.name }, p.country)
                                    .filterNotNull().filter { it.isNotBlank() }
                                    .joinToString(" · ")
                                if (where.isNotBlank()) {
                                    Text(
                                        where,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = onSurface.copy(alpha = 0.55f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    }
                }
            }
        },
    )
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
    val s = LocalStrings.current
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
                        Icons.Filled.Delete, s.delete, Color(0xFFB23B3B), progress,
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
                        // ⬅ ADD — a subtle gradient tinted by condition. Enough to
                        // tell cards apart without competing with the text.
                        .background(conditionGradient(weather?.currentCode))
                        .clickable {
                            if (offsetX.value != 0f) {
                                scope.launch { offsetX.animateTo(0f, tween(250)); onOpenChange(false) }
                            } else if (weather != null) {
                                expanded = !expanded
                            }
                        }
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            // ⬅ CHANGED — was "Dindigul · India" as the title with the
                            // condition on its own line below. Country moves down to
                            // join the condition, saving a line per card.
                            Text(
                                city.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(1.dp))
                            val subtitle = when {
                                loading -> s.loadingLabel
                                failed -> s.couldntLoadWeather
                                weather != null -> listOf(city.country, weatherLabel(weather!!.currentCode))
                                    .filter { it.isNotBlank() }.joinToString(" · ")
                                else -> city.country
                            }
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (failed) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (weather != null) {
                            // Emoji, deliberately: Open-Meteo returns 28 WMO codes and
                            // Material has about six weather glyphs. This is the one
                            // place emoji carry more information than icons.
                            Text(weatherEmoji(weather!!.currentCode), style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "${weather!!.currentTemp.toInt()}°",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "::",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(20.dp),
                        )
                    }

                    // ⬅ CHANGED — was "H 35°  L 25°" as a bare string. The bar spans
                    // the week's overall range, the segment is today's, the dot is now.
                    val days = weather?.days.orEmpty()
                    val today = days.firstOrNull()
                    if (today != null) {
                        val weekMin = days.minOf { it.minTemp }
                        val weekMax = days.maxOf { it.maxTemp }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${today.minTemp.toInt()}°",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.width(30.dp),
                            )
                            TempRangeBar(
                                dayMin = today.minTemp,
                                dayMax = today.maxTemp,
                                weekMin = weekMin,
                                weekMax = weekMax,
                                current = weather!!.currentTemp,
                                modifier = Modifier.weight(1f).height(6.dp),
                            )
                            Text(
                                "${today.maxTemp.toInt()}°",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(32.dp),
                                textAlign = TextAlign.End,
                            )
                            Spacer(Modifier.width(4.dp))
                            // ⬅ ADD — the card expanded on tap and nothing said so.
                            Icon(
                                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    if (expanded && weather != null && days.isNotEmpty()) {
                        val weekMin = days.minOf { it.minTemp }
                        val weekMax = days.maxOf { it.maxTemp }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(Modifier.height(6.dp))
                        days.forEachIndexed { index, day ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    if (index == 0) s.today else prettyDate(day.date, s),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.width(62.dp),
                                    maxLines = 1,
                                )
                                Text(weatherEmoji(day.code), style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${day.minTemp.toInt()}°",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.End,
                                )
                                Spacer(Modifier.width(8.dp))
                                TempRangeBar(
                                    dayMin = day.minTemp,
                                    dayMax = day.maxTemp,
                                    weekMin = weekMin,
                                    weekMax = weekMax,
                                    current = if (index == 0) weather!!.currentTemp else null,
                                    modifier = Modifier.weight(1f).height(5.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${day.maxTemp.toInt()}°",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.width(30.dp),
                                    textAlign = TextAlign.End,
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


/**
 * Temperature range bar. The track spans the week's overall min/max; the filled
 * segment is this day's range; the dot marks the current temperature.
 *
 * Replaces "H 35°  L 25°", which made you read numbers to notice that today is
 * the hot one.
 */
@Composable
private fun TempRangeBar(
    dayMin: Double,
    dayMax: Double,
    weekMin: Double,
    weekMax: Double,
    current: Double?,
    modifier: Modifier = Modifier,
) {
    val span = (weekMax - weekMin).takeIf { it > 0.5 } ?: 1.0
    val startFrac = ((dayMin - weekMin) / span).toFloat().coerceIn(0f, 1f)
    val endFrac = ((dayMax - weekMin) / span).toFloat().coerceIn(0f, 1f)
    val widthFrac = (endFrac - startFrac).coerceAtLeast(0.04f)
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

    BoxWithConstraints(
        modifier.clip(RoundedCornerShape(3.dp)).background(trackColor),
    ) {
        val w = maxWidth
        Box(
            Modifier
                .offset(x = w * startFrac)
                .width(w * widthFrac)
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(tempColor(dayMin), tempColor(dayMax))
                    )
                ),
        )
        if (current != null) {
            val curFrac = ((current - weekMin) / span).toFloat().coerceIn(0f, 1f)
            Box(
                Modifier
                    .offset(x = (w * curFrac) - 4.dp)
                    .align(Alignment.CenterStart)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}

/** Cold blue through to hot orange, clamped either side of a comfortable range. */
private fun tempColor(celsius: Double): Color = when {
    celsius <= 0 -> Color(0xFF6EA8DC)
    celsius <= 10 -> Color(0xFF5B9BD5)
    celsius <= 18 -> Color(0xFF5DBFA6)
    celsius <= 25 -> Color(0xFF8FBF4A)
    celsius <= 31 -> Color(0xFFE0A93C)
    else -> Color(0xFFD8703C)
}

/** Subtle card wash keyed to the current condition — cool for wet, warm for clear. */
@Composable
private fun conditionGradient(code: Int?): Brush {
    val tint = when (code) {
        null -> Color.Transparent
        0, 1 -> Color(0xFFE0A93C)                       // clear / mainly clear
        2, 3 -> Color(0xFF6E7C93)                       // partly cloudy / overcast
        45, 48 -> Color(0xFF8A8F98)                     // fog
        in 51..67, in 80..82 -> Color(0xFF5B9BD5)       // drizzle / rain
        in 71..77, 85, 86 -> Color(0xFF9FC4DC)          // snow
        in 95..99 -> Color(0xFF7C6BB5)                  // thunderstorm
        else -> Color.Transparent
    }
    return Brush.linearGradient(listOf(tint.copy(alpha = 0.16f), Color.Transparent))
}

/** "2026-06-26" -> "Fri, Jun 26". */
private fun prettyDate(iso: String, s: com.itinera.app.i18n.Strings): String {
    val parts = iso.split("-")
    if (parts.size != 3) return iso
    val y = parts[0].toIntOrNull() ?: return iso
    val m = parts[1].toIntOrNull() ?: return iso
    val d = parts[2].toIntOrNull() ?: return iso
    val months = listOf(s.jan, s.feb, s.mar, s.apr, s.may, s.jun, s.jul, s.aug, s.sep, s.oct, s.nov, s.dec)
    var mm = m; var yy = y
    if (mm < 3) { mm += 12; yy -= 1 }
    val k = yy % 100; val j = yy / 100
    val h = (d + (13 * (mm + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
    val dowList = listOf(s.satShort, s.sunShort, s.monShort, s.tueShort, s.wedShort, s.thuShort, s.friShort)
    val dow = dowList.getOrElse(h) { "" }
    val mon = months.getOrElse(m - 1) { "" }
    return "$dow, $mon $d"
}
