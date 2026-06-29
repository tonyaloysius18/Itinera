package com.itinera.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Activity
import com.itinera.app.model.Trip
import com.itinera.app.model.label
import com.itinera.app.ui.components.PostcardFront
import com.itinera.app.ui.components.Progress
import com.itinera.app.ui.components.TopBar
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Mail
import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.decodeToImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.itinera.app.model.DocItem
import com.itinera.app.resources.Res
import com.itinera.app.resources.*
import com.itinera.app.ui.components.ImageCropScreen
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import org.jetbrains.compose.resources.Font


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripDetailScreen(
    trip: Trip,
    activities: List<Activity>,
    documents: List<DocItem> = emptyList(),
    onBack: () -> Unit,
    onTravellers: () -> Unit,
    onDocuments: () -> Unit,
    onOpenDoc: (String) -> Unit = {},
    onAddLeg: () -> Unit,
    onAddPlace: () -> Unit,
    onChecklist: () -> Unit,
    onToggleLeg: (String) -> Unit,
    onEditLeg: (String) -> Unit,
    onEditActivity: (String) -> Unit,
    onDeleteLeg: (String) -> Unit,
    onToggleActivity: (String) -> Unit,
    onDeleteActivity: (String) -> Unit,
    canEdit: Boolean = true,
    onMembers: () -> Unit,
) {
    val s = LocalStrings.current
    val done = trip.legs.count { it.completed }

    // ── Postcard photo slots (decoded bitmaps) + crop pipeline state ──
    var heartImage by remember { mutableStateOf<ImageBitmap?>(null) }   // ⬅ CHANGED: ImageBitmap?
    var rectImage  by remember { mutableStateOf<ImageBitmap?>(null) }   // ⬅ CHANGED: ImageBitmap?
    var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }   // awaiting crop
    var pendingSlot  by remember { mutableStateOf<String?>(null) }      // "heart" or "rect"
    var isPickingImage by remember { mutableStateOf(false) }            // hides postcard while native picker is up

    // ── ONE scope + ONE picker per slot (the duplicate block is removed) ──
    val scope = rememberCoroutineScope()
    val heartPicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = {
            it.firstOrNull()?.let { b -> pendingBytes = b; pendingSlot = "heart" }
            isPickingImage = false
        },
    )
    val rectPicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = {
            it.firstOrNull()?.let { b -> pendingBytes = b; pendingSlot = "rect" }
            isPickingImage = false
        },
    )

    var pendingDeleteLegId by remember { mutableStateOf<String?>(null) }
    var pendingDeleteActivityId by remember { mutableStateOf<String?>(null) }

    var showAddChooser by remember { mutableStateOf(false) }
    var showPostcard by remember { mutableStateOf(false) }

    val totalItems = trip.legs.size + activities.size
    val allComplete = totalItems > 0 &&
            trip.legs.all { it.completed } && activities.all { it.completed }

    var wasComplete by remember { mutableStateOf(allComplete) }
    LaunchedEffect(allComplete) {
        if (allComplete && !wasComplete) showPostcard = true
        wasComplete = allComplete
    }

    val allDates = (trip.legs.map { it.date } + activities.map { it.date })
        .distinct()
        .sorted()

    val legsByDate = trip.legs.groupBy { it.date }
    val actsByDate = activities.groupBy { it.date }

    val displayFont = FontFamily(Font(Res.font.arizonia_regular))
    val souvenirFont = FontFamily(Font(Res.font.caudex_bold))
    val nextLegId = trip.legs.sortedWith(compareBy({ it.date }, { it.timeLabel }))
        .firstOrNull { !it.completed }?.id

    Column(Modifier.fillMaxSize()) {
        TopBar(
            trip.title,
            onBack = onBack,
            trailing = {
                Row {
                    IconButton(onClick = onTravellers) {
                        Icon(Icons.Filled.People, contentDescription = s.travellers, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDocuments) {
                        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = s.documents, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onMembers) {
                        Icon(Icons.Filled.Groups, contentDescription = s.members, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
        )
        Spacer(Modifier.height(8.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                "$done ${s.legsTravelled}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(6.dp))
            Progress(if (trip.legs.isEmpty()) 0f else done.toFloat() / trip.legs.size)
        }
        Spacer(Modifier.height(14.dp))

        Box(Modifier.weight(1f).fillMaxWidth()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                if (allDates.isEmpty()) {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 300.dp),
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
                        Text(
                            "${s.day} $dayNumber · ${date.label()}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        )

                        legsByDate[date].orEmpty().forEach { leg ->
                            val isNext = leg.id == nextLegId
                            var showMenu by remember { mutableStateOf(false) }

                            Box {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { if (canEdit) onToggleLeg(leg.id) },
                                            onLongClick = { if (canEdit) showMenu = true },
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    if (leg.completed) {
                                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF1D9E75), modifier = Modifier.size(20.dp))
                                    } else {
                                        Icon(
                                            Icons.Outlined.Circle, null,
                                            tint = if (isNext) Color(0xFFBA7517) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        val operatorSuffix = if (leg.operator.isNotBlank()) " (${leg.operator})" else ""
                                        Text(
                                            "${leg.fromCity} → ${leg.toCity}$operatorSuffix",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isNext) FontWeight.Medium else FontWeight.Normal,
                                            textDecoration = if (leg.completed) TextDecoration.LineThrough else null,
                                            color = if (leg.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(transportIcon(leg.transport), null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            Spacer(Modifier.width(6.dp))
                                            val tail = if (isNext) {
                                                if (leg.timeLabel.isNotBlank()) "${leg.timeLabel} · ${s.nextUp}" else s.nextUp
                                            } else {
                                                leg.timeLabel
                                            }
                                            Text(
                                                tail,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isNext) Color(0xFFBA7517) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            )
                                        }
                                    }
                                }

                                val legDocs = documents.filter { it.legId == leg.id }
                                if (legDocs.isNotEmpty()) {
                                    var showLegDocs by remember(leg.id) { mutableStateOf(false) }
                                    IconButton(
                                        onClick = {
                                            if (legDocs.size == 1) onOpenDoc(legDocs.first().id)
                                            else showLegDocs = true
                                        },
                                        modifier = Modifier.size(34.dp).align(Alignment.CenterEnd),
                                    ) {
                                        Icon(
                                            Icons.Filled.ConfirmationNumber,
                                            contentDescription = s.viewTicket,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    if (showLegDocs) {
                                        AlertDialog(
                                            onDismissRequest = { showLegDocs = false },
                                            title = { Text("${leg.fromCity} → ${leg.toCity}") },
                                            shape = RoundedCornerShape(16.dp),
                                            text = {
                                                Column {
                                                    legDocs.forEach { d ->
                                                        Row(
                                                            Modifier.fillMaxWidth()
                                                                .clickable { showLegDocs = false; onOpenDoc(d.id) }
                                                                .padding(vertical = 12.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                        ) {
                                                            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                                            Spacer(Modifier.width(10.dp))
                                                            Text(d.title, style = MaterialTheme.typography.bodyLarge)
                                                        }
                                                    }
                                                }
                                            },
                                            confirmButton = {},
                                            dismissButton = { TextButton(onClick = { showLegDocs = false }) { Text(s.close) } },
                                        )
                                    }
                                }
                            }

                            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp))) {
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    offset = DpOffset(x = 280.dp, y = 0.dp),
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(s.edit) },
                                        leadingIcon = { Icon(Icons.Filled.Edit, null) },
                                        onClick = { showMenu = false; onEditLeg(leg.id) },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(s.delete, color = Color(0xFFE03131)) },
                                        leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFE03131)) },
                                        onClick = { showMenu = false; pendingDeleteLegId = leg.id },
                                    )
                                }
                            }
                        }

                        actsByDate[date].orEmpty().forEach { act ->
                            var showMenu by remember { mutableStateOf(false) }

                            Box {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { if (canEdit) onToggleActivity(act.id) },
                                            onLongClick = { if (canEdit) showMenu = true },
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    if (act.completed) {
                                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF1D9E75), modifier = Modifier.size(20.dp))
                                    } else {
                                        Icon(Icons.Filled.Place, null, tint = Color(0xFF378ADD), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            act.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            textDecoration = if (act.completed) TextDecoration.LineThrough else null,
                                            color = if (act.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                        )
                                        val tail = listOf(act.time, act.location).filter { it.isNotBlank() }.joinToString(" · ")
                                        if (tail.isNotBlank()) {
                                            Spacer(Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    tail,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                )
                                            }
                                        }
                                    }
                                }

                                MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp))) {
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        offset = DpOffset(x = 280.dp, y = 0.dp),
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(s.edit) },
                                            leadingIcon = { Icon(Icons.Filled.Edit, null) },
                                            onClick = { showMenu = false; onEditActivity(act.id) },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(s.delete, color = Color(0xFFE03131)) },
                                            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFE03131)) },
                                            onClick = { showMenu = false; pendingDeleteActivityId = act.id },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (allComplete) {                       // ⬅ only once the trip is complete (postcard has shown)
                    Spacer(Modifier.height(28.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.18f))   // grey transparent circle
                                    .clickable { showPostcard = true },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("💌", fontSize = 30.sp)                       // emoji instead of the Mail icon
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                s.souvenir, //"Your Souvenir !",                                 // ⬅ replaces "Postcard"
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = souvenirFont
                            )
                        }
                    }
                }
                Spacer(Modifier.height(96.dp))
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = onChecklist,
                    modifier = Modifier.padding(bottom = 60.dp).height(50.dp),
                    contentPadding = PaddingValues(horizontal = 25.dp, vertical = 8.dp),
                    shape = CircleShape,
                ) {
                    Text(s.beforeYouGo, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
                if (canEdit) {
                    Spacer(Modifier.width(30.dp))
                    Button(
                        onClick = { showAddChooser = true },
                        modifier = Modifier.padding(bottom = 60.dp).height(50.dp),
                        contentPadding = PaddingValues(horizontal = 25.dp, vertical = 8.dp),
                        shape = CircleShape,
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(s.add, style = MaterialTheme.typography.bodyMedium)
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

    if (pendingDeleteLegId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteLegId = null },
            title = { Text(s.deleteLegQ) },
            text = { Text(s.cantBeUndone) },
            confirmButton = {
                TextButton(onClick = { onDeleteLeg(pendingDeleteLegId!!); pendingDeleteLegId = null }) {
                    Text(s.delete, color = Color(0xFFE03131))
                }
            },
            dismissButton = { TextButton(onClick = { pendingDeleteLegId = null }) { Text(s.cancel) } },
        )
    }

    if (pendingDeleteActivityId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteActivityId = null },
            title = { Text(s.deletePlaceQ) },
            text = { Text(s.cantBeUndone) },
            confirmButton = {
                TextButton(onClick = { onDeleteActivity(pendingDeleteActivityId!!); pendingDeleteActivityId = null }) {
                    Text(s.delete, color = Color(0xFFE03131))
                }
            },
            dismissButton = { TextButton(onClick = { pendingDeleteActivityId = null }) { Text(s.cancel) } },
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    // POSTCARD DIALOG  —  the crop screen is an OVERLAY INSIDE this same
    // dialog (NOT a second Dialog), which is what fixes the iOS freeze.
    // Hidden while the native picker is up (isPickingImage).
    // ═══════════════════════════════════════════════════════════════════
    if (showPostcard && !isPickingImage) {
        Dialog(
            onDismissRequest = { showPostcard = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(Modifier.fillMaxSize()) {

                // --- postcard content ---
                Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            s.tripCompleted,  //"Trip complete !",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = displayFont,
                            fontSize = 40.sp,
                        )
                        Spacer(Modifier.height(16.dp))
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
                            country = trip.title.trim().substringBefore(" "),
                            onPickHeart = {
                                isPickingImage = true
                                heartPicker.launch()
                            },
                            onPickRect = {
                                isPickingImage = true
                                rectPicker.launch()
                            },
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                            heartPhoto = heartImage?.let { bmp -> {
                                Image(bmp, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } },
                            rectPhoto = rectImage?.let { bmp -> {
                                Image(bmp, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } },
                        )
                        Spacer(Modifier.height(16.dp))
                        IconButton(
                            onClick = { showPostcard = false },
                            modifier = Modifier.background(Color(0xFF333333).copy(alpha = 0.5f), CircleShape),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = s.close, tint = Color.White)
                        }
                    }
                }

                // --- CROP OVERLAY (same dialog) ---
                if (pendingBytes != null) {
                    ImageCropScreen(
                        imageBytes = pendingBytes!!,
                        aspectRatio = if (pendingSlot == "rect") 0.75f else 0.78f,
                        onConfirm = { cropped ->
                            try {
                                if (cropped.isNotEmpty()) {
                                    val bitmap = cropped.decodeToImageBitmap()
                                    if (pendingSlot == "heart") heartImage = bitmap else rectImage = bitmap
                                } else {
                                    println("CROP ERROR: empty byte array from native cropper")
                                }
                            } catch (t: Throwable) {
                                println("CROP DECODE FAILED: ${t.message}")
                            } finally {
                                pendingBytes = null
                                pendingSlot = null
                            }
                        },
                        onDismiss = {
                            pendingBytes = null
                            pendingSlot = null
                        },
                    )
                }
            }
        }
    }
}