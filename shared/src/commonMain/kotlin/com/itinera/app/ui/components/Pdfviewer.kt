package com.itinera.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Renders a PDF (given its raw bytes) using the platform-native viewer. */
@Composable
expect fun PdfViewer(bytes: ByteArray, modifier: Modifier = Modifier)