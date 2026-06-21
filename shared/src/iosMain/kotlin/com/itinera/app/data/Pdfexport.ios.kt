package com.itinera.app.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
//import platform.Foundation.NSStringDrawingUsesFontLeading
//import platform.Foundation.NSStringDrawingUsesLineFragmentOrigin
//import platform.Foundation.create
import platform.UIKit.NSFontAttributeName
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.Foundation.NSString
//import platform.Foundation.boundingRectWithSize
//import platform.Foundation.drawAtPoint
//import platform.Foundation.drawInRect
import platform.UIKit.NSStringDrawingUsesFontLeading
import platform.UIKit.NSStringDrawingUsesLineFragmentOrigin
import platform.UIKit.boundingRectWithSize
import platform.UIKit.drawAtPoint
import platform.UIKit.drawInRect
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual fun buildItineraryPdf(title: String, lines: List<String>): ByteArray {
    val pageWidth = 595.0
    val pageHeight = 842.0
    val margin = 40.0
    val lineHeight = 16.0
    val textWidth = pageWidth - margin * 2

    val data = NSMutableData()
    val bounds = CGRectMake(0.0, 0.0, pageWidth, pageHeight)

    UIGraphicsBeginPDFContextToData(data, bounds, null)

    val titleFont = UIFont.boldSystemFontOfSize(18.0)
    val bodyFont = UIFont.systemFontOfSize(11.0)
    val titleAttrs = mapOf<Any?, Any?>(NSFontAttributeName to titleFont)
    val bodyAttrs = mapOf<Any?, Any?>(NSFontAttributeName to bodyFont)

    UIGraphicsBeginPDFPage()
    var y = margin

    @Suppress("CAST_NEVER_SUCCEEDS")
    (title as NSString).drawAtPoint(CGPointMake(margin, y), titleAttrs)
    y += lineHeight * 2.0

    for (raw in lines) {
        @Suppress("CAST_NEVER_SUCCEEDS")
        val ns = raw as NSString
        val rect = ns.boundingRectWithSize(
            CGSizeMake(textWidth, Double.MAX_VALUE),
            options = NSStringDrawingUsesLineFragmentOrigin or NSStringDrawingUsesFontLeading,
            attributes = bodyAttrs,
            context = null,
        )
        val h = maxOf(CGRectGetHeight(rect), lineHeight)
        if (y + h > pageHeight - margin) {
            UIGraphicsBeginPDFPage()
            y = margin
        }
        ns.drawInRect(CGRectMake(margin, y, textWidth, h), bodyAttrs)
        y += h + 2.0
    }

    UIGraphicsEndPDFContext()
    return data.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val result = ByteArray(size)
    if (size > 0) {
        result.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }
    return result
}