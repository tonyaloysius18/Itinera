package com.itinera.app.data

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.PDFKit.PDFDocument
import platform.PDFKit.kPDFDisplayBoxMediaBox
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIRectFill
import platform.Vision.VNBarcodeObservation
import platform.Vision.VNDetectBarcodesRequest
import platform.Vision.VNImageRequestHandler
import platform.posix.memcpy

/**
 * iOS barcode extraction (step 1b):
 *  - renderPdfFirstPage: PDFKit thumbnail of page 1 at 2x, composited onto white
 *    (PDF417 needs contrast; PDF pages are transparent by default).
 *  - detectBarcodesInImage: Vision VNDetectBarcodesRequest on the CGImage; reads
 *    QR / PDF417 / Aztec / Code128 etc. Vision's boundingBox is normalized with a
 *    BOTTOM-LEFT origin, so we flip Y into our top-left convention.
 *
 * Like cropToRect, call this from the Main dispatcher (UIKit rendering is used).
 * UIGraphicsImageRenderer is the thread-safe renderer (same as the crop fix).
 */

@OptIn(ExperimentalForeignApi::class)
actual suspend fun renderPdfFirstPage(pdfBytes: ByteArray): ImageBitmap? {
    val data = pdfBytes.toNSDataBc() ?: return null
    val doc = PDFDocument(data = data) ?: return null
    if (doc.pageCount.toInt() <= 0) return null
    val page = doc.pageAtIndex(0u) ?: return null

    // page size in points
    val (pw, ph) = page.boundsForBox(kPDFDisplayBoxMediaBox).useContents { size.width to size.height }
    if (pw <= 0.0 || ph <= 0.0) return null

    val renderScale = 3.0                                    // 3x for a crisp, decodable barcode
    val outW = pw * renderScale
    val outH = ph * renderScale

    // PDFKit renders the page for us; then composite onto white
    val thumb = page.thumbnailOfSize(CGSizeMake(outW, outH), kPDFDisplayBoxMediaBox)

    val format = UIGraphicsImageRendererFormat.defaultFormat().apply {
        scale = 1.0                                           // 1 px per unit — we sized explicitly
        opaque = true
    }
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(outW, outH), format = format)
    val composed: UIImage = renderer.imageWithActions {
        UIColor.whiteColor.setFill()
        UIRectFill(CGRectMake(0.0, 0.0, outW, outH))
        thumb.drawInRect(CGRectMake(0.0, 0.0, outW, outH))
    }
    return composed.toImageBitmapBc()
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun detectBarcodesInImage(image: ImageBitmap): List<DetectedBarcode> {
    // ImageBitmap → PNG bytes (Skia, from ImageBitmapPng.ios.kt) → UIImage → CGImage
    val png = runCatching { image.toPngBytes() }.getOrNull() ?: return emptyList()
    val uiImage = png.toNSDataBc()?.let { UIImage(data = it) } ?: return emptyList()
    val cg = uiImage.CGImage ?: return emptyList()

    val request = VNDetectBarcodesRequest()                   // all symbologies by default
    val handler = VNImageRequestHandler(cGImage = cg, options = emptyMap<Any?, Any?>())

    val ok = runCatching { handler.performRequests(listOf(request), null) }.getOrDefault(false)
    if (!ok) return emptyList()

    val observations = request.results?.filterIsInstance<VNBarcodeObservation>() ?: return emptyList()
    return observations.map { obs ->
        // Vision: normalized rect, origin BOTTOM-left → flip to top-left
        val (x, y, w, h) = obs.boundingBox.useContents {
            listOf(origin.x, origin.y, size.width, size.height)
        }
        DetectedBarcode(
            rawValue = obs.payloadStringValue ?: "",
            left = x.toFloat(),
            top = (1.0 - (y + h)).toFloat(),
            right = (x + w).toFloat(),
            bottom = (1.0 - y).toFloat(),
        )
    }
}

// ── bridging helpers (same patterns as ShareFile.ios / PdfExport.ios) ──

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSDataBc(): NSData? {
    if (isEmpty()) return null
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.convert())
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArrayBc(): ByteArray {
    val size = length.toInt()
    val result = ByteArray(size)
    if (size > 0) {
        result.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }
    return result
}

@OptIn(ExperimentalResourceApi::class)
private fun UIImage.toImageBitmapBc(): ImageBitmap? {
    val png = UIImagePNGRepresentation(this) ?: return null
    return runCatching { png.toByteArrayBc().decodeToImageBitmap() }.getOrNull()
}