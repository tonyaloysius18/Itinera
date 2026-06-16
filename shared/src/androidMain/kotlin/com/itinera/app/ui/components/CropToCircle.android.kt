package com.itinera.app.ui.components

import android.graphics.*
import java.io.ByteArrayOutputStream

actual fun cropToCircle(bytes: ByteArray, size: Int): ByteArray {
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val paint = Paint().apply {
        isAntiAlias = true
    }

    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

    val srcRect = if (bitmap.width > bitmap.height) {
        val left = (bitmap.width - bitmap.height) / 2
        Rect(left, 0, left + bitmap.height, bitmap.height)
    } else {
        val top = (bitmap.height - bitmap.width) / 2
        Rect(0, top, bitmap.width, top + bitmap.width)
    }

    val destRect = Rect(0, 0, size, size)
    canvas.drawBitmap(bitmap, srcRect, destRect, paint)

    val stream = ByteArrayOutputStream()
    output.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}
