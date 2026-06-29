package com.itinera.app.ui.components

import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.UIKit.*
import platform.Foundation.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual fun cropToRect(
    bytes: ByteArray,
    left: Float, top: Float, right: Float, bottom: Float,
    outWidth: Int, outHeight: Int,
): ByteArray {
    val image = UIImage(data = bytes.toNSDataRect()) ?: return bytes

    val (iw, ih) = image.size.useContents { width to height }
    if (iw <= 0.0 || ih <= 0.0) return bytes

    val cropX = left * iw
    val cropY = top * ih
    val cropW = (right - left) * iw
    val cropH = (bottom - top) * ih
    if (cropW <= 0.0 || cropH <= 0.0) return bytes

    val ow = outWidth.toDouble()
    val oh = outHeight.toDouble()
    val sx = ow / cropW
    val sy = oh / cropH

    val format = UIGraphicsImageRendererFormat.defaultFormat().apply {
        opaque = true
        scale = 1.0
    }
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(ow, oh), format = format)
    val result = renderer.imageWithActions { _ ->
        image.drawInRect(CGRectMake(-cropX * sx, -cropY * sy, iw * sx, ih * sy))
    }

    return UIImageJPEGRepresentation(result, 0.9)?.toByteArrayRect() ?: bytes
}

// file-private helpers (named *Rect to avoid clashing with the cropToCircle file's helpers)
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSDataRect(): NSData = usePinned {
    NSData.dataWithBytes(it.addressOf(0), it.get().size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArrayRect(): ByteArray = ByteArray(length.toInt()).apply {
    usePinned {
        memcpy(it.addressOf(0), bytes, length)
    }
}