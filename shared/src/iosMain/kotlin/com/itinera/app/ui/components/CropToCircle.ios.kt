package com.itinera.app.ui.components

import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.UIKit.*
import platform.Foundation.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual fun cropToCircle(
    bytes: ByteArray,
    left: Float, top: Float, right: Float, bottom: Float,
    size: Int
): ByteArray {
    val data = bytes.toNSData()
    val fullImage = UIImage(data = data) ?: return bytes
    
    val iw = fullImage.size.useContents { width }
    val ih = fullImage.size.useContents { height }
    
    val x = (left * iw)
    val y = (top * ih)
    val w = ((right - left) * iw)
    val h = ((bottom - top) * ih)
    
    val cropRect = CGRectMake(x, y, w, h)
    val cgImage = fullImage.CGImage?.let { CGImageCreateWithImageInRect(it, cropRect) } ?: return bytes
    val image = UIImage.imageWithCGImage(cgImage)
    
    val side = size.toDouble()
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(side, side), false, 1.0)
    
    val rect = CGRectMake(0.0, 0.0, side, side)
    UIBezierPath.bezierPathWithOvalInRect(rect).addClip()
    
    image.drawInRect(rect)
    
    val resultImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    
    val resultData = resultImage?.let { UIImagePNGRepresentation(it) } ?: return bytes
    return resultData.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = usePinned {
    NSData.dataWithBytes(it.addressOf(0), it.get().size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    usePinned {
        memcpy(it.addressOf(0), bytes, length)
    }
}
