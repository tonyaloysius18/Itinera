package com.itinera.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

actual fun cropToCircle(
    bytes: ByteArray,
    left: Float, top: Float, right: Float, bottom: Float,
    size: Int
): ByteArray {
    var src = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
    src = applyExifRotation(src, bytes)

    val x = (left * src.width).toInt().coerceIn(0, src.width - 1)
    val y = (top * src.height).toInt().coerceIn(0, src.height - 1)
    val w = ((right - left) * src.width).toInt().coerceIn(1, src.width - x)
    val h = ((bottom - top) * src.height).toInt().coerceIn(1, src.height - y)

    val sub = Bitmap.createBitmap(src, x, y, w, h)
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val paint = Paint().apply {
        isAntiAlias = true
    }

    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

    val destRect = Rect(0, 0, size, size)
    canvas.drawBitmap(sub, null, destRect, paint)

    val stream = ByteArrayOutputStream()
    output.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

private fun applyExifRotation(bmp: Bitmap, bytes: ByteArray): Bitmap = try {
    val exif = ExifInterface(ByteArrayInputStream(bytes))
    val deg = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> return bmp
    }
    Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(deg) }, true)
} catch (e: Exception) { bmp }
