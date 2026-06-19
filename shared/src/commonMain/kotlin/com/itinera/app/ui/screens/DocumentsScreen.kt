package com.itinera.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itinera.app.data.PickedFile
import com.itinera.app.data.rememberFilePicker
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.i18n.Strings
import com.itinera.app.model.DocItem
import com.itinera.app.model.Trip
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.EmptyState
import com.itinera.app.ui.components.TopBar
import kotlinx.coroutines.launch

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

private fun nameWithoutExtension(fileName: String): String =
    fileName.substringBeforeLast('.', fileName)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentsScreen(
    trip: Trip,
    documents: List<DocItem>,                 // already filtered to this trip
    onBack: () -> Unit,
    onOpenDoc: (String) -> Unit,
    onUpload: suspend (PickedFile, title: String, category: String) -> Boolean,
    onDeleteDocument: (String) -> Unit,
    onMessage: (String) -> Unit,
) {
    val s = LocalStrings.current
    val filePicker = rememberFilePicker()
    val scope = rememberCoroutineScope()

    var pickedFile by remember { mutableStateOf<PickedFile?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                title = s.documents,
                onBack = onBack,
                trailing = {
                    IconButton(
                        enabled = !uploading,
                        onClick = {
                            scope.launch {
                                val file = filePicker.pickFile()
                                if (file != null) {
                                    pickedFile = file
                                    showDialog = true
                                }
                            }
                        },
                    ) {
                        Icon(Icons.Filled.Upload, contentDescription = s.addDocument, tint = MaterialTheme.colorScheme.primary)
                    }
                },
            )

            if (documents.isEmpty()) {
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                    title = s.noDocuments,
                    subtitle = s.noDocumentsSubtitle,
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    items(documents, key = { it.id }) { doc ->
                        Surface(
                            modifier = Modifier.combinedClickable(
                                onClick = { onOpenDoc(doc.id) },
                                onLongClick = { pendingDeleteId = doc.id },
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
                                Text(
                                    categoryLabel(doc.category, s),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
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
                        CircularProgressIndicator()
                        Spacer(Modifier.height(14.dp))
                        Text(s.uploading, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    if (showDialog && pickedFile != null) {
        AddDocumentDialog(
            file = pickedFile!!,
            onDismiss = { showDialog = false; pickedFile = null },
            onConfirm = { title, category ->
                val file = pickedFile!!
                showDialog = false
                scope.launch {
                    uploading = true
                    val ok = onUpload(file, title, category)
                    uploading = false
                    pickedFile = null
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDocumentDialog(
    file: PickedFile,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String) -> Unit,
) {
    val s = LocalStrings.current
    var title by remember { mutableStateOf(nameWithoutExtension(file.fileName)) }   // default to file name
    var category by remember { mutableStateOf(CAT_OTHER) }
    var menuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.addDocument) },
        shape = RoundedCornerShape(16.dp),
        text = {
            Column {
                // show the picked file name
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
                    onValueChange = { title = it },
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
                    ) {
                        ALL_CATEGORIES.forEach { value ->
                            DropdownMenuItem(
                                text = { Text(categoryLabel(value, s)) },
                                onClick = { category = value; menuOpen = false },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), category) },
                enabled = title.isNotBlank(),
            ) { Text(s.add) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}