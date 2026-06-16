package com.itinera.app.ui.components

import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.UIKit.*
import platform.Foundation.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual fun cropToCircle(bytes: ByteArray, size: Int): ByteArray {
    val data = bytes.toNSData()
    val image = UIImage(data = data) ?: return bytes
    
    val side = size.toDouble()
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(side, side), false, 1.0)
    
    val rect = CGRectMake(0.0, 0.0, side, side)
    UIBezierPath.bezierPathWithOvalInRect(rect).addClip()
    
    val width = image.size.useContents { width }
    val height = image.size.useContents { height }
    
    val drawRect = if (width > height) {
        val scale = side / height
        val xOffset = (width * scale - side) / 2.0
        CGRectMake(-xOffset, 0.0, width * scale, side)
    } else {
        val scale = side / width
        val yOffset = (height * scale - side) / 2.0
        CGRectMake(0.0, -yOffset, side, height * scale)
    }
    
    image.drawInRect(drawRect)
    
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
