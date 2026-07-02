package com.itinera.app.data

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.math.roundToInt

/**
 * Extracts the real barcode/QR from an uploaded ticket (PDF or image) and returns a
 * tight crop of it, so we can show the actual scannable code (not a regenerated one).
 *
 * Flow: PDF → render page 1 to a bitmap (or image → decode) → detect barcodes →
 * pick the largest → crop to it with a little quiet-zone padding.
 *
 * Detection + PDF rendering are per-platform (expect/actual):
 *  - Android: ML Kit barcode scanning + PdfRenderer
 *  - iOS:     Vision (VNDetectBarcodesRequest) + PDFKit   (step 1b)
 */

/** A detected code with its box as fractions of the image (0..1, top-left origin). */
data class DetectedBarcode(
    val rawValue: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val area: Float get() = (right - left).coerceAtLeast(0f) * (bottom - top).coerceAtLeast(0f)
}

/** Result of extraction: the cropped code image + its decoded value (may be blank). */
data class BarcodeExtraction(val image: ImageBitmap, val rawValue: String)

expect suspend fun renderPdfFirstPage(pdfBytes: ByteArray): ImageBitmap?
expect suspend fun detectBarcodesInImage(image: ImageBitmap): List<DetectedBarcode>

/**
 * Top-level orchestrator. Returns null if the file can't be rendered/decoded or no
 * barcode is found — caller should then fall back to opening the full file.
 */
@OptIn(ExperimentalResourceApi::class)
suspend fun extractBarcode(bytes: ByteArray, mimeType: String): BarcodeExtraction? {
    val isPdf = mimeType.contains("pdf", ignoreCase = true)
    val page: ImageBitmap = if (isPdf) {
        renderPdfFirstPage(bytes) ?: return null
    } else {
        runCatching { bytes.decodeToImageBitmap() }.getOrNull() ?: return null
    }
    val codes = runCatching { detectBarcodesInImage(page) }.getOrElse { emptyList() }
    val best = codes.maxByOrNull { it.area } ?: return null
    return BarcodeExtraction(cropTo(page, best), best.rawValue)
}

/** Crops [src] to the barcode box plus a thin quiet zone, returns a new bitmap. */
private fun cropTo(src: ImageBitmap, box: DetectedBarcode): ImageBitmap {
    // pad relative to the CODE's size (quiet zone), not the page — a page-relative pad
    // drags in neighbouring banners/text on big PDF pages
    val padX = 0.15f * (box.right - box.left)
    val padY = 0.15f * (box.bottom - box.top)
    val l = (box.left - padX).coerceIn(0f, 1f) * src.width
    val t = (box.top - padY).coerceIn(0f, 1f) * src.height
    val r = (box.right + padX).coerceIn(0f, 1f) * src.width
    val b = (box.bottom + padY).coerceIn(0f, 1f) * src.height
    val cw = (r - l).roundToInt().coerceAtLeast(1)
    val ch = (b - t).roundToInt().coerceAtLeast(1)

    val out = ImageBitmap(cw, ch)
    val canvas = Canvas(out)
    CanvasDrawScope().draw(Density(1f, 1f), LayoutDirection.Ltr, canvas, Size(cw.toFloat(), ch.toFloat())) {
        drawImage(
            image = src,
            srcOffset = IntOffset(l.roundToInt(), t.roundToInt()),
            srcSize = IntSize(cw, ch),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(cw, ch),
        )
    }
    return out
}