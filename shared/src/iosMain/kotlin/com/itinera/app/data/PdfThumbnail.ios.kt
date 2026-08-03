package com.itinera.app.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.PDFKit.PDFDisplayBox            // typealias for NSInteger
import platform.PDFKit.PDFDocument
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy

/**
 * PDFKit ships with iOS; no SPM dependency needed. If the linker can't find it:
 *
 *     iosTarget.binaries.framework { linkerOpts("-framework", "PDFKit") }
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun renderPdfFirstPage(bytes: ByteArray, targetWidthPx: Int): ByteArray? =
    withContext(Dispatchers.Default) {
        try {
            val data = bytes.toNSData() ?: return@withContext null
            val doc = PDFDocument(data) ?: return@withContext null
            if (doc.pageCount.toInt() == 0) return@withContext null
            val page = doc.pageAtIndex(0u) ?: return@withContext null

            // PDFDisplayBox is a typealias for NSInteger here, not an enum class.
            // Header values: 0 media, 1 crop, 2 bleed, 3 trim, 4 art.
            val box: PDFDisplayBox = 0L

            // boundsForBox returns CValue<CGRect>; fields readable only in useContents.
            val ratio = page.boundsForBox(box).useContents {
                if (size.height > 0.0) size.width / size.height else 0.75
            }
            val h = (targetWidthPx / ratio).toInt().coerceAtLeast(1)

            val uiImage = page.thumbnailOfSize(
                CGSizeMake(targetWidthPx.toDouble(), h.toDouble()),
                box,
            )
            // Already PNG data — no skia round-trip needed now that we return bytes.
            UIImagePNGRepresentation(uiImage)?.toByteArray()
        } catch (_: Exception) {
            null
        }
    }

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData? {
    if (isEmpty()) return null
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val out = ByteArray(size)
    if (size > 0) {
        out.usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    }
    return out
}