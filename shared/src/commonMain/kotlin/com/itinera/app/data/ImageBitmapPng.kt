package com.itinera.app.data

import androidx.compose.ui.graphics.ImageBitmap

/** Encodes an ImageBitmap to PNG bytes. Implemented per-platform. */
expect fun ImageBitmap.toPngBytes(): ByteArray