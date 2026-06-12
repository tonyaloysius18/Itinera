package com.itinera.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itinera.app.model.DocItem
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.TopBar

/**
 * Placeholder viewer. Real PDF/image rendering is platform-specific: use an
 * expect/actual that calls Android PdfRenderer and iOS PDFKit. The grey panel
 * stands in for the rendered page.
 */
@Composable
fun DocumentViewerScreen(doc: DocItem, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = doc.title,
            onBack = onBack,
            trailing = { Icon(Icons.Filled.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary) },
        )
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
                Icon(Icons.Filled.QrCode2, contentDescription = null, modifier = Modifier.size(96.dp))
                Spacer(Modifier.height(12.dp))
                Text(doc.title, style = MaterialTheme.typography.titleMedium)
                Text("(${doc.type.name})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}
