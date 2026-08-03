package com.itinera.app.data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.EncodedImageFormat

actual fun ImageBitmap.toPngBytes(): ByteArray {
    val skiaImage = Image.makeFromBitmap(this.asSkiaBitmap())
    val data = skiaImage.encodeToData(EncodedImageFormat.PNG)
        ?: error("PNG encode failed")
    return data.bytes
}