package com.itinera.app.data

import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*
import platform.posix.memcpy
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class)
actual fun compressFeedbackImage(
    bytes: ByteArray,
    maxDimension: Int,
    quality: Int,
): ByteArray {
    if (bytes.isEmpty()) return bytes
    val image = UIImage(data = bytes.toFeedbackNSData()) ?: return bytes
    val (sourceWidth, sourceHeight) = image.size.useContents { width to height }
    if (sourceWidth <= 0.0 || sourceHeight <= 0.0) return bytes

    val longest = maxOf(sourceWidth, sourceHeight)
    val scale = if (longest > maxDimension) maxDimension / longest else 1.0
    val width = (sourceWidth * scale).roundToInt().coerceAtLeast(1).toDouble()
    val height = (sourceHeight * scale).roundToInt().coerceAtLeast(1).toDouble()

    val format = UIGraphicsImageRendererFormat.defaultFormat().apply {
        opaque = true
        this.scale = 1.0
    }
    val renderer = UIGraphicsImageRenderer(CGSizeMake(width, height), format)
    val resized = renderer.imageWithActions {
        image.drawInRect(CGRectMake(0.0, 0.0, width, height))
    }

    return UIImageJPEGRepresentation(resized, quality.coerceIn(50, 95) / 100.0)
        ?.toFeedbackByteArray()
        ?: bytes
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toFeedbackNSData(): NSData = usePinned {
    NSData.dataWithBytes(it.addressOf(0), size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toFeedbackByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
}
