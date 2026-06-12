package com.itinera.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.DocItem
import com.itinera.app.model.DocType
import com.itinera.app.ui.components.CardShape
import com.itinera.app.ui.components.TopBar

@Composable
fun DocumentsScreen(documents: List<DocItem>, onOpenDoc: (String) -> Unit) {
    val s = LocalStrings.current
    Column(Modifier.fillMaxSize()) {
        TopBar(s.tickets, trailing = {
            Icon(Icons.Filled.Upload, contentDescription = "Upload", tint = MaterialTheme.colorScheme.primary)
        })
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            items(documents, key = { it.id }) { doc ->
                Surface(
                    modifier = Modifier.clickable { onOpenDoc(doc.id) },
                    shape = CardShape,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (doc.type == DocType.PDF) {
                            Icon(Icons.Filled.PictureAsPdf, null, tint = Color(0xFFA32D2D), modifier = Modifier.size(34.dp))
                        } else {
                            Icon(Icons.Filled.Image, null, tint = Color(0xFF378ADD), modifier = Modifier.size(34.dp))
                        }
                        Spacer(Modifier.height(9.dp))
                        Text(doc.title, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        Text(doc.attachedToLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}
