package com.itinera.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.itinera.app.data.BarcodeExtraction

/**
 * Full-screen scannable ticket code: white background (contrast for gate scanners),
 * max screen brightness while open, and an escape hatch to view the full file.
 */
@Composable
fun TicketBarcodeDialog(
    title: String,
    extraction: BarcodeExtraction,
    onOpenFullTicket: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        KeepMaxBrightness()

        Box(Modifier.fillMaxSize().background(Color.White)) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111111),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                val codeAspect = extraction.image.width.toFloat() /
                        extraction.image.height.toFloat().coerceAtLeast(1f)
                Image(
                    bitmap = extraction.image,
                    contentDescription = "Ticket code",
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .aspectRatio(codeAspect),
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None,   // nearest-neighbour: crisp modules, no blur
                )
                Spacer(Modifier.height(28.dp))
                TextButton(onClick = { onDismiss(); onOpenFullTicket() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.InsertDriveFile, null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("View full ticket")
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .background(Color(0xFF111111).copy(alpha = 0.08f), CircleShape),
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF111111))
            }
        }
    }
}