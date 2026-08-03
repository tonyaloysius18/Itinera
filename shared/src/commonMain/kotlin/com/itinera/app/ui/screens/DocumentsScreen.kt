package com.itinera.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itinera.app.data.PickedFile
import com.itinera.app.data.rememberFilePicker
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.i18n.Strings
import com.itinera.app.model.DocItem
import com.itinera.app.model.Leg
import com.itinera.app.model.Traveller
import com.itinera.app.model.Trip
import com.itinera.app.parseHourMinute
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.PlaneLoader
import com.itinera.app.ui.components.TopBar
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val CAT_TRANSPORT = "TRANSPORT"
private const val CAT_ACCOMMODATION = "ACCOMMODATION"
private const val CAT_ATTRACTION = "ATTRACTION"
private const val CAT_OTHER = "OTHER"

private val ALL_CATEGORIES = listOf(CAT_TRANSPORT, CAT_ACCOMMODATION, CAT_ATTRACTION, CAT_OTHER)

private fun categoryLabel(category: String, s: Strings): String = when (category) {
    CAT_TRANSPORT -> s.transport
    CAT_ACCOMMODATION -> s.accommodation
    CAT_ATTRACTION -> s.attraction
    else -> s.other
}

private fun docIcon(mimeType: String): ImageVector = when {
    mimeType.contains("pdf", ignoreCase = true) -> Icons.Filled.PictureAsPdf
    mimeType.startsWith("image", ignoreCase = true) -> Icons.Filled.Image
    else -> Icons.AutoMirrored.Filled.InsertDriveFile
}

private fun docColor(mimeType: String): Color = when {
    mimeType.contains("pdf", ignoreCase = true) -> Color(0xFFA32D2D)
    mimeType.startsWith("image", ignoreCase = true) -> Color(0xFF378ADD)
    else -> Color(0xFF7A7A7A)
}

/** Sub-leg options for a leg with stops: index to "CityA → CityB" (whole journey = -1). */
private fun segmentOptions(leg: Leg): List<Pair<Int, String>> {
    if (leg.stops.isEmpty()) return emptyList()
    val cities = listOf(leg.fromCity) + leg.stops.map { it.city } + listOf(leg.toCity)
    return (0 until cities.size - 1).map { i -> i to "${cities[i]} → ${cities[i + 1]}" }
}

private fun nameWithoutExtension(fileName: String): String =
    fileName.substringBeforeLast('.', fileName)

@OptIn(ExperimentalTime::class)
private fun nowMillisDocs(): Long =
    Clock.System.now().toEpochMilliseconds()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentsScreen(
    trip: Trip,
    documents: List<DocItem>,                 // already filtered to this trip
    isLoading: Boolean = false,               // ⬅ ADDED
    onBack: () -> Unit,
    onOpenDoc: (String) -> Unit,
    onUpload: suspend (PickedFile, title: String, category: String, legId: String, segmentIndex: Int, travellerId: String) -> Boolean,
    onMessage: (String) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onUpdateDocument: (String, String, String, String, Int, String) -> Unit,
    canEdit: Boolean = true,
) {
    val s = LocalStrings.current
    val filePicker = rememberFilePicker()
    val scope = rememberCoroutineScope()

    var pickedFile by remember { mutableStateOf<PickedFile?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var showChoiceMenu by remember { mutableStateOf(false) }
    var editingDoc by remember { mutableStateOf<DocItem?>(null) }
    var query by rememberSaveable { mutableStateOf("") }              // ⬅ ADD
    var activeCategory by rememberSaveable { mutableStateOf<String?>(null) }   // ⬅ ADD

    val imagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { bytes ->
            bytes.firstOrNull()?.let { b ->
                pickedFile = PickedFile(b, "image_${nowMillisDocs()}.jpg", "image/jpeg")
                showDialog = true
            }
        }
    )

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                title = s.documents,
                onBack = onBack,
                trailing = {
                    if (canEdit) {
                        IconButton(
                            enabled = !uploading,
                            onClick = { showChoiceMenu = true },
                        ) {
                            Icon(
                                Icons.Filled.Upload,
                                contentDescription = s.addDocument,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
            )

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PlaneLoader()
                    }
                }

                documents.isEmpty() -> {
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        // ⬅ CHANGED — was Text("📄"), which doesn't tint with the
                        // theme and renders differently across platforms.
                        Box(
                            Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            s.noDocuments,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            s.noDocumentsSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        )
                    }
                }

                else -> {
                    // ⬅ ADD — search + category filter. `category` and ALL_CATEGORIES
                    // already existed on the model; nothing consumed them.
                    val counts = remember(documents) {
                        documents.groupingBy { it.category.ifBlank { CAT_OTHER } }.eachCount()
                    }
                    val visible = remember(documents, query, activeCategory, trip.travellers) {
                        val q = query.trim()
                        documents.filter { doc ->
                            val catOk = activeCategory == null ||
                                    doc.category.ifBlank { CAT_OTHER } == activeCategory
                            val textOk = q.isBlank() ||
                                    doc.title.contains(q, ignoreCase = true) ||
                                    doc.traveller.contains(q, ignoreCase = true) ||
                                    trip.travellers.firstOrNull { it.id == doc.travellerId }
                                        ?.let { "${it.firstName} ${it.surname}".contains(q, ignoreCase = true) } == true
                            catOk && textOk
                        }
                    }

                    DocumentSearchField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    Spacer(Modifier.height(10.dp))

                    CategoryFilterRow(
                        counts = counts,
                        total = documents.size,
                        active = activeCategory,
                        onSelect = { activeCategory = it },
                    )

                    if (visible.isEmpty()) {
                        Column(
                            Modifier.fillMaxSize().padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(36.dp),
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                s.noMatchingDocuments,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(11.dp),
                            verticalArrangement = Arrangement.spacedBy(11.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                        ) {
                            items(visible, key = { it.id }) { doc ->
                                var showMenu by remember { mutableStateOf(false) }

                                val travellerName = remember(doc.traveller, doc.travellerId, trip.travellers) {
                                    if (doc.traveller.isNotBlank()) doc.traveller
                                    else trip.travellers.find { it.id == doc.travellerId }
                                        ?.let { "${it.firstName} ${it.surname}".trim() }
                                        .orEmpty()
                                }

                                Box {
                                    DocumentCard(
                                        doc = doc,
                                        subtitle = travellerName.ifBlank { categoryLabel(doc.category, s) },
                                        onClick = { onOpenDoc(doc.id) },
                                        onLongClick = { if (canEdit) showMenu = true },
                                    )

                                    MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp))) {
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false },
                                            offset = DpOffset(x = 40.dp, y = (-40).dp),
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(s.edit) },
                                                leadingIcon = { Icon(Icons.Filled.Edit, null) },
                                                onClick = {
                                                    showMenu = false
                                                    editingDoc = doc
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text(s.delete, color = Color(0xFFE03131)) },
                                                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFE03131)) },
                                                onClick = {
                                                    showMenu = false
                                                    pendingDeleteId = doc.id
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

        // Choice menu overlay
        if (showChoiceMenu) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { showChoiceMenu = false },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.fillMaxWidth().clickable(
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
                            s.addDocument,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text(s.uploadFromGallery) },
                            leadingContent = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                            modifier = Modifier.clickable {
                                showChoiceMenu = false
                                imagePicker.launch()
                            },
                        )
                        ListItem(
                            headlineContent = { Text(s.uploadFromFiles) },
                            leadingContent = { Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null) },
                            modifier = Modifier.clickable {
                                showChoiceMenu = false
                                scope.launch {
                                    val file = filePicker.pickFile()
                                    if (file != null) {
                                        pickedFile = file
                                        showDialog = true
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }

        // Upload spinner overlay
        if (uploading) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 4.dp) {
                    Column(
                        Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PlaneLoader(size = 72.dp)
                        Spacer(Modifier.height(14.dp))
                        Text(s.uploading, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    if (showDialog && pickedFile != null) {

        fun String.toTitleCase(): String =
            split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { c ->
                    if (c.isLowerCase()) c.titlecase() else c.toString()
                }
            }

        AddDocumentDialog(
            file = pickedFile!!,
            legs = trip.legs,
            travellers = trip.travellers,
            onDismiss = { showDialog = false; pickedFile = null },
            onConfirm = { title, category, legId, segmentIndex, travellerId ->
                val f = pickedFile!!
                showDialog = false
                pickedFile = null
                scope.launch {
                    uploading = true
                    val ok = onUpload(f, title.toTitleCase(), category, legId, segmentIndex, travellerId)
                    uploading = false
                    if (!ok) onMessage(s.uploadFailed)
                }
            },
        )
    }

    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(s.deleteDocumentQ) },
            text = { Text(s.cantBeUndone) },
            confirmButton = {
                TextButton(onClick = { onDeleteDocument(pendingDeleteId!!); pendingDeleteId = null }) {
                    Text(s.delete, color = Color(0xFFE03131))
                }
            },
            dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text(s.cancel) } },
        )
    }

    if (editingDoc != null) {
        EditDocumentDialog(
            doc = editingDoc!!,
            legs = trip.legs,
            travellers = trip.travellers,
            onDismiss = { editingDoc = null },
            onConfirm = { title, category, legId, segmentIndex, travellerId ->
                val docId = editingDoc?.id ?: return@EditDocumentDialog
                editingDoc = null
                onUpdateDocument(docId, title, category, legId, segmentIndex, travellerId)
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search, filter, card
// ─────────────────────────────────────────────────────────────────────────────

/** Accent per category — replaces repeating the word "Transport" on every card. */
private fun categoryColor(category: String): Color = when (category) {
    CAT_TRANSPORT -> Color(0xFFBA7517)
    CAT_ACCOMMODATION -> Color(0xFF7F77DD)
    CAT_ATTRACTION -> Color(0xFFD85A30)
    else -> Color(0xFF888780)
}

/** "PDF" / "JPG" badge, since the file-type icon is hidden behind a thumbnail. */
private fun formatBadge(mimeType: String, fileName: String): String {
    val ext = fileName.substringAfterLast('.', "").uppercase()
    if (ext.isNotBlank() && ext.length <= 4) return ext
    return when {
        mimeType.contains("pdf", ignoreCase = true) -> "PDF"
        mimeType.startsWith("image", ignoreCase = true) -> "IMG"
        else -> "FILE"
    }
}

@Composable
private fun DocumentSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = onSurface.copy(alpha = 0.06f),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = onSurface.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        s.searchDocuments,
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
                    Icons.Filled.Close,
                    contentDescription = s.clear,
                    tint = onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onValueChange("") },
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    counts: Map<String, Int>,
    total: Int,
    active: String?,
    onSelect: (String?) -> Unit,
) {
    val s = LocalStrings.current
    // Categories with nothing in them are hidden rather than offering a chip
    // that leads to an empty grid.
    val shown = remember(counts) { ALL_CATEGORIES.filter { (counts[it] ?: 0) > 0 } }
    val listState = rememberLazyListState()

    // ⬅ ADD — bring the selected chip into view. This was a plain Row with
    // horizontalScroll, so the offset survived the selection change: scrolling
    // right to reach "Attraction" and then tapping "All" left "All" half
    // off-screen on the left.
    val selectedIndex = if (active == null) 0
    else shown.indexOf(active).let { if (it < 0) 0 else it + 1 }
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item(key = "all") {
            FilterChipPill(
                label = s.all,
                count = total,
                selected = active == null,
                dotColor = null,
                onClick = { onSelect(null) },
            )
        }
        shown.forEach { cat ->
            item(key = cat) {
                FilterChipPill(
                    label = categoryLabel(cat, s),
                    count = counts[cat] ?: 0,
                    selected = active == cat,
                    dotColor = categoryColor(cat),
                    onClick = { onSelect(if (active == cat) null else cat) },
                )
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    count: Int,
    selected: Boolean,
    dotColor: Color?,
    onClick: () -> Unit,
) {
    // ⬅ CHANGED — was a solid primary fill when selected, which turned every
    // chip blue regardless of category. Mirrors CategoryMeter in
    // TripExpensesScreen: tint and border in the category's own colour, dot
    // always visible, label stays onSurface.
    val accent = dotColor ?: MaterialTheme.colorScheme.onSurface
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) accent.copy(alpha = 0.14f) else Color.Transparent,
        border = BorderStroke(
            if (selected) 1.dp else 0.5.dp,
            if (selected) accent.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        ),
        modifier = Modifier.clickable { onClick() },
    ) {
        Row(
            Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (dotColor != null) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (selected) 1f else 0.8f),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                "$count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}

/**
 * Grid tile. The thumbnail area is a fixed height and the title is one line, so
 * every card is the same size — previously a card with a traveller name was a
 * line taller, and LazyVerticalGrid sizes each row to its tallest item, which
 * left ragged gaps.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DocumentCard(
    doc: DocItem,
    subtitle: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val isImage = doc.mimeType.startsWith("image", ignoreCase = true)
    // ⬅ CHANGED — images preview from fileUrl directly; PDFs preview from a
    // thumbUrl rendered once at upload time. Blank on both = generic icon.
    val previewUrl = doc.thumbUrl.ifBlank { if (isImage) doc.fileUrl else "" }
    val onSurface = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = CardShape,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(onSurface.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center,
            ) {
                // ⬅ ADD — real thumbnails for image documents. fileUrl was already
                // on the model; every image was rendering as the same generic icon.
                if (previewUrl.isNotBlank()) {
                    AsyncImage(
                        model = previewUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        docIcon(doc.mimeType),
                        contentDescription = null,
                        tint = docColor(doc.mimeType),
                        modifier = Modifier.size(30.dp),
                    )
                }

                // Category accent, top-left.
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(7.dp)
                        .size(width = 4.dp, height = 24.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(categoryColor(doc.category.ifBlank { CAT_OTHER })),
                )

                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(7.dp),
                ) {
                    Text(
                        formatBadge(doc.mimeType, doc.fileName),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp)) {
                Text(
                    doc.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDocumentDialog(
    file: PickedFile,
    legs: List<Leg>,
    travellers: List<Traveller>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, legId: String, segmentIndex: Int, travellerId: String) -> Unit,
) {
    val s = LocalStrings.current
    var title by remember { mutableStateOf(nameWithoutExtension(file.fileName)) }
    var category by remember { mutableStateOf(CAT_OTHER) }
    var menuOpen by remember { mutableStateOf(false) }

    // Leg attachment (optional). "" = not attached to any leg.
    var legId by remember { mutableStateOf("") }
    var legMenuOpen by remember { mutableStateOf(false) }
    var segmentIndex by remember { mutableStateOf(-1) }
    var segMenuOpen by remember { mutableStateOf(false) }
    var travellerId by remember { mutableStateOf("") }
    var travMenuOpen by remember { mutableStateOf(false) }
    fun legLabel(id: String): String =
        legs.firstOrNull { it.id == id }?.let { "${it.fromCity} → ${it.toCity}" } ?: s.attachToNone

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.addDocument) },
        shape = RoundedCornerShape(16.dp),
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(docIcon(file.mimeType), null, tint = docColor(file.mimeType), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        file.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.toTitleCase() },
                    label = { Text(s.title) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(12.dp))

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = menuOpen,
                    onExpandedChange = { menuOpen = it },
                ) {
                    OutlinedTextField(
                        value = categoryLabel(category, s),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(s.category) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        ALL_CATEGORIES.forEach { value ->
                            DropdownMenuItem(
                                text = { Text(categoryLabel(value, s)) },
                                onClick = { category = value; menuOpen = false },
                            )
                        }
                    }
                }

                // Attach-to-leg dropdown (only shown if the trip has legs)
                if (legs.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    ExposedDropdownMenuBox(
                        expanded = legMenuOpen,
                        onExpandedChange = { legMenuOpen = it },
                    ) {
                        OutlinedTextField(
                            value = legLabel(legId),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(s.attachToLeg) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = legMenuOpen) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = legMenuOpen,
                            onDismissRequest = { legMenuOpen = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            // "None" option
                            DropdownMenuItem(
                                text = { Text(s.attachToNone) },
                                onClick = { legId = ""; segmentIndex = -1; travellerId = ""; legMenuOpen = false },
                            )
                            legs.sortedWith(compareBy({ it.date }, { parseHourMinute(it.timeLabel).first }, { parseHourMinute(it.timeLabel).second }))
                                .forEach { leg ->
                                    DropdownMenuItem(
                                        text = { Text("${leg.fromCity} → ${leg.toCity}") },
                                        onClick = { legId = leg.id; segmentIndex = -1; travellerId = ""; legMenuOpen = false },
                                    )
                                }
                        }
                    }
                }

                // Segment picker — only when the chosen leg has layover stops
                val segLeg = legs.firstOrNull { it.id == legId }
                val segOptions = segLeg?.let { segmentOptions(it) } ?: emptyList()
                if (segOptions.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    ExposedDropdownMenuBox(
                        expanded = segMenuOpen,
                        onExpandedChange = { segMenuOpen = it },
                    ) {
                        OutlinedTextField(
                            value = segOptions.firstOrNull { it.first == segmentIndex }?.second ?: s.wholeJourney,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(s.segment) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = segMenuOpen) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = segMenuOpen,
                            onDismissRequest = { segMenuOpen = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            DropdownMenuItem(
                                text = { Text(s.wholeJourney) },
                                onClick = { segmentIndex = -1; segMenuOpen = false },
                            )
                            segOptions.forEach { (i, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = { segmentIndex = i; segMenuOpen = false },
                                )
                            }
                        }
                    }
                }

                // Traveller picker — whose ticket this is (shown when the leg has travellers)
                val legTravs = legs.firstOrNull { it.id == legId }?.let { lg ->
                    travellers.filter { it.id in lg.travellerIds }
                } ?: emptyList()
                if (legTravs.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    ExposedDropdownMenuBox(
                        expanded = travMenuOpen,
                        onExpandedChange = { travMenuOpen = it },
                    ) {
                        OutlinedTextField(
                            value = legTravs.firstOrNull { it.id == travellerId }
                                ?.let { "${it.firstName} ${it.surname}".trim() } ?: s.anyone,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(s.travellerSingular) },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = travMenuOpen) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = travMenuOpen,
                            onDismissRequest = { travMenuOpen = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            DropdownMenuItem(
                                text = { Text(s.anyone) },
                                onClick = { travellerId = ""; travMenuOpen = false },
                            )
                            legTravs.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text("${t.firstName} ${t.surname}".trim()) },
                                    onClick = { travellerId = t.id; travMenuOpen = false },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), category, legId, segmentIndex, travellerId) },
                enabled = title.isNotBlank(),
            ) { Text(s.add) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDocumentDialog(
    doc: DocItem,
    legs: List<Leg>,
    travellers: List<Traveller>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, legId: String, segmentIndex: Int, travellerId: String) -> Unit,
) {
    val s = LocalStrings.current
    var title by remember { mutableStateOf(doc.title) }
    var category by remember { mutableStateOf(doc.category) }
    var menuOpen by remember { mutableStateOf(false) }

    var legId by remember { mutableStateOf(doc.legId) }
    var legMenuOpen by remember { mutableStateOf(false) }
    var segmentIndex by remember { mutableStateOf(doc.segmentIndex) }
    var segMenuOpen by remember { mutableStateOf(false) }
    var travellerId by remember { mutableStateOf(doc.travellerId) }
    var travMenuOpen by remember { mutableStateOf(false) }
    fun legLabel(id: String): String =
        legs.firstOrNull { it.id == id }?.let { "${it.fromCity} → ${it.toCity}" } ?: s.attachToNone

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.edit) },
        shape = RoundedCornerShape(16.dp),
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.toTitleCase() },
                    label = { Text(s.title) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = menuOpen,
                    onExpandedChange = { menuOpen = it },
                ) {
                    OutlinedTextField(
                        value = categoryLabel(category, s),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(s.category) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        ALL_CATEGORIES.forEach { value ->
                            DropdownMenuItem(
                                text = { Text(categoryLabel(value, s)) },
                                onClick = { category = value; menuOpen = false },
                            )
                        }
                    }
                }

                if (legs.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    ExposedDropdownMenuBox(
                        expanded = legMenuOpen,
                        onExpandedChange = { legMenuOpen = it },
                    ) {
                        OutlinedTextField(
                            value = legLabel(legId),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(s.attachToLeg) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = legMenuOpen) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = legMenuOpen,
                            onDismissRequest = { legMenuOpen = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            DropdownMenuItem(
                                text = { Text(s.attachToNone) },
                                onClick = { legId = ""; segmentIndex = -1; travellerId = ""; legMenuOpen = false },
                            )
                            legs.sortedWith(compareBy({ it.date }, { parseHourMinute(it.timeLabel).first }, { parseHourMinute(it.timeLabel).second }))
                                .forEach { leg ->
                                    DropdownMenuItem(
                                        text = { Text("${leg.fromCity} → ${leg.toCity}") },
                                        onClick = { legId = leg.id; segmentIndex = -1; travellerId = ""; legMenuOpen = false },
                                    )
                                }
                        }
                    }
                }

                // Segment picker — only when the chosen leg has layover stops
                val segLeg = legs.firstOrNull { it.id == legId }
                val segOptions = segLeg?.let { segmentOptions(it) } ?: emptyList()
                if (segOptions.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    ExposedDropdownMenuBox(
                        expanded = segMenuOpen,
                        onExpandedChange = { segMenuOpen = it },
                    ) {
                        OutlinedTextField(
                            value = segOptions.firstOrNull { it.first == segmentIndex }?.second ?: s.wholeJourney,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(s.segment) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = segMenuOpen) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = segMenuOpen,
                            onDismissRequest = { segMenuOpen = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            DropdownMenuItem(
                                text = { Text(s.wholeJourney) },
                                onClick = { segmentIndex = -1; segMenuOpen = false },
                            )
                            segOptions.forEach { (i, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = { segmentIndex = i; segMenuOpen = false },
                                )
                            }
                        }
                    }
                }

                // Traveller picker — whose ticket this is (shown when the leg has travellers)
                val legTravs = legs.firstOrNull { it.id == legId }?.let { lg ->
                    travellers.filter { it.id in lg.travellerIds }
                } ?: emptyList()
                if (legTravs.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    ExposedDropdownMenuBox(
                        expanded = travMenuOpen,
                        onExpandedChange = { travMenuOpen = it },
                    ) {
                        OutlinedTextField(
                            value = legTravs.firstOrNull { it.id == travellerId }
                                ?.let { "${it.firstName} ${it.surname}".trim() } ?: s.anyone,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(s.travellerSingular) },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = travMenuOpen) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = travMenuOpen,
                            onDismissRequest = { travMenuOpen = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            DropdownMenuItem(
                                text = { Text(s.anyone) },
                                onClick = { travellerId = ""; travMenuOpen = false },
                            )
                            legTravs.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text("${t.firstName} ${t.surname}".trim()) },
                                    onClick = { travellerId = t.id; travMenuOpen = false },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), category, legId, segmentIndex, travellerId) },
                enabled = title.isNotBlank(),
            ) { Text(s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}