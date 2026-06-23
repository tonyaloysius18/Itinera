package com.itinera.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itinera.app.data.rememberFileSharer
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.DocItem
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.PdfViewer
import com.itinera.app.ui.components.PlaneLoader
import com.itinera.app.ui.components.TopBar
import kotlinx.coroutines.launch

/**
 * Opens documents in-app:
 *  - images render inline via Coil
 *  - PDFs download then render via the native PdfViewer (PDFKit / PdfRenderer)
 *  - everything else (docx, ...) shows a card with an external "Open" fallback
 *
 * The top-bar action shares the actual file via the native share sheet.
 */
@Composable
fun DocumentViewerScreen(
    doc: DocItem,
    onBack: () -> Unit,
    onLoadBytes: suspend (url: String) -> ByteArray?,
    onMessage: (String) -> Unit,
) {
    val s = LocalStrings.current
    val uriHandler = LocalUriHandler.current
    val sharer = rememberFileSharer()
    val scope = rememberCoroutineScope()

    val isImage = doc.mimeType.startsWith("image", ignoreCase = true)
    val isPdf = doc.mimeType.contains("pdf", ignoreCase = true)

    var sharing by remember { mutableStateOf(false) }

    fun shareDoc() {
        if (doc.fileUrl.isBlank()) return
        scope.launch {
            sharing = true
            val bytes = onLoadBytes(doc.fileUrl)
            sharing = false
            if (bytes != null) sharer.share(bytes, doc.fileName, doc.mimeType)
            else onMessage(s.shareFailed)
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = doc.title,
            onBack = onBack,
            trailing = {
                if (doc.fileUrl.isNotBlank()) {
                    IconButton(enabled = !sharing, onClick = { shareDoc() }) {
                        if (sharing) {
                            //CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            PlaneLoader(Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Filled.Share, contentDescription = s.share, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
        )

        when {
            // ---- Image: inline ----
            isImage && doc.fileUrl.isNotBlank() -> {
                AsyncImage(
                    model = doc.fileUrl,
                    contentDescription = doc.title,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                )
            }

            // ---- PDF: download + native render ----
            isPdf && doc.fileUrl.isNotBlank() -> {
                var loading by remember { mutableStateOf(true) }
                var bytes by remember { mutableStateOf<ByteArray?>(null) }
                LaunchedEffect(doc.fileUrl) {
                    loading = true
                    bytes = onLoadBytes(doc.fileUrl)
                    loading = false
                }

                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    when {
                        loading -> PlaneLoader()
                        bytes != null -> PdfViewer(bytes!!, modifier = Modifier.fillMaxSize())
                        else -> Text(
                            s.fileNotUploaded,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                }
            }

            // ---- Other (docx, etc.): file card + external open (only viewer available) ----
            else -> {
                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null, modifier = Modifier.size(96.dp), tint = Color(0xFF8A8A8A))
                        Spacer(Modifier.height(12.dp))
                        Text(doc.title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        if (doc.fileName.isNotBlank()) {
                            Text(
                                doc.fileName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        if (doc.fileUrl.isNotBlank()) {
                            // docx can't render in-app; opening externally is the only way to view it
                            Button(onClick = { uriHandler.openUri(doc.fileUrl) }) {
                                Icon(Icons.Filled.OpenInNew, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(s.open)
                            }
                        } else {
                            Text(
                                s.fileNotUploaded,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }
    }
}