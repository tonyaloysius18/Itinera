package com.itinera.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.itinera.app.i18n.LocalStrings
import androidx.compose.material3.Surface
import com.itinera.app.model.ChecklistItem
import com.itinera.app.data.PackingSuggestion
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.itinera.app.ui.components.TopBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChecklistScreen(
    items: List<ChecklistItem>,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
    onAdd: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    suggestions: List<PackingSuggestion> = emptyList(),
    /** e.g. "Leaves in 3 days". Blank hides the line. */
    departureNote: String = "",
) {
    val s = LocalStrings.current
    val doneCount = items.count { it.done }
    val pct = if (items.isEmpty()) 0 else (doneCount * 100 / items.size)

    var showAddDialog by remember { mutableStateOf(false) }
    var suggestionsExpanded by rememberSaveable { mutableStateOf(false) }
    var openSwipeId by remember { mutableStateOf<String?>(null) }

    // packing suggestions
    var dismissed by remember { mutableStateOf<Set<String>>(emptySet()) }
    val visibleSuggestions = suggestions.filter {
        it.text !in dismissed && items.none { i -> i.text.equals(it.text, ignoreCase = true) }
    }

    val existingGroups = items.map { it.group }.distinct()

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            topBar = { TopBar(s.beforeYouGo, onBack = onBack) },
        ) { innerPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
            // ⬅ CHANGED — was a full-width bar plus a separate count row. The ring
            // is compact enough to sit beside the count, and pairing it with the
            // departure note makes the header answer "should I be worried".
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    fraction = if (items.isEmpty()) 0f else doneCount.toFloat() / items.size,
                    percentLabel = "$pct%",
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "$doneCount / ${items.size} ${s.done}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    if (departureNote.isNotBlank()) {
                        Text(
                            text = departureNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        )
                    }
                }
            }

            // ⬅ CHANGED — was duplicated verbatim in both branches of the
            // items.isEmpty() check, and permanently outweighed the list it was
            // meant to help with. Now one collapsed row that expands on demand.
            if (visibleSuggestions.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                SuggestionsPanel(
                    suggestions = visibleSuggestions,
                    expanded = suggestionsExpanded || items.isEmpty(),
                    onToggleExpanded = { suggestionsExpanded = !suggestionsExpanded },
                    onAdd = { sug -> onAdd(sug.text, sug.group); dismissed = dismissed + sug.text },
                    onDismiss = { sug -> dismissed = dismissed + sug.text },
                )
            }

            if (items.isEmpty()) {
                if (visibleSuggestions.isEmpty()) {
                    Column(
                        // ⬅ CHANGED — was padding(top = 270.dp), which lands
                        // differently on every screen size.
                        Modifier.fillMaxWidth().padding(top = 80.dp, start = 24.dp, end = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // ⬅ CHANGED — was Text("🧳"), which doesn't tint with the theme.
                        Box(
                            Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Luggage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            s.noChecklistItems,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            s.noChecklistSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                // canonical order so sections always appear in the same sequence
                val groupOrder = listOf(
                    s.documents, s.bookings, s.packing,
                    s.transport, s.money, s.gadget, s.other
                )
                val grouped = items
                    .groupBy { it.group }
                    .toList()
                    .sortedBy { (group, _) ->
                        groupOrder.indexOf(group).let { if (it == -1) Int.MAX_VALUE else it }
                    }

                grouped.forEach { (group, groupItems) ->
                    val groupDone = groupItems.count { it.done }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 7.dp, start = 2.dp, end = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // ⬅ CHANGED — group headers were primary blue, competing
                        // with the chips and the plus icons for the same accent.
                        Text(
                            group,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "$groupDone / ${groupItems.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Column {
                            groupItems.forEachIndexed { index, item ->
                                ChecklistRow(
                                    item = item,
                                    onToggle = onToggle,
                                    onDelete = { onDelete(item.id); openSwipeId = null },
                                    isSwipeOpen = openSwipeId == item.id,
                                    onSwipeOpenChange = { open ->
                                        openSwipeId = if (open) item.id else null
                                    },
                                    deleteLabel = s.delete,
                                )
                                if (index < groupItems.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 44.dp),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }

    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd).offset(x = (-25).dp, y = 60.dp)
            .padding(end = 20.dp, bottom = 220.dp),
    ) {
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
        ) {
            Icon(Icons.Filled.Add, contentDescription = s.addItem)
        }
    }
}

    if (showAddDialog) {
        AddChecklistItemDialog(
            existingGroups = existingGroups,
            onDismiss = { showAddDialog = false },
            onAdd = { text, group ->
                onAdd(text, group)
                showAddDialog = false
            },
        )
    }
}

/** Compact ring — frees the full-width row the bar occupied. */
@Composable
private fun ProgressRing(fraction: Float, percentLabel: String, size: Dp = 48.dp) {
    val track = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val done = Color(0xFF1D9E75)
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 4.dp.toPx()
            val inset = stroke / 2
            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(width = stroke),
            )
            if (fraction > 0f) {
                drawArc(
                    color = done,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(this.size.width - stroke, this.size.height - stroke),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Text(
            percentLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Suggestions, collapsed by default.
 *
 * They're a one-time action — helpful when the list is empty, noise once it
 * isn't. Expanded automatically while there's nothing else on the screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionsPanel(
    suggestions: List<PackingSuggestion>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onAdd: (PackingSuggestion) -> Unit,
    onDismiss: (PackingSuggestion) -> Unit,
) {
    val s = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { onToggleExpanded() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(8.dp))
                // ⬅ CHANGED — "Suggested for this trip" was a hardcoded English
                // literal, twice; everything else here goes through LocalStrings.
                Text(
                    s.nSuggestions.replace("%s", "${suggestions.size}"),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.height(9.dp))

            if (expanded) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    suggestions.forEach { sug ->
                        SuggestionChip(
                            suggestion = sug,
                            onAdd = { onAdd(sug) },
                            onDismiss = { onDismiss(sug) },
                        )
                    }
                }
            } else {
                // A peek at the first few, so the row isn't a blind promise.
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    suggestions.take(3).forEach { sug ->
                        SuggestionChip(
                            suggestion = sug,
                            onAdd = { onAdd(sug) },
                            onDismiss = { onDismiss(sug) },
                            compact = true,
                        )
                    }
                }
            }
        }
    }
}

// ---- Single checklist row ----
/**
 * ⬅ CHANGED — was a trash IconButton on every row: five destructive controls one
 * tap each with no undo. Swipe matches the expenses, weather and clock cards.
 */
@Composable
private fun ChecklistRow(
    item: ChecklistItem,
    onToggle: (String) -> Unit,
    onDelete: () -> Unit,
    isSwipeOpen: Boolean,
    onSwipeOpenChange: (Boolean) -> Unit,
    deleteLabel: String,
) {
    val density = LocalDensity.current
    val panelWidth = 76.dp
    val panelPx = with(density) { panelWidth.toPx() }
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(isSwipeOpen) {
        if (!isSwipeOpen && offsetX.value != 0f) {
            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
        }
    }

    Box(Modifier.fillMaxWidth()) {
        if (offsetX.value != 0f) {
            Row(
                Modifier.matchParentSize().background(Color(0xFF7A2E2E)),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.width(panelWidth).fillMaxHeight().clickable { onDelete() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = deleteLabel,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(item.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, drag ->
                            scope.launch {
                                offsetX.snapTo((offsetX.value + drag).coerceIn(-panelPx, 0f))
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value < -panelPx / 2) {
                                    offsetX.animateTo(-panelPx, spring(stiffness = Spring.StiffnessMediumLow))
                                    onSwipeOpenChange(true)
                                } else {
                                    offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                    onSwipeOpenChange(false)
                                }
                            }
                        },
                    )
                }
                .clickable { onToggle(item.id) }
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Rounded square rather than the stock checkbox glyph — sits better
            // beside the rounded cards.
            Box(
                Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (item.done) Color(0xFF1D9E75) else Color.Transparent)
                    .border(
                        width = if (item.done) 0.dp else 1.5.dp,
                        color = if (item.done) Color.Transparent
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(6.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (item.done) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Spacer(Modifier.width(11.dp))
            Text(
                item.text,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (item.done) TextDecoration.LineThrough else null,
                color = if (item.done) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ---- Add-item dialog ----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddChecklistItemDialog(
    existingGroups: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit,
) {

    val s = LocalStrings.current

    // offer existing groups + a few sensible defaults, de-duplicated
    val groupOptions = (existingGroups + listOf(s.documents, s.bookings, s.packing, s.transport, s.money, s.gadget, s.other))
        .distinct()

    var text by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(groupOptions.firstOrNull() ?: "Other") }
    var userPicked by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val textFieldShape = RoundedCornerShape(12.dp)

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }

    // auto-suggest a group from the text until the user picks one manually
    LaunchedEffect(text) {
        if (!userPicked) group = suggestGroup(
            text,
            documents = s.documents,
            bookings = s.bookings,
            packing = s.packing,
            transport = s.transport,
            money = s.money,
            gadget = s.gadget,
            other = s.other,
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onAdd(text.trim(), group) }, enabled = text.isNotBlank()) {
                Text(s.add)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
        title = { Text(s.addItem) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { input ->
                        text = input.toTitleCase()
                    },
                    label = { Text(s.needToAdd) },
                    singleLine = true,
                    //keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                )
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = group,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(s.section) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, shape = RoundedCornerShape(12.dp)) {
                        groupOptions.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = { group = g; userPicked = true; expanded = false },
                            )
                        }
                    }
                }
            }
        },
    )
}

// keyword auto-suggest → returns one of the provided localized labels, else the "other" label
private fun suggestGroup(
    text: String,
    documents: String,
    bookings: String,
    packing: String,
    transport: String,
    money: String,
    gadget: String,
    other: String,
): String {
    val t = text.lowercase()
    fun has(vararg w: String) = w.any { t.contains(it) }
    return when {
        // Documents — passport / visa / insurance / id / licence
        has("passport", "visa", "insurance", "document", "licence", "license", "id",
            "passeport", "assurance", "papiers", "pièce",                          // FR
            "pasaporte", "seguro", "documento", "carnet",                          // ES
            "reisepass", "ausweis", "versicherung", "dokument",                    // DE
            "passaporto", "assicurazione", "documento", "patente",                 // IT
            "passaporte", "seguro", "documento", "carteira"                        // PT
        ) -> documents

        // Bookings — hotel / flight / ticket / reservation
        has("hotel", "hostel", "book", "reserve", "airbnb", "flight", "ticket", "reservation",
            "hôtel", "vol", "billet", "réservation", "auberge",                    // FR
            "vuelo", "billete", "reserva", "alojamiento",                          // ES
            "flug", "ticket", "buchung", "unterkunft", "reservierung",             // DE
            "volo", "biglietto", "prenotazione", "albergo",                        // IT
            "voo", "bilhete", "reserva", "hospedagem"                              // PT
        ) -> bookings

        // Packing — clothes / bag / shoes / suitcase
        has("pack", "clothes", "backpack", "sunscreen", "shoes", "suitcase", "handbag", "bag",
            "valise", "vêtements", "chaussures", "sac", "bagage",                  // FR
            "maleta", "ropa", "zapatos", "bolsa", "equipaje",                      // ES
            "koffer", "kleidung", "schuhe", "tasche", "gepäck",                    // DE
            "valigia", "vestiti", "scarpe", "borsa", "bagaglio",                   // IT
            "mala", "roupas", "sapatos", "bolsa", "bagagem"                        // PT
        ) -> packing

        // Transport — bus / train / taxi / car
        has("bus", "train", "transfer", "taxi", "metro", "car", "shuttle", "subway",
            "voiture", "métro", "navette", "transfert",                            // FR
            "tren", "coche", "autobús", "metro", "traslado",                       // ES
            "zug", "auto", "u-bahn", "transfer",                                   // DE
            "treno", "auto", "metropolitana", "navetta",                           // IT
            "trem", "carro", "ônibus", "metrô", "transporte"                       // PT
        ) -> transport

        // Money — cash / card / currency / atm
        has("cash", "money", "card", "currency", "atm", "exchange", "credit", "debit",
            "argent", "carte", "monnaie", "espèces", "change",                     // FR
            "dinero", "tarjeta", "efectivo", "cambio", "moneda",                   // ES
            "geld", "karte", "bargeld", "währung", "wechsel",                      // DE
            "soldi", "carta", "contanti", "valuta", "cambio",                      // IT
            "dinheiro", "cartão", "moeda", "câmbio"                                // PT
        ) -> money

        // Gadget — electronics
        has("drone", "camera", "hard disk", "laptop", "charger", "trimmer", "tripod",
            "mobile", "phone", "tablet", "computer", "printer", "scanner", "projector",
            "monitor", "headphones", "speaker", "microphone", "mouse", "keyboard",
            "appareil", "caméra", "chargeur", "téléphone", "ordinateur", "casque", // FR
            "cámara", "cargador", "teléfono", "ordenador", "auriculares",          // ES
            "kamera", "ladegerät", "telefon", "computer", "kopfhörer",             // DE
            "fotocamera", "caricabatterie", "telefono", "computer", "cuffie",      // IT
            "câmera", "carregador", "telefone", "computador", "fones"              // PT
        ) -> gadget

        else -> other
    }
}


/** @param compact drops the reason line, for the collapsed peek row. */
@Composable
private fun SuggestionChip(
    suggestion: PackingSuggestion,
    onAdd: () -> Unit,
    onDismiss: () -> Unit,
    compact: Boolean = false,
) {
    val s = LocalStrings.current
    Surface(
        shape = RoundedCornerShape(if (compact) 20.dp else 12.dp),
        color = if (compact) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
        border = if (compact) BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        ) else null,
        modifier = Modifier.clickable { onAdd() },
    ) {
        Row(
            Modifier.padding(
                start = if (compact) 10.dp else 12.dp,
                end = if (compact) 11.dp else 8.dp,
                top = if (compact) 5.dp else 7.dp,
                bottom = if (compact) 5.dp else 7.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (compact) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = s.addItem,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    suggestion.text,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            } else {
                Column {
                    Text(
                        suggestion.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        suggestion.reason,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Filled.Add,
                    // ⬅ CHANGED — was a hardcoded "Add" content description.
                    contentDescription = s.addItem,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}