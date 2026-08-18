package com.itinera.app.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

actual fun compressFeedbackImage(
    bytes: ByteArray,
    maxDimension: Int,
    quality: Int,
): ByteArray {
    var source = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
    source = applyFeedbackExifRotation(source, bytes)

    val longest = maxOf(source.width, source.height)
    val scale = if (longest > maxDimension) maxDimension.toFloat() / longest else 1f
    val width = (source.width * scale).roundToInt().coerceAtLeast(1)
    val height = (source.height * scale).roundToInt().coerceAtLeast(1)
    val output = if (width == source.width && height == source.height) {
        source
    } else {
        Bitmap.createScaledBitmap(source, width, height, true)
    }

    return ByteArrayOutputStream().also {
        output.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(50, 95), it)
    }.toByteArray()
}

private fun applyFeedbackExifRotation(bitmap: Bitmap, bytes: ByteArray): Bitmap = try {
    val degrees = when (
        ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> return bitmap
    }
    Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        Matrix().apply { postRotate(degrees) },
        true,
    )
} catch (_: Exception) {
    bitmap
}
