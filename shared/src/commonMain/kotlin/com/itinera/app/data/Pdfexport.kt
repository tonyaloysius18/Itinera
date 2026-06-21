package com.itinera.app.data

/**
 * Builds a simple multi-page PDF from a title + body lines and returns the PDF bytes.
 * Implemented per-platform: Android uses android.graphics.pdf.PdfDocument,
 * iOS uses UIGraphics PDF context.
 */
expect fun buildItineraryPdf(title: String, lines: List<String>): ByteArray