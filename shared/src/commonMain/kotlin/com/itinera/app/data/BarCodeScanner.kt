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
suspend fun extractBarcode(bytes: ByteArray, mimeType: String): BarcodeExtraction? =
    extractAllBarcodes(bytes, mimeType).firstOrNull()

/**
 * Like [extractBarcode] but returns EVERY code found in the file (e.g. one PDF
 * holding boarding passes for several passengers), largest first, deduplicated
 * by decoded value. Empty list = nothing decodable.
 */
@OptIn(ExperimentalResourceApi::class)
suspend fun extractAllBarcodes(bytes: ByteArray, mimeType: String): List<BarcodeExtraction> {
    val isPdf = mimeType.contains("pdf", ignoreCase = true)
    val page: ImageBitmap = if (isPdf) {
        renderPdfFirstPage(bytes) ?: return emptyList()
    } else {
        runCatching { bytes.decodeToImageBitmap() }.getOrNull() ?: return emptyList()
    }
    // Small/soft images (screenshots, phone photos) often don't decode — give the
    // detector more pixels per module by upscaling so the short side is >= 1400px.
    val minSide = minOf(page.width, page.height)
    val detectImage = if (minSide in 1 until 1400) upscale(page, 1400f / minSide) else page

    val codes = runCatching { detectBarcodesInImage(detectImage) }.getOrElse { emptyList() }
    return codes
        .sortedByDescending { it.area }
        .distinctBy { it.rawValue.ifBlank { "${it.left},${it.top},${it.right},${it.bottom}" } }
        .withoutAppPromoCodes()
        .map { BarcodeExtraction(cropTo(detectImage, it), it.rawValue) }   // crop the same image we detected on
}

/**
 * Tickets often print a second QR that only opens the operator's app (FlixBus puts a
 * flixbus.go.link "manage your booking in the app" code next to the real pass). Counting
 * it as another pass made a one-passenger ticket look like a multi-passenger one, so it
 * was never shown as the assigned traveller's own pass. Drop those app/deep-link codes
 * whenever some other code is present; keep everything if that's all there is.
 */
internal fun List<DetectedBarcode>.withoutAppPromoCodes(): List<DetectedBarcode> {
    val real = filterNot { isAppPromoLink(it.rawValue) }
    return if (real.isEmpty()) this else real
}

private val appPromoMarkers = listOf(
    // deep-link / attribution services (Adjust, Branch, AppsFlyer, Firebase)
    ".go.link/", "adj_t=", "adjust.com/", "app.adjust.", ".app.link/", "onelink.me/", ".page.link/",
    // app stores
    "apps.apple.com/", "itunes.apple.com/", "play.google.com/store", "appgallery.huawei.com/",
)

internal fun isAppPromoLink(raw: String): Boolean {
    val v = raw.trim().lowercase()
    if (!(v.startsWith("http://") || v.startsWith("https://"))) return false
    return appPromoMarkers.any { it in v }
}

/** Scales an ImageBitmap by [factor] (nearest-ish via CanvasDrawScope) for better decoding. */
private fun upscale(src: ImageBitmap, factor: Float): ImageBitmap {
    val w = (src.width * factor).roundToInt().coerceAtLeast(1)
    val h = (src.height * factor).roundToInt().coerceAtLeast(1)
    val out = ImageBitmap(w, h)
    val canvas = Canvas(out)
    CanvasDrawScope().draw(Density(1f, 1f), LayoutDirection.Ltr, canvas, Size(w.toFloat(), h.toFloat())) {
        drawImage(
            image = src,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(src.width, src.height),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(w, h),
        )
    }
    return out
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