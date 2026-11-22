package com.itinera.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.zIndex
import com.itinera.app.model.DayMove
import com.itinera.app.model.dayOrderKey
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.itinera.app.data.countriesCovered
import com.itinera.app.data.distanceTravelledKm
import com.itinera.app.data.extractAllBarcodes
import com.itinera.app.data.primaryCountry
import com.itinera.app.data.rememberFileSharer
import com.itinera.app.data.toPngBytes
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.openInMaps
import com.itinera.app.model.Activity
import com.itinera.app.model.DocItem
import com.itinera.app.model.Leg
import com.itinera.app.model.Traveller
import com.itinera.app.model.Trip
import com.itinera.app.model.isOwnedBy
import com.itinera.app.model.canSeeDocument
import com.itinera.app.model.assignedTravellerIds
import com.itinera.app.model.WalletTicket
import com.itinera.app.model.label
import com.itinera.app.parseHourMinute
import com.itinera.app.resources.Res
import com.itinera.app.resources.arizonia_regular
import com.itinera.app.resources.caudex_bold
import com.itinera.app.resources.heart_fill
import com.itinera.app.resources.pb_envelope
import com.itinera.app.resources.pb_frame_bottom
import com.itinera.app.resources.pb_frame_top
import com.itinera.app.resources.pb_map
import com.itinera.app.resources.pb_mask_bottom
import com.itinera.app.resources.pb_mask_top
import com.itinera.app.resources.pb_plane
import com.itinera.app.resources.pb_stamp
import com.itinera.app.resources.pb_title
import com.itinera.app.resources.postcard_halftone
import com.itinera.app.resources.postcard_heart_frame
import com.itinera.app.resources.postcard_map
import com.itinera.app.resources.postcard_paper
import com.itinera.app.resources.postcard_plane
import com.itinera.app.resources.postcard_rect_frame
import com.itinera.app.resources.postcard_title
import com.itinera.app.resources.rect_fill
import com.itinera.app.ui.components.ImageCropScreen
import com.itinera.app.ui.components.PostcardBack
import com.itinera.app.ui.components.PostcardFront
import com.itinera.app.ui.components.Progress
import com.itinera.app.ui.components.SwipeRevealRow
import com.itinera.app.ui.components.TicketWalletDialog
import com.itinera.app.ui.components.TopBar
import com.itinera.app.ui.components.rememberPostcardExporter
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.coroutines.launch
import kotlinx.datetime.daysUntil
import org.jetbrains.compose.resources.Font
import kotlin.math.roundToInt
import com.itinera.app.ui.theme.itinera
import com.itinera.app.ui.theme.OverlayControlScrim


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripDetailScreen(
    trip: Trip,
    activities: List<Activity>,
    travellers: List<Traveller> = emptyList(),
    currentUid: String = "",
    expensesTotal: Double = 0.0,
    documents: List<DocItem> = emptyList(),
    onBack: () -> Unit,
    onTravellers: () -> Unit,
    onUploadPostcardPhoto: (suspend (slot: String, bytes: ByteArray) -> String)? = null,
    onRemovePostcardPhoto: ((slot: String) -> Unit)? = null,
    onLoadImageBytes: (suspend (url: String) -> ByteArray?)? = null,
    onDocuments: () -> Unit,
    onOpenDoc: (String) -> Unit = {},
    onAskNera: () -> Unit = {},
    onShare: () -> Unit = {},
    onAddLeg: () -> Unit,
    onAddPlace: () -> Unit,
    onChecklist: () -> Unit,
    onToggleLeg: (String) -> Unit,
    onEditLeg: (String) -> Unit,
    onEditActivity: (String) -> Unit,
    onDeleteLeg: (String) -> Unit,
    onToggleActivity: (String) -> Unit,
    onDeleteActivity: (String) -> Unit,
    // A leg/place dragged on the page: new day + order key per moved leg and place.
    onMoveEntries: (legMoves: Map<String, DayMove>, activityMoves: Map<String, DayMove>) -> Unit = { _, _ -> },
    canEdit: Boolean = true,
    //onMembers: () -> Unit,
    onMap: () -> Unit,
) {
    val s = LocalStrings.current
    val done = trip.legs.count { it.completed }

    // ── Postcard photo URLs (persisted on the trip via Cloudinary) ──
    var heartUrl      by remember { mutableStateOf(trip.frontHeartUrl) }
    var rectUrl       by remember { mutableStateOf(trip.frontRectUrl) }
    var backTopUrl    by remember { mutableStateOf(trip.backTopUrl) }
    var backBottomUrl by remember { mutableStateOf(trip.backBottomUrl) }

    // keep local slot state in sync with the trip as Firestore delivers/updates it
    LaunchedEffect(trip.frontHeartUrl) { heartUrl      = trip.frontHeartUrl }
    LaunchedEffect(trip.frontRectUrl)  { rectUrl       = trip.frontRectUrl }
    LaunchedEffect(trip.backTopUrl)    { backTopUrl    = trip.backTopUrl }
    LaunchedEffect(trip.backBottomUrl) { backBottomUrl = trip.backBottomUrl }

    // ── Crop / pick pipeline state ──
    var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }
    var pendingSlot  by remember { mutableStateOf<String?>(null) }
    var pickTarget   by remember { mutableStateOf<String?>(null) }   // slot the gallery picker fills
    var isPickingImage by remember { mutableStateOf(false) }
    var activeSlotForSheet by remember { mutableStateOf<String?>(null) } // if non-null, show the sheet for this slot

    val scope = rememberCoroutineScope()
    val picker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = {
            it.firstOrNull()?.let { b -> pendingBytes = b; pendingSlot = pickTarget }
            isPickingImage = false
        },
    )



    // hoisted so it survives the Dialog leaving/re-entering while the picker is up
    val pagerState = rememberPagerState(pageCount = { 2 })


    var showAddChooser by remember { mutableStateOf(false) }
    var openRowId by remember { mutableStateOf<String?>(null) }   // the leg/place row swiped open, if any
    var showPostcard by remember { mutableStateOf(false) }
    val bodyScroll = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }


    // ── ticket wallet viewer ──
    var walletTickets by remember { mutableStateOf<List<WalletTicket>>(emptyList()) }
    var walletLeg by remember { mutableStateOf<Leg?>(null) }
    var walletAct by remember { mutableStateOf<Activity?>(null) }
    var barcodeLoading by remember { mutableStateOf(false) }
    val legTicketCache = remember { mutableStateMapOf<String, List<WalletTicket>>() }

    // Places (hostels, attractions) only offer "View ticket" when an attached file really
    // holds a QR/barcode. A booking confirmation without one would otherwise open as a
    // whole PDF, which isn't a ticket. Checked once per file in the background.
    val placeDocHasCode = remember { mutableStateMapOf<String, Boolean>() }
    val placeDocs = documents.filter { it.activityId.isNotBlank() }
    LaunchedEffect(placeDocs.map { it.id to it.fileUrl }) {
        placeDocs.forEach { d ->
            val key = d.id + "|" + d.fileUrl
            if (key in placeDocHasCode) return@forEach
            val bytes = runCatching { onLoadImageBytes?.invoke(d.fileUrl) }.getOrNull()
            placeDocHasCode[key] = bytes != null &&
                runCatching { extractAllBarcodes(bytes, d.mimeType) }.getOrNull().orEmpty().isNotEmpty()
        }
    }
    val isOwner = trip.isOwnedBy(currentUid)
    val myTravellerId = remember(currentUid, travellers, trip.ownerId) {
        travellers.firstOrNull { currentUid.isNotBlank() && it.userId == currentUid }?.id
            ?: travellers.firstOrNull { currentUid == trip.ownerId && it.isOwner }?.id
            ?: ""
    }

    /**
     * Pulls every barcode out of [docs] and opens the wallet via [show]; falls back to
     * opening the first file when none has a code. [routeTime] gives a doc's own
     * route/time override ("" to "" = use the header's).
     */
    fun openTickets(
        ownerId: String,
        docs: List<DocItem>,
        routeTime: (DocItem) -> Pair<String, String>,
        show: (List<WalletTicket>) -> Unit,
        // Travellers an "Anyone" ticket belongs to (a leg's passengers). Empty for places,
        // which already treat unassigned tickets as the group's.
        everyoneIds: List<String> = emptyList(),
    ) {
        if (docs.isEmpty()) return
        scope.launch {
            barcodeLoading = true
            try {
                val cacheKey = buildString {
                    append(ownerId).append('#').append(everyoneIds.joinToString(","))
                    docs.forEach { append('|').append(it.id).append(':').append(it.assignedTravellerIds.joinToString(",")).append(':').append(it.fileUrl) }
                }
                val tickets = legTicketCache.getOrPut(cacheKey) {
                    docs.flatMap { d ->
                        val bytes = onLoadImageBytes?.invoke(d.fileUrl)
                        val codes = bytes?.let { extractAllBarcodes(it, d.mimeType) }.orEmpty()
                        val (route, time) = routeTime(d)

                        // A document with several codes cannot safely use one document-level
                        // traveller assignment for every code. Keep those codes group-only until
                        // they are split into individually assigned documents.
                        val assignmentAmbiguous = codes.size > 1
                        // One code can be for several travellers (a single QR for a whole booking).
                        val ticketTravellerIds = if (assignmentAmbiguous) emptyList() else d.assignedTravellerIds
                        // "Anyone" on a leg = this ticket is every passenger's pass.
                        val sharedWithAll = !assignmentAmbiguous && ticketTravellerIds.isEmpty() && everyoneIds.isNotEmpty()
                        val holders = (if (sharedWithAll) everyoneIds else ticketTravellerIds)
                            .mapNotNull { id -> travellers.firstOrNull { it.id == id } }
                        val travName = holders.joinToString(" + ") { "${it.firstName} ${it.surname}".trim() }
                        val travNames = holders.map { t ->
                            val full = "${t.firstName} ${t.surname}".trim()
                            t.firstName.trim().ifBlank { full } to full
                        }

                        codes.map {
                            WalletTicket(
                                extraction = it,
                                docId = d.id,
                                docTitle = d.title,
                                routeOverride = route,
                                timeOverride = time,
                                travellerIds = ticketTravellerIds,
                                travellerName = travName,
                                travellerNames = travNames,
                                assignmentAmbiguous = assignmentAmbiguous,
                                sharedWithAll = sharedWithAll,
                            )
                        }
                    }
                }
                if (tickets.isNotEmpty()) {
                    show(tickets)
                } else {
                    onOpenDoc(docs.first().id)   // no codes anywhere → open the file
                }
            } finally {
                barcodeLoading = false
            }
        }
    }

    fun openLegTickets(leg: Leg, legDocs: List<DocItem>) =
        openTickets(
            ownerId = leg.id,
            docs = legDocs,
            routeTime = { d ->
                // derive this doc's segment route/times if it's bound to one
                val cities = listOf(leg.fromCity) + leg.stops.map { it.city } + listOf(leg.toCity)
                val depTimes = listOf(leg.timeLabel) + leg.stops.map { it.departureTime }
                val arrTimes = leg.stops.map { it.arrivalTime } + listOf(leg.endTimeLabel)
                val si = d.segmentIndex
                if (si in 0..leg.stops.size) {
                    "${cities[si]} → ${cities[si + 1]}" to
                            listOf(depTimes[si], arrTimes[si]).filter { it.isNotBlank() }.joinToString(" - ")
                } else "" to ""
            },
            show = { walletTickets = it; walletAct = null; walletLeg = leg },
            // The leg's passengers; a leg that doesn't list any is for everyone on the trip.
            everyoneIds = leg.travellerIds.ifEmpty { travellers.map { it.id } },
        )

    fun openActivityTickets(act: Activity, actDocs: List<DocItem>) =
        openTickets(
            ownerId = act.id,
            docs = actDocs,
            routeTime = { "" to "" },
            show = { walletTickets = it; walletLeg = null; walletAct = act },
        )

    // Postcard unlocks when every LEG is travelled (places/activities don't gate it).
    val allComplete = trip.legs.isNotEmpty() && trip.legs.all { it.completed }

    var wasComplete by remember { mutableStateOf(allComplete) }
    LaunchedEffect(allComplete) {
        if (allComplete && !wasComplete) {
            showPostcard = true
            // bring the newly-revealed souvenir into view, above the fixed buttons
            bodyScroll.animateScrollTo(bodyScroll.maxValue)
        }
        wasComplete = allComplete
    }

    val allDates = (trip.legs.map { it.date } + activities.map { it.date })
        .distinct()
        .sorted()

    // NOTE — legsByDate / actsByDate were removed: entriesByDate below supersedes
    // them, and they were re-sorting every leg and activity on each recomposition.

    // Merge legs + activities per day, ordered by clock time, so a 10:00 place shows
    // between a 09:00 leg and a 12:45 leg instead of after all legs.
    val entriesByDate: Map<kotlinx.datetime.LocalDate, List<DayEntry>> = allDatesRaw@ run {
        val dates = (trip.legs.map { it.date } + activities.map { it.date }).distinct()
        dates.associateWith { d ->
            val legEntries = trip.legs.filter { it.date == d }.map { DayEntry.LegEntry(it) as DayEntry }
            val actEntries = activities.filter { it.date == d }.map { DayEntry.ActEntry(it) as DayEntry }
            // Clock time, unless an item was dragged into place (see dayOrderKey).
            (legEntries + actEntries).sortedBy { it.orderKey }
        }
    }

    // ── drag to reorder (long-press a leg/place, drop it anywhere in any day) ──
    var dragKey by remember { mutableStateOf<String?>(null) }
    var dragFingerY by remember { mutableStateOf(0f) }        // finger, in root coordinates
    var dragStartFingerY by remember { mutableStateOf(0f) }
    var dragGrabOffset by remember { mutableStateOf(0f) }       // finger's offset inside the row
    var dragHeightPx by remember { mutableStateOf(0f) }         // the dragged row's height
    var snapGaps by remember { mutableStateOf(true) }           // no gap animation on pick-up/drop
    // The dragged row's slot top and the open gap's top, as state so the floating card is
    // re-placed every frame the rows shift (not only when the finger moves).
    var dragSlotTop by remember { mutableFloatStateOf(0f) }
    var gapTop by remember { mutableFloatStateOf(Float.NaN) }
    // After release the card glides into the gap; the drop spot is frozen meanwhile.
    var settleTarget by remember { mutableStateOf<DropTarget?>(null) }
    // Pick-up/put-down: 0 = resting row, 1 = lifted card (scale + shadow).
    val lift by animateFloatAsState(
        targetValue = if (dragKey != null && settleTarget == null) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "dragLift",
    )
    var dragEndedAt by remember { mutableStateOf<TimeSource.Monotonic.ValueTimeMark?>(null) }
    var viewport by remember { mutableStateOf(0f to 0f) }
    val rowBounds = remember { mutableMapOf<String, Pair<Float, Float>>() }
    val dayBounds = remember { mutableMapOf<kotlinx.datetime.LocalDate, Pair<Float, Float>>() }
    val haptics = LocalHapticFeedback.current
    val autoScrollEdge = with(LocalDensity.current) { 72.dp.toPx() }
    val dragHeightDp = with(LocalDensity.current) { dragHeightPx.toDp() }
    // The gap opens instantly where the row was picked up, then animates as it moves.
    LaunchedEffect(dragKey) { snapGaps = true; if (dragKey != null) { delay(60); snapGaps = false } }
    fun justDragged() = dragEndedAt?.elapsedNow()?.let { it < 400.milliseconds } == true

    fun dropTargetFor(y: Float): DropTarget? {
        if (allDates.isEmpty()) return null
        // The day under the finger: the first one that ends below it (a gap or a day's
        // header counts as the top of the next day).
        val date = allDates.firstOrNull { d -> dayBounds[d]?.let { y <= it.second } == true } ?: allDates.last()
        val others = entriesByDate[date].orEmpty().filter { it.key != dragKey }
        return DropTarget(date, others.count { e -> rowBounds[e.key]?.let { (t, b) -> (t + b) / 2 < y } == true })
    }

    fun applyDrop(target: DropTarget) {
        val dragged = entriesByDate.values.flatten().firstOrNull { it.key == dragKey } ?: return
        val fromDate = entriesByDate.entries.first { (_, l) -> dragged in l }.key
        if (fromDate == target.date &&
            entriesByDate[fromDate].orEmpty().indexOf(dragged) == target.index) return   // dropped where it was
        val day = entriesByDate[target.date].orEmpty().filter { it != dragged }.toMutableList()
        day.add(target.index, dragged)
        // The moved item takes a key between its new neighbours; neighbours keep theirs unless
        // they tie (e.g. several untimed items), in which case the fewest get nudged.
        val prev = day.getOrNull(target.index - 1)?.orderKey
        val next = day.getOrNull(target.index + 1)?.orderKey
        val keys = day.map { it.orderKey }.toMutableList()
        keys[target.index] = when {
            prev != null && next != null -> (prev + next) / 2
            prev != null -> prev + 1
            next != null -> next - 1
            else -> dragged.orderKey
        }
        val newKeys = mutableMapOf(dragged.key to keys[target.index])
        var last = Double.NEGATIVE_INFINITY
        keys.forEachIndexed { i, k ->
            val v = if (k > last) k else last + 1
            if (v != day[i].orderKey || day[i] == dragged) newKeys[day[i].key] = v
            last = v
        }
        val legMoves = mutableMapOf<String, DayMove>()
        val actMoves = mutableMapOf<String, DayMove>()
        day.filter { it.key in newKeys }.forEach { e ->
            val move = DayMove(target.date, newKeys.getValue(e.key))
            when (e) {
                is DayEntry.LegEntry -> legMoves[e.leg.id] = move
                is DayEntry.ActEntry -> actMoves[e.act.id] = move
            }
        }
        onMoveEntries(legMoves, actMoves)
    }

    val dropTarget = settleTarget ?: if (dragKey != null) dropTargetFor(dragFingerY) else null
    // A light tick each time the drop spot moves to another slot.
    LaunchedEffect(dropTarget) {
        if (dragKey != null && settleTarget == null && !snapGaps) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    // Each row's long-press handler is set up once, so it must reach the drop logic through
    // this: calling applyDrop directly would use the order from when the row first appeared,
    // and every drag after the first would be worked out against stale positions.
    val dropLatest by rememberUpdatedState { t: DropTarget -> applyDrop(t) }
    val targetLatest by rememberUpdatedState { y: Float -> dropTargetFor(y) }

    // Release: glide the card into the open gap, then save the new order and let go.
    fun endDrag() {
        val target = targetLatest(dragFingerY) ?: run { dragKey = null; return }
        settleTarget = target
        scope.launch {
            val goal = gapTop.takeIf { !it.isNaN() }?.plus(dragGrabOffset)
            if (goal != null) {
                Animatable(dragFingerY).animateTo(goal, tween(200, easing = FastOutSlowInEasing)) { dragFingerY = value }
            }
            dropLatest(target)
            dragKey = null; settleTarget = null
            dragEndedAt = TimeSource.Monotonic.markNow()
        }
    }
    val endDragLatest by rememberUpdatedState { endDrag() }

    // Scroll while the finger is held near the top or bottom edge of the itinerary.
    LaunchedEffect(dragKey) {
        while (dragKey != null) {
            if (settleTarget != null) { delay(16); continue }
            val (top, bottom) = viewport
            val speed = when {
                dragFingerY < top + autoScrollEdge -> -(top + autoScrollEdge - dragFingerY) / autoScrollEdge * 22f
                dragFingerY > bottom - autoScrollEdge -> (dragFingerY - (bottom - autoScrollEdge)) / autoScrollEdge * 22f
                else -> 0f
            }
            if (speed != 0f) bodyScroll.scrollBy(speed)
            delay(16)
        }
    }

    val daysCount = if (allDates.isEmpty()) 0
    else allDates.first().daysUntil(allDates.last()) + 1
    val currencySymbol = when (trip.currencyCode.uppercase()) {
        "EUR" -> "€"
        "USD" -> "$"
        "GBP" -> "£"
        "INR" -> "₹"
        "JPY" -> "¥"
        "CHF" -> "CHF"
        "SEK", "NOK", "DKK" -> "kr"
        else -> trip.currencyCode      // unknown codes stay as-is
    }
    val expensesLabel = "${expensesTotal.roundToInt()} $currencySymbol"

    // dominant destination country from geocoded legs; title-derived guess as fallback
    val postcardCountry = trip.primaryCountry().ifBlank { trip.title.trim().substringBefore(" ") }

    val exporter = rememberPostcardExporter(
        country = postcardCountry,
        dateRange = if (allDates.isNotEmpty())
            "${allDates.first().label()} – ${allDates.last().label()}" else "",
        countriesCount = trip.countriesCovered(),
        distanceKm = trip.distanceTravelledKm(),
        daysCount = daysCount,
        expensesLabel = expensesLabel,
        travellers = travellers.map { it.firstName.substringBefore(" ") },
        heartUrl = heartUrl, rectUrl = rectUrl,
        backTopUrl = backTopUrl, backBottomUrl = backBottomUrl,
        loadBytes = { url -> onLoadImageBytes?.invoke(url) },
    )

    val fileSharer = rememberFileSharer()

    var exporting by remember { mutableStateOf(false) }

    val displayFont = FontFamily(Font(Res.font.arizonia_regular))
    val souvenirFont = FontFamily(Font(Res.font.caudex_bold))
    val nextLegId = trip.legs
        .sortedWith(compareBy({ it.date }, { parseHourMinute(it.timeLabel).first }, { parseHourMinute(it.timeLabel).second }))
        .firstOrNull { !it.completed }?.id

    // ⬅ CHANGED — was a guessed 160dp/290dp constant, which drifted out of sync
    // with the floating bar's real size (safe-area inset, button height) and let
    // the last day/leg end up stuck underneath it, unreachable by scrolling.
    // Measuring the bar's actual rendered height and using that as the scroll
    // content's bottom padding keeps them in sync no matter the device.
    val density = LocalDensity.current
    var floatingBarHeight by remember { mutableStateOf(0.dp) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                trip.title,
                onBack = onBack,
                trailing = {
                    Row {
                        IconButton(onClick = onAskNera) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = s.planWithNera, tint = MaterialTheme.colorScheme.primary)
                        }
                        if (trip.isOwnedBy(currentUid)) {
                            IconButton(onClick = onShare) {
                                Icon(Icons.Filled.Share, contentDescription = s.shareItinerary, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        IconButton(onClick = onTravellers) {
                            Icon(Icons.Filled.People, contentDescription = s.travellers, tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDocuments) {
                            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = s.documents, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
            )
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {

                val rangeLabel = if (allDates.isNotEmpty())
                    "${allDates.first().label()} – ${allDates.last().label()}" else ""
                val countries = trip.countriesCovered()
                val subtitle = listOfNotNull(
                    rangeLabel.takeIf { it.isNotBlank() },
                    if (countries > 0) "$countries ${if (countries == 1) s.country else s.countries}" else null,
                ).joinToString(" · ")
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TripStatCell("$daysCount", s.days, Modifier.weight(1f))
                    TripStatCell("${trip.legs.size}", s.legs, Modifier.weight(1f))
                    TripStatCell("${trip.distanceTravelledKm()}", "km", Modifier.weight(1f))
                }

                Spacer(Modifier.height(14.dp))

                Column(Modifier.fillMaxWidth()) {
                    val pct = if (trip.legs.isEmpty()) 0
                    else (done * 100f / trip.legs.size).roundToInt()
                    Row(Modifier.fillMaxWidth()) {
                        // ⬅ CHANGED — "18 Legs Travelled" never said out of how many.
                        Text(
                            "$done / ${trip.legs.size} ${s.legsTravelled}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "$pct%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    // ⬅ CHANGED — the Map pill now sits in the same row as the bar
                    // itself (not the label row above it too), so CenterVertically
                    // lines its middle up with the bar's middle instead of the
                    // taller label+bar block.
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Progress(
                            fraction = if (trip.legs.isEmpty()) 0f else done.toFloat() / trip.legs.size,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(
                                0.5.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            ),
                            modifier = Modifier.clickable { onMap() },
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.Map,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(s.map, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                Column(
                    Modifier
                        .fillMaxSize()
                        // ⬅ CHANGED — was always scrollable, even with nothing to
                        // scroll to; the empty state doesn't need it.
                        .onGloballyPositioned { c -> val b = c.boundsInRoot(); viewport = b.top to b.bottom }
                        .then(if (allDates.isNotEmpty()) Modifier.verticalScroll(bodyScroll) else Modifier)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = if (allDates.isNotEmpty()) floatingBarHeight + 16.dp else 0.dp)
                ) {
                    if (allDates.isEmpty()) {
                        // ⬅ CHANGED — was a 300.dp top-padding guess to fake centering
                        // inside a scrollable column; now that the column isn't
                        // scrollable, it can just center for real.
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text("🗺", style = MaterialTheme.typography.displayMedium)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                s.noLegsYet,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                s.noLegsSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp),
                            )
                        }
                    } else {
                        allDates.forEachIndexed { index, date ->
                            val dayNumber = index + 1
                            val entries = entriesByDate[date].orEmpty()

                            DayHeader(dayNumber, date.label())

                            val dayOthers = entries.filter { it.key != dragKey }
                            Box(
                                Modifier.fillMaxWidth()
                                    // The dragged row draws above the days after its own.
                                    .zIndex(if (entries.any { it.key == dragKey }) 1f else 0f)
                                    .onGloballyPositioned { c ->
                                        val b = c.boundsInRoot(); dayBounds[date] = b.top to b.bottom
                                    }
                            ) {
                                // ⬅ ADD — timeline spine. Drawn before the entries so the
                                // markers paint over it; matchParentSize keeps it out of
                                // the Box's size calculation.
                                if (entries.size > 1) {
                                    val spineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                    Canvas(Modifier.matchParentSize()) {
                                        val x = 12.dp.toPx()
                                        val inset = 30.dp.toPx()
                                        drawLine(
                                            color = spineColor,
                                            start = Offset(x, inset),
                                            end = Offset(x, size.height - inset),
                                            strokeWidth = 1.5.dp.toPx(),
                                        )
                                    }
                                }

                                Column(Modifier.fillMaxWidth()) {
                                    entries.forEach { entry ->
                                        // Key each row by its item so its swipe offset and
                                        // gesture state stay with it: unkeyed, deleting a row
                                        // handed its (open) state to the row that moved up.
                                        val entryKey = entry.key
                                        val isDragged = entryKey == dragKey
                                        key(entryKey) {
                                        // Long-press and drag to move a leg/place within its day
                                        // or onto another day.
                                        // The rows part to open a gap where the dragged one will land.
                                        if (!isDragged) DropGap(
                                            open = dropTarget?.date == date && dropTarget.index == dayOthers.indexOf(entry),
                                            height = dragHeightDp, snap = snapGaps || dragKey == null,
                                            onTop = { gapTop = it },
                                        )
                                        Box(
                                            Modifier
                                                .onGloballyPositioned { c ->
                                                    val b = c.boundsInRoot(); rowBounds[entryKey] = b.top to b.bottom
                                                    if (isDragged) dragSlotTop = b.top
                                                }
                                                // While dragged the row gives up its slot (the gap shows where
                                                // it'll land) and is drawn floating under the finger. The move
                                                // sits outside the collapsed slot; the card look sits inside it,
                                                // at the row's full size.
                                                .zIndex(if (isDragged) 1f else 0f)
                                                .graphicsLayer {
                                                    translationY = if (isDragged) dragFingerY - dragGrabOffset - dragSlotTop else 0f
                                                }
                                                .layout { measurable, constraints ->
                                                    val placeable = measurable.measure(constraints)
                                                    // 1px, not 0: a zero-height row isn't drawn at all.
                                                    layout(placeable.width, if (isDragged) 1 else placeable.height) {
                                                        placeable.place(0, 0)
                                                    }
                                                }
                                                .graphicsLayer {
                                                    if (isDragged) {
                                                        shadowElevation = 12.dp.toPx() * lift
                                                        scaleX = 1f + 0.03f * lift; scaleY = 1f + 0.03f * lift
                                                        shape = RoundedCornerShape(12.dp); clip = true
                                                    }
                                                }
                                                .background(
                                                    if (isDragged) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = lift.coerceIn(0f, 1f))
                                                    else Color.Transparent,
                                                    RoundedCornerShape(12.dp),
                                                )
                                                .then(if (!canEdit) Modifier else Modifier.pointerInput(entryKey) {
                                                    detectDragGesturesAfterLongPress(
                                                        onDragStart = { off ->
                                                            openRowId = null
                                                            dragStartFingerY = (rowBounds[entryKey]?.first ?: 0f) + off.y
                                                            dragFingerY = dragStartFingerY
                                                            dragGrabOffset = off.y
                                                            dragSlotTop = rowBounds[entryKey]?.first ?: 0f
                                                            gapTop = Float.NaN
                                                            dragHeightPx = rowBounds[entryKey]?.let { it.second - it.first } ?: 0f
                                                            dragKey = entryKey
                                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        },
                                                        onDrag = { change, amount ->
                                                            change.consume()
                                                            if (settleTarget == null) dragFingerY += amount.y
                                                        },
                                                        onDragEnd = { endDragLatest() },
                                                        onDragCancel = { dragKey = null; dragEndedAt = TimeSource.Monotonic.markNow() },
                                                    )
                                                }),
                                        ) {
                                        when (entry) {
                                            is DayEntry.LegEntry -> {
                                                val leg = entry.leg
                                                val isNext = leg.id == nextLegId
                                                var stopsExpanded by remember(leg.id) { mutableStateOf(false) }
                                                // Owner sees every pass on the leg; anyone else only the pass assigned to them
                                                // (no chip at all when there's none of theirs).
                                                val legDocs = documents.filter {
                                                    it.legId == leg.id && (isOwner || it.assignedTravellerIds.isEmpty() ||
                                                        myTravellerId in it.assignedTravellerIds)
                                                }

                                                val duration = if (leg.timeLabel.isNotBlank() && leg.endTimeLabel.isNotBlank()) {
                                                    val (h1, m1) = parseHourMinute(leg.timeLabel)
                                                    val (h2, m2) = parseHourMinute(leg.endTimeLabel)
                                                    var diff = (h2 * 60 + m2) - (h1 * 60 + m1)
                                                    if (diff < 0) diff += 24 * 60   // crosses midnight
                                                    val h = diff / 60
                                                    val m = diff % 60
                                                    if (h > 0) "${h}h${m}m" else "${m}m"
                                                } else ""

                                                // ⬅ CHANGED — operator moved out of the title into
                                                // the meta line. Titles were running to two lines.
                                                val meta = buildList {
                                                    if (leg.operator.isNotBlank()) add(leg.operator)
                                                    when {
                                                        leg.timeLabel.isNotBlank() && leg.endTimeLabel.isNotBlank() ->
                                                            add("${leg.timeLabel} – ${leg.endTimeLabel}")
                                                        leg.timeLabel.isNotBlank() -> add(leg.timeLabel)
                                                        leg.endTimeLabel.isNotBlank() -> add(leg.endTimeLabel)
                                                    }
                                                    if (duration.isNotBlank()) add(duration)
                                                }.joinToString(" · ")

                                                SwipeRevealRow(
                                                    enabled = canEdit && dragKey == null,
                                                    isOpen = openRowId == leg.id,
                                                    onOpenChange = { open -> openRowId = if (open) leg.id else openRowId.takeIf { it != leg.id } },
                                                    onEdit = { onEditLeg(leg.id) },
                                                    onDelete = { onDeleteLeg(leg.id) },
                                                ) {
                                                    Row(
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                // Releasing a long-press drag isn't a tap.
                                                                if (justDragged()) return@clickable
                                                                // A tap while a row is open just closes it.
                                                                if (openRowId != null) openRowId = null
                                                                else if (canEdit) onToggleLeg(leg.id)
                                                            }
                                                            .padding(vertical = 9.dp),
                                                        verticalAlignment = Alignment.Top,
                                                    ) {
                                                        TimelineMarker(
                                                            completed = leg.completed,
                                                            highlighted = isNext,
                                                            icon = transportIcon(leg.transport),
                                                        )
                                                        Spacer(Modifier.width(12.dp))
                                                        Column(Modifier.weight(1f)) {
                                                            Text(
                                                                "${leg.fromCity} → ${leg.toCity}",
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                fontWeight = if (isNext) FontWeight.Medium else FontWeight.Normal,
                                                                textDecoration = if (leg.completed) TextDecoration.LineThrough else null,
                                                                color = if (leg.completed)
                                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                                                else MaterialTheme.colorScheme.onSurface,
                                                            )
                                                            if (meta.isNotBlank()) {
                                                                Spacer(Modifier.height(2.dp))
                                                                Text(
                                                                    meta,
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                                        alpha = if (leg.completed) 0.4f else 0.6f
                                                                    ),
                                                                )
                                                            }

                                                            if (legDocs.isNotEmpty() || leg.travellerIds.isNotEmpty() || isNext) {
                                                                Spacer(Modifier.height(7.dp))
                                                                Row(
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                ) {
                                                                    if (isNext) {
                                                                        MetaChip(label = s.nextUp, accent = true)
                                                                    }
                                                                    if (legDocs.isNotEmpty()) {
                                                                        MetaChip(
                                                                            icon = Icons.Filled.QrCode2,
                                                                            label = s.viewTicket,
                                                                            loading = barcodeLoading,
                                                                            onClick = { openLegTickets(leg, legDocs) },
                                                                        )
                                                                    }
                                                                    if (leg.travellerIds.isNotEmpty()) {
                                                                        MetaChip(
                                                                            icon = Icons.Filled.People,
                                                                            label = "${leg.travellerIds.size}",
                                                                        )
                                                                    }
                                                                }
                                                            }

                                                            if (leg.stops.isNotEmpty()) {
                                                                Spacer(Modifier.height(4.dp))
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    modifier = Modifier.clickable(
                                                                        interactionSource = remember { MutableInteractionSource() },
                                                                        indication = null,
                                                                    ) { stopsExpanded = !stopsExpanded },
                                                                ) {
                                                                    Text(
                                                                        "via ${leg.stops.size} " + if (leg.stops.size == 1) "stop" else "stops",
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                                    )
                                                                    Icon(
                                                                        if (stopsExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                                                        contentDescription = null,
                                                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                                        modifier = Modifier.size(18.dp),
                                                                    )
                                                                }
                                                                if (stopsExpanded) {
                                                                    val segments = buildList {
                                                                        var prevCity = leg.fromCity
                                                                        var prevDep = leg.timeLabel
                                                                        leg.stops.forEach { stop ->
                                                                            add(LegSegment(prevCity, prevDep, stop.city, stop.arrivalTime))
                                                                            prevCity = stop.city
                                                                            prevDep = stop.departureTime
                                                                        }
                                                                        add(LegSegment(prevCity, prevDep, leg.toCity, leg.endTimeLabel))
                                                                    }
                                                                    segments.forEach { seg ->
                                                                        Row(
                                                                            verticalAlignment = Alignment.CenterVertically,
                                                                            modifier = Modifier.padding(start = 8.dp, top = 3.dp),
                                                                        ) {
                                                                            Icon(
                                                                                Icons.Outlined.Circle, null,
                                                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                                                                modifier = Modifier.size(8.dp),
                                                                            )
                                                                            Spacer(Modifier.width(8.dp))
                                                                            val times = listOf(seg.depTime, seg.arrTime)
                                                                                .filter { it.isNotBlank() }.joinToString(" - ")
                                                                            Text(
                                                                                "${seg.fromCity} → ${seg.toCity}" +
                                                                                        if (times.isBlank()) "" else "  ·  $times",
                                                                                style = MaterialTheme.typography.bodySmall,
                                                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                }
                                            }

                                            is DayEntry.ActEntry -> {
                                                val act = entry.act
                                                val actDocs = documents.filter {
                                                    it.activityId == act.id && trip.canSeeDocument(it, currentUid) &&
                                                        placeDocHasCode[it.id + "|" + it.fileUrl] == true
                                                }
                                                val tail = listOf(act.time, act.location)
                                                    .filter { it.isNotBlank() }.joinToString(" · ")

                                                SwipeRevealRow(
                                                    enabled = canEdit && dragKey == null,
                                                    isOpen = openRowId == act.id,
                                                    onOpenChange = { open -> openRowId = if (open) act.id else openRowId.takeIf { it != act.id } },
                                                    onEdit = { onEditActivity(act.id) },
                                                    onDelete = { onDeleteActivity(act.id) },
                                                ) {
                                                    Row(
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                if (justDragged()) return@clickable
                                                                if (openRowId != null) openRowId = null
                                                                else if (canEdit) onToggleActivity(act.id)
                                                            }
                                                            .padding(vertical = 9.dp),
                                                        verticalAlignment = Alignment.Top,
                                                    ) {
                                                        TimelineMarker(
                                                            completed = act.completed,
                                                            highlighted = false,
                                                            icon = Icons.Filled.Place,
                                                        )
                                                        Spacer(Modifier.width(12.dp))
                                                        Column(Modifier.weight(1f)) {
                                                            Text(
                                                                act.title,
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                textDecoration = if (act.completed) TextDecoration.LineThrough else null,
                                                                color = if (act.completed)
                                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                                                else MaterialTheme.colorScheme.onSurface,
                                                            )
                                                            if (tail.isNotBlank()) {
                                                                Spacer(Modifier.height(2.dp))
                                                                Text(
                                                                    tail,
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                                        alpha = if (act.completed) 0.4f else 0.6f
                                                                    ),
                                                                )
                                                            }
                                                            if (actDocs.isNotEmpty()) {
                                                                Spacer(Modifier.height(7.dp))
                                                                MetaChip(
                                                                    icon = Icons.Filled.QrCode2,
                                                                    label = s.viewTicket,
                                                                    loading = barcodeLoading,
                                                                    onClick = { openActivityTickets(act, actDocs) },
                                                                )
                                                            }
                                                        }

                                                        // ⬅ ADD — opens the place in the platform maps app.
                                                        // Only when there's something to search for; the row's
                                                        // own tap still toggles completion.
                                                        if (act.location.isNotBlank()) {
                                                            Spacer(Modifier.width(8.dp))
                                                            Box(
                                                                Modifier
                                                                    .size(32.dp)
                                                                    .clip(CircleShape)
                                                                    .clickable {
                                                                        openInMaps(
                                                                            // Search by the address alone: Google Maps reads a title like
                                                                            // "Fly back to Toulouse" as a route request and opens directions.
                                                                            label = act.location.ifBlank { act.title },
                                                                            latitude = act.lat.takeIf { it != 0.0 },
                                                                            longitude = act.lng.takeIf { it != 0.0 },
                                                                        )
                                                                    },
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                Icon(
                                                                    Icons.Filled.Directions,
                                                                    contentDescription = s.openInMaps,
                                                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                                    modifier = Modifier.size(18.dp),
                                                                )
                                                            }
                                                        }
                                                    }

                                                }
                                            }
                                        }
                                        }
                                        }
                                    }
                                    DropGap(
                                        open = dropTarget?.date == date && dropTarget.index == dayOthers.size,
                                        height = dragHeightDp, snap = snapGaps || dragKey == null,
                                        onTop = { gapTop = it },
                                    )
                                }
                            }
                        }
                        // Souvenir appears at the END of the itinerary once legs or places are added.
                        // It remains visible but disabled until all legs are checked as completed.
                        val hasContent = trip.legs.isNotEmpty() || activities.isNotEmpty()
                        AnimatedVisibility(
                            visible = hasContent,
                            enter = fadeIn() + slideInVertically { it / 2 },
                            exit = fadeOut(),
                        ) {
                            Column(
                                Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Spacer(Modifier.height(16.dp))
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .alpha(if (allComplete) 1f else 0.45f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable(enabled = allComplete) { showPostcard = true }
                                        .padding(12.dp),
                                ) {
                                    Box(
                                        Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (allComplete) MaterialTheme.colorScheme.primaryContainer
                                                else Color.Gray.copy(alpha = 0.18f)
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("💌", fontSize = 30.sp)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        s.souvenir,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = souvenirFont,
                                        fontStyle = FontStyle.Italic,
                                        color = if (allComplete) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }

                    }
                }

                // Fixed action buttons — pinned to the bottom, do NOT scroll.
                // ⬅ CHANGED — scrim and buttons are now one self-sizing unit: its
                // real rendered height (button height + padding + the device's
                // own safe-area inset) is measured and fed back as the scroll
                // content's bottom padding, so the last day/leg always clears it
                // and reaches the true bottom edge instead of getting stuck
                // underneath. The gradient still gives content a soft fade
                // rather than a hard cut as it scrolls behind the buttons.
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .onGloballyPositioned {
                            floatingBarHeight = with(density) { it.size.height.toDp() }
                        }
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to MaterialTheme.colorScheme.background,
                            )
                        )
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
                ) {
                    Spacer(Modifier.height(56.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 48.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = onChecklist,
                            modifier = Modifier.height(50.dp),
                            contentPadding = PaddingValues(horizontal = 25.dp, vertical = 8.dp),
                            shape = CircleShape,
                        ) {
                            Text(
                                s.beforeYouGo,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }

                        if (canEdit) {
                            Spacer(Modifier.width(30.dp))
                            Button(
                                onClick = { showAddChooser = true },
                                modifier = Modifier.height(50.dp),
                                contentPadding = PaddingValues(horizontal = 25.dp, vertical = 8.dp),
                                shape = CircleShape,
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s.add)
                            }
                        }
                    }
                }
            }
        }


        if (showAddChooser) {
            AlertDialog(
                onDismissRequest = { showAddChooser = false },
                confirmButton = {},
                title = { Text(s.addToItinerary) },
                shape = RoundedCornerShape(16.dp),
                text = {
                    Column {
                        ListItem(
                            headlineContent = { Text(s.travel) },
                            supportingContent = { Text(s.aLegBetween) },
                            leadingContent = { Icon(Icons.Filled.Flight, null) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { showAddChooser = false; onAddLeg() },
                        )
                        ListItem(
                            headlineContent = { Text(s.place) },
                            supportingContent = { Text(s.somewhereToVisit) },
                            leadingContent = { Icon(Icons.Filled.Place, null) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { showAddChooser = false; onAddPlace() },
                        )
                    }
                },
            )
        }

        // ── ticket wallet (swipeable codes for the tapped leg or place) ──
        walletLeg?.let { leg ->
            if (walletTickets.isNotEmpty()) {
                TicketWalletDialog(
                    legRoute = "${leg.fromCity} → ${leg.toCity}",
                    legDateLabel = leg.date.label(),
                    legTime = leg.timeLabel,
                    operator = leg.operator,
                    transport = leg.transport,
                    tickets = walletTickets,
                    myTravellerId = myTravellerId,
                    // Not travelling on this leg yourself (e.g. the owner checking the pass
                    // they assigned to someone else): open on the leg's passes rather than
                    // an empty "no pass for you".
                    notOnThisLeg = leg.travellerIds.isNotEmpty() && myTravellerId !in leg.travellerIds,
                    canSeeAllPasses = isOwner,
                    canManagePasses = canEdit,
                    onManagePasses = {
                        walletTickets = emptyList()
                        walletLeg = null
                        onDocuments()
                    },
                    onOpenFullTicket = { docId -> onOpenDoc(docId) },
                    onDismiss = { walletTickets = emptyList(); walletLeg = null },
                )
            }
        }
        walletAct?.let { act ->
            if (walletTickets.isNotEmpty()) {
                TicketWalletDialog(
                    legRoute = act.title,
                    legDateLabel = act.date.label(),
                    legTime = act.time,
                    operator = "",
                    transport = null,
                    tickets = walletTickets,
                    myTravellerId = myTravellerId,
                    canSeeAllPasses = isOwner,
                    canManagePasses = canEdit,
                    onManagePasses = {
                        walletTickets = emptyList()
                        walletAct = null
                        onDocuments()
                    },
                    onOpenFullTicket = { docId -> onOpenDoc(docId) },
                    onDismiss = { walletTickets = emptyList(); walletAct = null },
                )
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // POSTCARD DIALOG — all extra UI (crop, camera, source sheet) are
        // overlays INSIDE this one dialog (no stacked dialogs → no iOS freeze).
        // The dialog is hidden only while the native gallery picker is up.
        // ═══════════════════════════════════════════════════════════════════
        if (showPostcard && !isPickingImage) {
            Dialog(
                onDismissRequest = { showPostcard = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Trip complete !",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White, fontWeight = FontWeight.Bold,
                                fontFamily = displayFont, fontSize = 40.sp,
                            )
                            Spacer(Modifier.height(16.dp))

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1748f / 1240f),
                            ) { page ->
                                if (page == 0) {
                                    PostcardFront(
                                        paper = Res.drawable.postcard_paper,
                                        halftone = Res.drawable.postcard_halftone,
                                        map = Res.drawable.postcard_map,
                                        heartFrame = Res.drawable.postcard_heart_frame,
                                        rectFrame = Res.drawable.postcard_rect_frame,
                                        heartMask = Res.drawable.heart_fill,
                                        rectMask = Res.drawable.rect_fill,
                                        title = Res.drawable.postcard_title,
                                        plane = Res.drawable.postcard_plane,
                                        country = postcardCountry,
                                        onPickHeart = { activeSlotForSheet = "heart" },
                                        onPickRect  = { activeSlotForSheet = "rect" },
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                                        heartPhoto = heartUrl.takeIf { it.isNotBlank() }?.let { u -> {
                                            AsyncImage(u, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } },
                                        rectPhoto = rectUrl.takeIf { it.isNotBlank() }?.let { u -> {
                                            AsyncImage(u, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } },
                                    )
                                } else {
                                    PostcardBack(
                                        paper = Res.drawable.postcard_paper,
                                        map = Res.drawable.pb_map,
                                        frameTop = Res.drawable.pb_frame_top,
                                        frameBottom = Res.drawable.pb_frame_bottom,
                                        maskTop = Res.drawable.pb_mask_top,
                                        maskBottom = Res.drawable.pb_mask_bottom,
                                        stamp = Res.drawable.pb_stamp,
                                        plane = Res.drawable.pb_plane,
                                        envelope = Res.drawable.pb_envelope,
                                        title = Res.drawable.pb_title,
                                        country = postcardCountry,
                                        dateRange = if (allDates.isNotEmpty())
                                            "${allDates.first().label()} – ${allDates.last().label()}" else "",
                                        countriesCount = trip.countriesCovered(),
                                        distanceKm = trip.distanceTravelledKm(),
                                        daysCount = daysCount,
                                        expensesLabel = expensesLabel,
                                        travellers = travellers.map { it.firstName.substringBefore(" ") },
                                        onPickTop    = { activeSlotForSheet = "backTop" },
                                        onPickBottom = { activeSlotForSheet = "backBottom" },
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                                        topPhoto = backTopUrl.takeIf { it.isNotBlank() }?.let { u -> {
                                            AsyncImage(u, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } },
                                        bottomPhoto = backBottomUrl.takeIf { it.isNotBlank() }?.let { u -> {
                                            AsyncImage(u, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } },
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {     // page dots
                                repeat(2) { i ->
                                    Box(
                                        Modifier.size(8.dp).clip(CircleShape)
                                            .background(if (pagerState.currentPage == i) Color.White else Color.White.copy(alpha = 0.4f)),
                                    )
                                    if (i == 0) Spacer(Modifier.width(6.dp))
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    enabled = !exporting,
                                    onClick = {
                                        scope.launch {
                                            exporting = true
                                            try {
                                                val png = exporter().toPngBytes()
                                                fileSharer.share(png, "postcard.png", "image/png")
                                            } catch (t: Throwable) {
                                                println("POSTCARD EXPORT FAILED: ${t.message}")
                                            } finally {
                                                exporting = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.background(OverlayControlScrim.copy(alpha = 0.5f), CircleShape),
                                ) {
                                    if (exporting)
                                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                    else
                                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
                                }
                                IconButton(
                                    onClick = { showPostcard = false },
                                    modifier = Modifier.background(OverlayControlScrim.copy(alpha = 0.5f), CircleShape),
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = s.close, tint = Color.White)
                                }
                            }
                        }
                    }

                    // ── crop overlay ──
                    if (pendingBytes != null) {
                        val aspect = when (pendingSlot) {
                            "rect"       -> 0.75f
                            "heart"      -> 0.78f
                            "backTop"    -> 0.97f
                            "backBottom" -> 0.89f
                            else         -> 0.78f
                        }
                        ImageCropScreen(
                            imageBytes = pendingBytes!!,
                            aspectRatio = aspect,
                            onConfirm = { cropped ->
                                val slot = pendingSlot
                                scope.launch {
                                    try {
                                        val url = onUploadPostcardPhoto?.invoke(slot ?: "", cropped) ?: ""
                                        if (url.isNotBlank()) when (slot) {
                                            "heart"      -> heartUrl = url
                                            "rect"       -> rectUrl = url
                                            "backTop"    -> backTopUrl = url
                                            "backBottom" -> backBottomUrl = url
                                        }
                                    } catch (t: Throwable) {
                                        println("POSTCARD UPLOAD FAILED: ${t.message}")
                                    } finally {
                                        pendingBytes = null; pendingSlot = null
                                    }
                                }
                            },
                            onDismiss = { pendingBytes = null; pendingSlot = null },
                        )
                    }

                    // ── photo source sheet (gallery / take photo / remove) ──
                    val slot = activeSlotForSheet
                    if (slot != null) {
                        val currentUrl = when (slot) {
                            "heart"      -> heartUrl
                            "rect"       -> rectUrl
                            "backTop"    -> backTopUrl
                            "backBottom" -> backBottomUrl
                            else         -> ""
                        }
                        val hasPhoto = currentUrl.isNotBlank()
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { activeSlotForSheet = null },
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) {},
                            ) {
                                Column(
                                    Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 24.dp)
                                        .navigationBarsPadding(),
                                ) {
                                    Text(
                                        s.choosePhoto,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(vertical = 12.dp),
                                    )
                                    HorizontalDivider()
                                    ListItem(
                                        headlineContent = { Text(s.uploadFromGallery) },
                                        leadingContent = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                                        modifier = Modifier.clickable {
                                            pickTarget = slot
                                            activeSlotForSheet = null
                                            isPickingImage = true
                                            picker.launch()
                                        },
                                    )
                                    if (hasPhoto) {
                                        ListItem(
                                            headlineContent = { Text(s.removePhoto, color = MaterialTheme.colorScheme.error) },
                                            leadingContent = {
                                                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                            },
                                            modifier = Modifier.clickable {
                                                when (slot) {
                                                    "heart"      -> heartUrl = ""
                                                    "rect"       -> rectUrl = ""
                                                    "backTop"    -> backTopUrl = ""
                                                    "backBottom" -> backBottomUrl = ""
                                                }
                                                onRemovePostcardPhoto?.invoke(slot)
                                                activeSlotForSheet = null
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** A derived sub-leg between consecutive points of a leg with stops. */
/**
 * Day heading with a numbered chip and the day's destination.
 *
 * Not sticky: the itinerary body is a `Column` inside `verticalScroll`, and
 * `stickyHeader` needs a `LazyColumn`. Converting would also mean reworking the
 * postcard reveal, which calls `bodyScroll.animateScrollTo(maxValue)`.
 */
@Composable
private fun DayHeader(dayNumber: Int, dateLabel: String) {
    val s = LocalStrings.current
    Row(
        Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$dayNumber",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "${s.day} $dayNumber · $dateLabel",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * 24dp marker on the timeline spine. Carries the transport type or a pin, so a
 * completed train and a completed museum no longer look identical — the icon
 * used to sit at 14dp inside the metadata line where it read as decoration.
 */
@Composable
private fun TimelineMarker(
    completed: Boolean,
    highlighted: Boolean,
    icon: ImageVector,
) {
    val done = MaterialTheme.itinera.success
    val primary = MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                when {
                    completed -> done
                    highlighted -> primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (completed) Icons.Filled.Check else icon,
            contentDescription = null,
            tint = when {
                completed || highlighted -> Color.White
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            },
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun MetaChip(
    label: String,
    icon: ImageVector? = null,
    accent: Boolean = false,
    loading: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val tint = if (accent) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    Surface(
        shape = CircleShape,
        color = if (accent) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        modifier = if (onClick != null && !loading) Modifier.clickable { onClick() } else Modifier,
    ) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 1.5.dp, color = tint)
                Spacer(Modifier.width(5.dp))
            } else if (icon != null) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
        }
    }
}

@Composable
private fun TripStatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
        }
    }
}

private data class LegSegment(
    val fromCity: String,
    val depTime: String,
    val toCity: String,
    val arrTime: String,
)


private sealed interface DayEntry {
    val key: String
    val orderKey: Double
    data class LegEntry(val leg: com.itinera.app.model.Leg) : DayEntry {
        override val key get() = "leg-${leg.id}"
        override val orderKey = dayOrderKey(leg.sortKey, leg.timeLabel)
    }
    data class ActEntry(val act: com.itinera.app.model.Activity) : DayEntry {
        override val key get() = "act-${act.id}"
        override val orderKey = dayOrderKey(act.sortKey, act.time)
    }
}

/** Where a dragged row would land: a day, and its position among that day's other rows. */
private data class DropTarget(val date: kotlinx.datetime.LocalDate, val index: Int)

/** The space a dragged row will drop into; springs open/closed as the drop spot moves. */
@Composable
private fun DropGap(open: Boolean, height: Dp, snap: Boolean, onTop: (Float) -> Unit) {
    val target = if (open) height else 0.dp
    val animated by animateDpAsState(
        targetValue = target,
        animationSpec = if (snap) snap() else spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "dropGap",
    )
    // On pick-up/drop use the target straight away: even snap() lags one frame, which let
    // the next row jump into the lifted row's place for a frame.
    val h = if (snap) target else animated
    if (h > 0.dp) Spacer(
        Modifier.fillMaxWidth().height(h)
            .onGloballyPositioned { c -> if (open) onTop(c.boundsInRoot().top) }
    )
}
