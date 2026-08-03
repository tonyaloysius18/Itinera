package com.itinera.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

actual fun cropToRect(
    bytes: ByteArray,
    left: Float, top: Float, right: Float, bottom: Float,
    outWidth: Int, outHeight: Int,
): ByteArray {
    var src = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
    src = applyExifRotation(src, bytes)   // match Coil's oriented intrinsicSize

    val x = (left * src.width).toInt().coerceIn(0, src.width - 1)
    val y = (top * src.height).toInt().coerceIn(0, src.height - 1)
    val w = ((right - left) * src.width).toInt().coerceIn(1, src.width - x)
    val h = ((bottom - top) * src.height).toInt().coerceIn(1, src.height - y)

    val sub = Bitmap.createBitmap(src, x, y, w, h)
    val scaled = Bitmap.createScaledBitmap(sub, outWidth, outHeight, true)
    return ByteArrayOutputStream().also { scaled.compress(Bitmap.CompressFormat.JPEG, 90, it) }.toByteArray()
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