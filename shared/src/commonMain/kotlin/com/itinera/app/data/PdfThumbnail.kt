package com.itinera.app.data

/**
 * Rasterises page 1 of a PDF and returns it as PNG bytes.
 *
 * There is no cross-platform PDF rasteriser in KMP, and coil only decodes image
 * formats — a PDF must be rendered by the platform first. Android uses
 * PdfRenderer, iOS uses PDFKit.
 *
 * Returns PNG bytes rather than an ImageBitmap because the only caller uploads
 * the result to storage. Returning a bitmap would need a second expect/actual
 * just to encode it again.
 *
 * Null on any failure (corrupt or encrypted PDF, zero pages, missing Android
 * context). Callers fall back to the generic PDF icon.
 *
 * ⚠️ THIS MUST BE THE ONLY DECLARATION of renderPdfFirstPage in common code.
 * The default value lives here and must NOT be repeated in either actual.
 */
expect suspend fun renderPdfFirstPage(bytes: ByteArray, targetWidthPx: Int = 400): ByteArray?