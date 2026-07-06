package com.itinera.app.data

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

actual fun ImageBitmap.toPngBytes(): ByteArray {
    val out = ByteArrayOutputStream()
    asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
    return out.toByteArray()
}