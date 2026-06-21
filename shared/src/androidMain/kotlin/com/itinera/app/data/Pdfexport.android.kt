package com.itinera.app.data

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream

actual fun buildItineraryPdf(title: String, lines: List<String>): ByteArray {
    val pageWidth = 595      // A4 @ 72 dpi
    val pageHeight = 842
    val margin = 40f
    val lineHeight = 16f
    val maxTextWidth = pageWidth - margin * 2

    val doc = PdfDocument()

    val titlePaint = Paint().apply {
        textSize = 18f
        isFakeBoldText = true
        isAntiAlias = true
        color = Color.BLACK
    }
    val bodyPaint = Paint().apply {
        textSize = 11f
        isAntiAlias = true
        color = Color.DKGRAY
    }

    var pageNumber = 1
    var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create())
    var canvas = page.canvas
    var y = margin + 12f

    canvas.drawText(title, margin, y, titlePaint)
    y += lineHeight * 2

    // Word-wrap a line so long journeys/expenses don't run off the page.
    fun wrap(text: String, paint: Paint): List<String> {
        if (text.isEmpty()) return listOf("")
        val words = text.split(" ")
        val out = mutableListOf<String>()
        var current = ""
        for (w in words) {
            val candidate = if (current.isEmpty()) w else "$current $w"
            if (paint.measureText(candidate) <= maxTextWidth) {
                current = candidate
            } else {
                if (current.isNotEmpty()) out.add(current)
                current = w
            }
        }
        if (current.isNotEmpty()) out.add(current)
        return out
    }

    for (raw in lines) {
        for (line in wrap(raw, bodyPaint)) {
            if (y > pageHeight - margin) {
                doc.finishPage(page)
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create())
                canvas = page.canvas
                y = margin + 12f
            }
            canvas.drawText(line, margin, y, bodyPaint)
            y += lineHeight
        }
    }
    doc.finishPage(page)

    val out = ByteArrayOutputStream()
    doc.writeTo(out)
    doc.close()
    return out.toByteArray()
}