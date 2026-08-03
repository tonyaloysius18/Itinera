package com.itinera.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Trip
import com.itinera.app.model.TripAccent
import com.itinera.app.model.isOwnedBy
import com.itinera.app.model.label
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.PlaneLoader
import androidx.compose.ui.text.style.TextOverflow
import com.itinera.app.ui.components.TopBar
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.daysUntil
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.math.roundToInt

fun accentColor(accent: TripAccent): Color = when (accent) {
    TripAccent.BLUE -> Color(0xFF378ADD)
    TripAccent.GREEN -> Color(0xFF639922)
    TripAccent.CORAL -> Color(0xFFD85A30)
    TripAccent.PURPLE -> Color(0xFF7F77DD)
}

@Composable
fun TripsHomeScreen(
    trips: List<Trip>,
    isLoading: Boolean = false,
    currentUid: String = "",                              // to know which trips I own
    onOpenMembers: (String) -> Unit,                      // open the Members screen for a trip
    onJoinByCode: suspend (String) -> String?,            // join a trip via invite code
    onOpenTrip: (String) -> Unit,
    onCreateTrip: (String) -> Unit,
    onRenameTrip: (String, String) -> Unit,
    onPinTrip: (String) -> Unit,
    onArchiveTrip: (String) -> Unit,
    onDeleteTrip: (String) -> Unit,
    pinnedTripIds: Set<String> = emptySet(),
) {

    val s = LocalStrings.current
    var openCardId by remember { mutableStateOf<String?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTrip by remember { mutableStateOf<Trip?>(null) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var fabMenuOpen by remember { mutableStateOf(false) }

    var searchActive by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val visibleTrips = if (query.isBlank()) trips
    else trips.filter { it.title.contains(query.trim(), ignoreCase = true) }

    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val grouped = remember(visibleTrips, today) { visibleTrips.groupBy { tripPhase(it, today) } }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(query) { if (query.isNotBlank()) listState.scrollToItem(0) }

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(s.myTrips.toTitleCase(), trailing = {
                IconButton(onClick = { searchActive = !searchActive; if (!searchActive) query = "" }) {
                    Icon(if (searchActive) Icons.Filled.Close else Icons.Filled.Search, contentDescription = s.search)
                }
            })
            if (searchActive) {
                // ⬅ CHANGED — was an OutlinedTextField, visually heavier than
                // everything around it. Matches the search pill on Documents.
                TripSearchField(
                    value = query,
                    // ⬅ CHANGED — was query = it.toTitleCase(), which fought anyone
                    // typing. The filter is already ignoreCase, so it bought nothing.
                    onValueChange = { query = it },
                    onClear = { query = "" },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PlaneLoader(size = 130.dp)
                    }
                }

                trips.isEmpty() -> HomeEmptyState(
                    icon = Icons.Filled.Luggage,
                    title = s.noTripsYet,
                    subtitle = s.noTripsSubtitle,
                )

                visibleTrips.isEmpty() -> HomeEmptyState(
                    icon = Icons.Filled.Search,
                    title = s.noResults,
                    subtitle = s.noResultsSubtitle,
                )

                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                ) {
                    // ⬅ ADD — the phase was already computed by tripStartsIn and
                    // spent on a line of grey text. Sectioning the list is what
                    // puts the trip you're on at the top.
                    fun cardsFor(phase: TripPhase) {
                        val group = grouped[phase].orEmpty()
                        if (group.isEmpty()) return
                        item(key = "hdr-$phase") {
                            SectionHeader(
                                label = when (phase) {
                                    TripPhase.IN_PROGRESS -> s.inProgress
                                    TripPhase.UPCOMING -> s.upcoming
                                    TripPhase.PAST -> s.past
                                },
                                count = group.size,
                            )
                        }
                        group.forEach { trip ->
                            item(key = trip.id) {
                                SwipeableTripCard(
                                    trip = trip,
                                    phase = phase,
                                    today = today,
                                    countriesWord = s.countries,
                                    legsWord = s.legs,
                                    doneWord = s.done,
                                    isOpen = openCardId == trip.id,
                                    canShare = trip.ownerId == currentUid && currentUid.isNotBlank(),
                                    onShare = { onOpenMembers(trip.id) },
                                    onOpenChange = { open -> openCardId = if (open) trip.id else null },
                                    onClick = { onOpenTrip(trip.id) },
                                    modifier = Modifier.animateItem(),
                                    onPin = { onPinTrip(trip.id); openCardId = null },
                                    onEdit = { editingTrip = trip; openCardId = null },
                                    onArchive = { onArchiveTrip(trip.id); openCardId = null },
                                    onDelete = { pendingDeleteId = trip.id; openCardId = null },
                                    isOwner = trip.isOwnedBy(currentUid),
                                    isPinned = trip.id in pinnedTripIds,
                                )
                            }
                        }
                    }

                    cardsFor(TripPhase.IN_PROGRESS)
                    cardsFor(TripPhase.UPCOMING)
                    cardsFor(TripPhase.PAST)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd).offset(x = (-25).dp, y = 60.dp)
                .padding(end = 20.dp, bottom = 220.dp),
        ) {
            FloatingActionButton(
                onClick = { fabMenuOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            ) {
                Icon(Icons.Filled.Add, contentDescription = s.newTrip)
            }
            DropdownMenu(
                expanded = fabMenuOpen,
                onDismissRequest = { fabMenuOpen = false },
                shape = RoundedCornerShape(12.dp),
            ) {
                DropdownMenuItem(
                    text = { Text(s.createTripOption) },
                    leadingIcon = { Icon(Icons.Filled.Add, null) },
                    onClick = { fabMenuOpen = false; showAddDialog = true },
                )
                DropdownMenuItem(
                    text = { Text(s.joinTripOption) },
                    leadingIcon = { Icon(Icons.Filled.PersonAdd, null) },
                    onClick = { fabMenuOpen = false; showJoinDialog = true },
                )
            }
        }
    }
    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(s.deleteTripQ) },
            text = { Text(s.cantBeUndone) },
            confirmButton = {
                TextButton(onClick = { onDeleteTrip(pendingDeleteId!!); pendingDeleteId = null }) {
                    Text(s.delete, color = Color(0xFFE03131))
                }
            },
            dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text(s.cancel) } },
        )
    }
    if (showAddDialog) {
        TripNameDialog(
            initialName = "",
            isEdit = false,
            onConfirm = { name -> onCreateTrip(name); showAddDialog = false },
            onDismiss = { showAddDialog = false },
        )
    }

    if (editingTrip != null) {
        TripNameDialog(
            initialName = editingTrip!!.title,
            isEdit = true,
            onConfirm = { name -> onRenameTrip(editingTrip!!.id, name); editingTrip = null },
            onDismiss = { editingTrip = null },
        )
    }

    if (showJoinDialog) {
        JoinTripDialog(
            onJoinByCode = onJoinByCode,
            onDismiss = { showJoinDialog = false },
        )
    }
}


@Composable
private fun JoinTripDialog(
    onJoinByCode: suspend (String) -> String?,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()

    var suffix by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var joinedTitle by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text(s.joinTripOption) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                when {
                    joinedTitle != null -> {
                        Text(
                            "${s.joined} $joinedTitle",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    loading -> {
                        PlaneLoader(size = 72.dp)
                    }
                    else -> {
                        Text(
                            s.enterInviteCode,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = suffix,
                            onValueChange = {
                                // keep only A-Z and 2-9, max 4 chars (matches your invite alphabet)
                                suffix = it.uppercase().filter { c -> c in "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" }.take(4)
                                error = false
                            },
                            prefix = { Text("ITIN-", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                            placeholder = { Text("XXXX") },
                            singleLine = true,
                            isError = error,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        if (error) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                s.invalidCode,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (joinedTitle != null) {
                TextButton(onClick = onDismiss) { Text(s.done) }
            } else if (!loading) {
                TextButton(
                    onClick = {
                        scope.launch {
                            loading = true
                            error = false
                            val title = onJoinByCode("ITIN-$suffix")   // ⬅ reconstruct full code
                            loading = false
                            if (title != null) joinedTitle = title else error = true
                        }
                    },
                    enabled = suffix.length == 4,                       // ⬅ enable only when 4 entered
                ) { Text(s.join) }
            }
        },
        dismissButton = {
            if (joinedTitle == null && !loading) {
                TextButton(onClick = onDismiss) { Text(s.cancel) }
            }
        },
    )
}


@Composable
private fun SwipeableTripCard(
    trip: Trip,
    phase: TripPhase,     // ⬅ ADD
    today: LocalDate,     // ⬅ ADD
    countriesWord: String,
    legsWord: String,
    doneWord: String,
    isOpen: Boolean,
    canShare: Boolean,
    onShare: () -> Unit,
    isOwner: Boolean,
    onOpenChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier,
    isPinned: Boolean,
) {
    val s = LocalStrings.current
    val density = LocalDensity.current
    val actionWidth = 80.dp
    val gap = 15.dp
    val panelWidth = (if (isOwner) actionWidth * 2 else actionWidth) + gap
    val panelPx = with(density) { panelWidth.toPx() }
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val exitOffsetX = remember { Animatable(0f) }   // slides the WHOLE card (incl. icons) off on archive

    val progress = ((-offsetX.value - with(density) { gap.toPx() }) / (panelPx - with(density) { gap.toPx() })).coerceIn(0f, 1f)

    LaunchedEffect(isOpen) {
        if (!isOpen && offsetX.value != 0f) offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
    }

    // Slide the WHOLE card (card + action icons) off the left edge, then run the action.
    fun animateOutThen(action: () -> Unit) {
        scope.launch {
            val screenSlide = with(density) { (panelWidth + 600.dp).toPx() }
            exitOffsetX.animateTo(-screenSlide, tween(durationMillis = 300))
            action()
        }
    }

    Box(
        modifier
            .fillMaxWidth()
            .offset { IntOffset(exitOffsetX.value.roundToInt(), 0) },
    ) {
        // Behind: the action panel, pinned to the right edge
        Row(
            Modifier.matchParentSize().clip(CardShape),
            horizontalArrangement = Arrangement.End,
        ) {
            Column(
                Modifier
                    .width(panelWidth)
                    .fillMaxHeight()
                    .padding(start = gap)
            ) {
                if (isOwner) {
                    Row(Modifier.weight(1f)) {
                        ActionButton(Icons.Filled.PushPin, if (isPinned) s.unpin else s.pin, Color(0xFF4F7CC0), progress, Modifier.weight(1f), onPin)
                        ActionButton(Icons.Filled.Edit, s.edit, Color(0xFF5B8A4B), progress, Modifier.weight(1f), onEdit)
                    }
                    Row(Modifier.weight(1f)) {
                        ActionButton(Icons.Filled.Archive, s.archive, Color(0xFF8A7B3B), progress, Modifier.weight(1f)) { animateOutThen(onArchive) }
                        ActionButton(Icons.Filled.Delete, s.delete, Color(0xFFB23B3B), progress, Modifier.weight(1f), onDelete)
                    }
                } else {
                    ActionButton(Icons.Filled.PushPin, if (isPinned) s.unpin else s.pin, Color(0xFF4F7CC0), progress, Modifier.weight(1f), onPin)
                    ActionButton(Icons.Filled.Archive, s.archive, Color(0xFF8A7B3B), progress, Modifier.weight(1f)) { animateOutThen(onArchive) }
                }
            }
        }

        // Front: the trip card, draggable horizontally
        Box(
            Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(trip.id) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            if (!isOpen) onOpenChange(false)
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newValue = (offsetX.value + dragAmount).coerceIn(-panelPx, 0f)
                                offsetX.snapTo(newValue)
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
            TripCardContent(
                trip = trip,
                phase = phase,     // ⬅ ADD
                today = today,     // ⬅ ADD
                countriesWord = countriesWord,
                legsWord = legsWord,
                legWordSingular = s.leg,
                doneWord = doneWord,
                noDatesWord = s.noDatesYet,
                canShare = canShare,
                onShare = onShare,
                onClick = {
                    if (offsetX.value != 0f) {
                        scope.launch { offsetX.animateTo(0f, tween(250)); onOpenChange(false) }
                    } else onClick()
                },
                isPinned = isPinned,
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bg: Color,
    progress: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(52.dp)
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
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun TripCardContent(
    trip: Trip,
    countriesWord: String,
    legsWord: String,
    doneWord: String,
    legWordSingular: String,
    noDatesWord: String,
    canShare: Boolean = false,
    onShare: () -> Unit = {},
    onClick: () -> Unit,
    isPinned: Boolean,
    phase: TripPhase = TripPhase.UPCOMING,   // ⬅ ADD
    today: LocalDate? = null,                // ⬅ ADD
) {
    val accent = accentColor(trip.accent)
    val doneCount = trip.legs.count { it.completed }
    val s = LocalStrings.current

    val dates = trip.legs.map { it.date }.sorted()
    val rangeShown = if (dates.isEmpty()) noDatesWord
    else if (dates.first() == dates.last()) dates.first().label()
    else "${dates.first().label()} – ${dates.last().label()}"

    // ⬅ ADD — finished trips don't need a 120dp photo. Compact row keeps the
    // swipe actions working while giving the section a much lighter footprint.
    if (phase == TripPhase.PAST) {
        Surface(
            modifier = Modifier.fillMaxWidth().clip(CardShape).clickable(onClick = onClick),
            shape = CardShape,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(9.dp))
                        .background(accent.copy(alpha = 0.18f)),
                ) {
                    if (trip.imageUrl != null) {
                        AsyncImage(
                            model = trip.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize(),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        trip.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    )
                    Text(
                        "$rangeShown · ${trip.legs.size} ${if (trip.legs.size == 1) legWordSingular else legsWord}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = DoneGreen,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        return
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clip(CardShape).clickable(onClick = onClick),
        shape = CardShape,
        tonalElevation = 1.dp,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(if (phase == TripPhase.IN_PROGRESS) 128.dp else 110.dp),
                contentAlignment = Alignment.BottomStart,
            ) {
                if (trip.imageUrl != null) {
                    AsyncImage(
                        model = trip.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                    Box(
                        Modifier.matchParentSize().background(
                            Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.55f))
                        )
                    )
                } else {
                    Box(Modifier.matchParentSize().background(accent.copy(alpha = 0.15f)))
                }

                // ⬅ ADD — status badge. This used to be 0.6-alpha grey text under
                // the title, competing with it and saying nothing at a glance.
                if (today != null) {
                    StatusBadge(
                        label = statusLabel(trip, today, phase),
                        inProgress = phase == TripPhase.IN_PROGRESS,
                        modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                    )
                }

                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    val labelColor = if (trip.imageUrl != null) Color.White else accent
                    if (isPinned) {
                        Icon(Icons.Filled.PushPin, null, tint = labelColor, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Icon(Icons.Filled.Place, null, tint = labelColor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    val countryCount = trip.legs
                        .map { it.country }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .size
                    Text("$countryCount $countriesWord", color = labelColor, style = MaterialTheme.typography.labelMedium)
                }

                Row(
                    Modifier.align(Alignment.BottomEnd).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // ⬅ ADD — trip.travellers was never shown here. The stack says
                    // "shared trip" instantly; the lone person-plus icon didn't.
                    AvatarStack(trip.travellers)

                    if (canShare) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.40f))
                                .clickable(
                                    onClick = onShare,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.PersonAdd,
                                contentDescription = s.inviteToTrip,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp),
                            )
                        }
                    }
                }
            }

            Column(Modifier.padding(horizontal = 13.dp, vertical = 11.dp)) {
                Text(
                    trip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    rangeShown,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )

                // ⬅ CHANGED — replaces "18 Legs · 18 Done" run-on with something
                // readable at a glance.
                if (trip.legs.isNotEmpty()) {
                    Spacer(Modifier.height(9.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(doneCount.toFloat() / trip.legs.size)
                                    .fillMaxHeight()
                                    .background(if (doneCount == trip.legs.size) DoneGreen else accent),
                            )
                        }
                        Spacer(Modifier.width(9.dp))
                        Text(
                            "$doneCount / ${trip.legs.size} ${if (trip.legs.size == 1) legWordSingular else legsWord}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripNameDialog(
    initialName: String,
    isEdit: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }


    var name by remember { mutableStateOf(initialName) }
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) s.renameTrip else s.newTrip) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.toTitleCase() },
                label = { Text(s.tripName) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) { Text(if (isEdit) s.save else s.create) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
        properties = DialogProperties(usePlatformDefaultWidth = true),
    )

}

// ─────────────────────────────────────────────────────────────────────────────
// Trip phase, status, and the pieces the card and list are built from
// ─────────────────────────────────────────────────────────────────────────────

internal val DoneGreen = Color(0xFF1D9E75)

enum class TripPhase { IN_PROGRESS, UPCOMING, PAST }

/**
 * Which section a trip belongs to.
 *
 * Replaces tripStartsIn(), which returned hardcoded English ("Starts in 3
 * days", "In Progress") — the one place in the app that bypassed LocalStrings.
 * Phase is computed here; the label is built in statusLabel() where strings are
 * available. A trip with no legs has no dates, so it sits under Upcoming.
 */
internal fun tripPhase(trip: Trip, today: LocalDate): TripPhase {
    val dates = trip.legs.map { it.date }.sorted()
    val first = dates.firstOrNull() ?: return TripPhase.UPCOMING
    return when {
        dates.last() < today -> TripPhase.PAST
        first <= today -> TripPhase.IN_PROGRESS
        else -> TripPhase.UPCOMING
    }
}

@Composable
private fun statusLabel(trip: Trip, today: LocalDate, phase: TripPhase): String {
    val s = LocalStrings.current
    val dates = trip.legs.map { it.date }.sorted()
    val first = dates.firstOrNull() ?: return s.noDatesYet
    return when (phase) {
        TripPhase.PAST -> s.completed
        TripPhase.IN_PROGRESS -> s.dayXofY
            .replace("%1\$s", "${first.daysUntil(today) + 1}")
            .replace("%2\$s", "${first.daysUntil(dates.last()) + 1}")
        TripPhase.UPCOMING -> when (val d = today.daysUntil(first)) {
            0 -> s.startsToday
            1 -> s.startsTomorrow
            else -> s.startsInDays.replace("%s", "$d")
        }
    }
}

@Composable
private fun StatusBadge(label: String, inProgress: Boolean, modifier: Modifier = Modifier) {
    val bg = if (inProgress) DoneGreen else MaterialTheme.colorScheme.primary
    Surface(shape = CircleShape, color = bg, modifier = modifier) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (inProgress) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(Color.White))
                Spacer(Modifier.width(5.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = Color.White,
            )
        }
    }
}

private val AvatarColors = listOf(
    Color(0xFF85B7EB), Color(0xFFC9A0DC), Color(0xFF8FD1B6),
    Color(0xFFE8B87A), Color(0xFFE49AAE), Color(0xFF9FB8E8),
)

/** Overlapping initials, capped at three plus an overflow count. */
@Composable
private fun AvatarStack(travellers: List<com.itinera.app.model.Traveller>, max: Int = 3) {
    if (travellers.isEmpty()) return
    val shown = travellers.take(max)
    val overflow = travellers.size - shown.size

    Row(verticalAlignment = Alignment.CenterVertically) {
        shown.forEachIndexed { index, t ->
            Box(
                Modifier
                    .offset(x = if (index == 0) 0.dp else (-8 * index).dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(AvatarColors[t.colorIndex.mod(AvatarColors.size)]),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    t.firstName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.75f),
                )
            }
        }
        if (overflow > 0) {
            Box(
                Modifier
                    .offset(x = (-8 * shown.size).dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "+$overflow",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String, count: Int) {
    Row(
        Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(9.dp))
        Text(
            "$count",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(bottom = 2.dp),
        )
    }
}

@Composable
private fun TripSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    Surface(modifier = modifier, shape = RoundedCornerShape(22.dp), color = onSurface.copy(alpha = 0.06f)) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Search, null, tint = onSurface.copy(alpha = 0.45f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        s.searchTrips,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurface.copy(alpha = 0.4f),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (value.isNotEmpty()) {
                Icon(
                    Icons.Filled.Close, s.clear,
                    tint = onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp).clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onClear() },
                )
            }
        }
    }
}

@Composable
private fun HomeEmptyState(icon: ImageVector, title: String, subtitle: String) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // ⬅ CHANGED — was Text("✈️") / Text("🔎"), which don't tint with the theme
        // and render differently across platforms.
        Box(
            Modifier.size(64.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
        )
    }
}