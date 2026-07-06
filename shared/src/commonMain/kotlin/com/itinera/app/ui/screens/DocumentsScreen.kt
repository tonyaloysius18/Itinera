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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
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
                        Text("📄", style = MaterialTheme.typography.displayMedium)
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
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                    ) {
                        items(documents, key = { it.id }) { doc ->
                            var showMenu by remember { mutableStateOf(false) }

                            Box {
                                Surface(
                                    modifier = Modifier.combinedClickable(
                                        onClick = { onOpenDoc(doc.id) },
                                        onLongClick = { if (canEdit) showMenu = true },
                                    ),
                                    shape = CardShape,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                ) {
                                    Column(
                                        Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Icon(docIcon(doc.mimeType), null, tint = docColor(doc.mimeType), modifier = Modifier.size(34.dp))
                                        Spacer(Modifier.height(9.dp))
                                        Text(doc.title, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                                        val travellerName = remember(doc.traveller, doc.travellerId, trip.travellers) {
                                            if (doc.traveller.isNotBlank()) {
                                                doc.traveller
                                            } else {
                                                trip.travellers.find { it.id == doc.travellerId }?.let { "${it.firstName} ${it.surname}".trim() } ?: ""
                                            }
                                        }
                                        if (travellerName.isNotBlank()) {
                                            Text(travellerName, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                        Text(
                                            categoryLabel(doc.category, s),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }

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