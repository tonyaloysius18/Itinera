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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
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
import com.itinera.app.ui.components.EmptyState
import com.itinera.app.ui.components.PlaneLoader
import com.itinera.app.ui.components.TopBar
import kotlinx.coroutines.launch
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
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it.toTitleCase() },
                    placeholder = { Text(s.searchTrips) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                )
            }
            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PlaneLoader(size = 130.dp)
                    }
                }
                trips.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Luggage,
                    title = s.noTripsYet,
                    subtitle = s.noTripsSubtitle,
                    modifier = Modifier.weight(1f),
                )
                visibleTrips.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Search,
                    title = s.noResults,
                    subtitle = s.noResultsSubtitle,
                    modifier = Modifier.weight(1f),
                )
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    items(visibleTrips, key = { it.id }) { trip ->
                        SwipeableTripCard(
                            trip = trip,
                            countriesWord = s.countries,
                            legsWord = s.legs,
                            doneWord = s.done,
                            isOpen = openCardId == trip.id,
                            canShare = trip.ownerId == currentUid && currentUid.isNotBlank(),  // owner only
                            onShare = { onOpenMembers(trip.id) },                               // → Members screen
                            onOpenChange = { open -> openCardId = if (open) trip.id else null },
                            onClick = { onOpenTrip(trip.id) },
                            modifier = Modifier.animateItem(),
                            onPin = { onPinTrip(trip.id); openCardId = null },
                            onEdit = { editingTrip = trip; openCardId = null },
                            onArchive = { onArchiveTrip(trip.id); openCardId = null },
                            onDelete = { pendingDeleteId = trip.id; openCardId = null },
                            isOwner = trip.isOwnedBy(currentUid),
                        )
                    }
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
                            "${s.joined} ${joinedTitle}",
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

    ) {
    val s = LocalStrings.current
    val density = LocalDensity.current
    val actionWidth = 80.dp
    val gap = 15.dp
    val panelWidth = (if (isOwner) actionWidth * 2 else actionWidth) + gap
    val panelPx = with(density) { panelWidth.toPx() }
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    val progress = ((-offsetX.value - with(density) { gap.toPx() }) / (panelPx - with(density) { gap.toPx() })).coerceIn(0f, 1f)

    LaunchedEffect(isOpen) {
        if (!isOpen && offsetX.value != 0f) offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
    }

    Box(Modifier.fillMaxWidth()) {
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
                        ActionButton(Icons.Filled.PushPin, if (trip.pinned) s.unpin else s.pin, Color(0xFF4F7CC0), progress, Modifier.weight(1f), onPin)
                        ActionButton(Icons.Filled.Edit, s.edit, Color(0xFF5B8A4B), progress, Modifier.weight(1f), onEdit)
                    }
                    Row(Modifier.weight(1f)) {
                        ActionButton(Icons.Filled.Archive, s.archive, Color(0xFF8A7B3B), progress, Modifier.weight(1f), onArchive)
                        ActionButton(Icons.Filled.Delete, s.delete, Color(0xFFB23B3B), progress, Modifier.weight(1f), onDelete)
                    }
                } else {
                    ActionButton(Icons.Filled.PushPin, if (trip.pinned) s.unpin else s.pin, Color(0xFF4F7CC0), progress, Modifier.weight(1f), onPin)
                    ActionButton(Icons.Filled.Archive, s.archive, Color(0xFF8A7B3B), progress, Modifier.weight(1f), onArchive)
                }
            }
        }

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
                }
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
) {
    val accent = accentColor(trip.accent)
    val doneCount = trip.legs.count { it.completed }

    val s = LocalStrings.current

    Surface(
        modifier = Modifier.fillMaxWidth().clip(CardShape).clickable(onClick = onClick),
        shape = CardShape,
        tonalElevation = 1.dp,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(120.dp),
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

                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    val labelColor = if (trip.imageUrl != null) Color.White else accent
                    if (trip.pinned) {
                        Icon(Icons.Filled.PushPin, null, tint = labelColor, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.6.dp))
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

                // Share / members icon (owner only), bottom-right of the image
                if (canShare) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .size(34.dp)
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
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            Column(Modifier.padding(horizontal = 13.dp, vertical = 11.dp)) {
                Text(trip.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(3.dp))
                val sub = buildString {
                    val rangeShown = if (trip.legs.isEmpty()) {
                        noDatesWord
                    } else {
                        val dates = trip.legs.map { it.date }.sorted()
                        val first = dates.first()
                        val last = dates.last()
                        if (first == last) first.label() else "${first.label()} – ${last.label()}"
                    }

                    val legCount = trip.legs.size
                    val legWord = if (legCount == 1) legWordSingular else legsWord

                    append("$rangeShown · ${trip.legs.size} $legWord")
                    if (doneCount > 0) append(" · $doneCount $doneWord")
                }
                Text(sub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
    val s = com.itinera.app.i18n.LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) s.renameTrip else s.newTrip) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.split(" ")
                    .joinToString(" ") { word ->
                        word.replaceFirstChar { c ->
                            if (c.isLowerCase()) c.titlecase() else c.toString()
                        }
                    } },
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