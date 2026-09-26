package com.itinera.app.data.pdf

/** Vector icons, authored in a 20×20 box (y up) and scaled to whatever size is asked for. */
internal enum class PdfIcon {
    Train, Bus, Flight, Pin, Calendar, People, Wallet, Food, Bed, Bag, Ticket, Cash, Route, Dot,
}

internal fun PdfCanvas.icon(kind: PdfIcon, cx: Float, cy: Float, size: Float, col: Int, bg: Int = Pdf.White) {
    save()
    translate(cx, cy)
    val k = size / 20f
    scale(k, k)
    when (kind) {
        PdfIcon.Train -> {
            fillRoundRect(-5.2f, -7f, 10.4f, 15f, 2.8f, col)
            fillRoundRect(-3.4f, 1.2f, 6.8f, 5.2f, 0f, bg)
            fillCircle(-2.4f, -3.6f, 1f, bg); fillCircle(2.4f, -3.6f, 1f, bg)
            line(-3.6f, -7f, -5.6f, -9.6f, col, 1.4f); line(3.6f, -7f, 5.6f, -9.6f, col, 1.4f)
        }
        PdfIcon.Bus -> {
            fillRoundRect(-8.5f, -5.5f, 17f, 12.5f, 2.6f, col)
            for (x0 in floatArrayOf(-6.6f, -2.1f, 2.4f)) fillRoundRect(x0, 0.8f, 4.2f, 3.8f, 0f, bg)
            fillRoundRect(-6.6f, -2.6f, 13.2f, 0.9f, 0f, bg)
            fillCircle(-4.6f, -5.6f, 1.9f, col); strokeCircle(-4.6f, -5.6f, 1.9f, bg, 1f)
            fillCircle(4.6f, -5.6f, 1.9f, col); strokeCircle(4.6f, -5.6f, 1.9f, bg, 1f)
        }
        PdfIcon.Flight -> {
            rotate(-45f)
            val p = floatArrayOf(
                9.5f, 0f, 6f, 1.3f, 0.5f, 1.4f, -3.5f, 8f, -5.6f, 8f, -3.4f, 1.5f, -7.5f, 1.4f, -9f, 3.6f, -10.4f, 3.6f, -9f, 0f,
                -10.4f, -3.6f, -9f, -3.6f, -7.5f, -1.4f, -3.4f, -1.5f, -5.6f, -8f, -3.5f, -8f, 0.5f, -1.4f, 6f, -1.3f,
            )
            val path = PdfPath().moveTo(p[0], p[1])
            for (i in 2 until p.size step 2) path.lineTo(p[i], p[i + 1])
            fillPath(path.close(), col)
        }
        PdfIcon.Pin -> {
            fillCircle(0f, 2.6f, 5.6f, col)
            fillPath(PdfPath().moveTo(-4.4f, 0f).lineTo(4.4f, 0f).lineTo(0f, -9f).close(), col)
            fillCircle(0f, 2.6f, 2.2f, bg)
        }
        PdfIcon.Calendar -> {
            fillRoundRect(-8f, -7.5f, 16f, 15f, 2.6f, col)
            fillRoundRect(-6.4f, -6f, 12.8f, 9.4f, 1.2f, bg)
            for (x0 in floatArrayOf(-4.4f, -0.9f, 2.6f)) for (y0 in floatArrayOf(-4.6f, -1.6f)) fillRoundRect(x0, y0, 1.9f, 1.7f, 0f, col)
            line(-4f, 6f, -4f, 9.4f, col, 1.6f); line(4f, 6f, 4f, 9.4f, col, 1.6f)
        }
        PdfIcon.People -> {
            fillCircle(-3.6f, 3.6f, 3.1f, col); fillRoundRect(-8.4f, -7.4f, 9.6f, 8.2f, 4.2f, col)
            save(); alpha(0.55f)
            fillCircle(4.6f, 4.4f, 2.6f, col); fillRoundRect(1.2f, -5.6f, 7.6f, 7.6f, 3.6f, col)
            restore()
        }
        PdfIcon.Wallet -> {
            fillRoundRect(-9f, -6.6f, 18f, 13.2f, 2.8f, col)
            fillRoundRect(2f, -2.4f, 8f, 5.2f, 1.6f, bg)
            fillCircle(5.2f, 0.2f, 1.05f, col)
        }
        PdfIcon.Food -> {
            for (x0 in floatArrayOf(-6.4f, -4.2f, -2f)) line(x0, 8f, x0, 2.4f, col, 1.3f)
            line(-6.4f, 2.4f, -2f, 2.4f, col, 1.3f); line(-4.2f, 2.4f, -4.2f, -8.4f, col, 1.3f)
            fillPath(
                PdfPath().moveTo(3.6f, -8.4f).lineTo(3.6f, 8.4f).curveTo(7.8f, 6.2f, 8f, 0.8f, 5.6f, -1.6f).lineTo(5.6f, -8.4f).close(),
                col,
            )
        }
        PdfIcon.Bed -> {
            fillRoundRect(-9.4f, -6.4f, 18.8f, 3.4f, 1f, col); fillRoundRect(-9.4f, -6.4f, 2f, 12.6f, 0f, col)
            fillCircle(-4.4f, 0.6f, 2.2f, col); fillRoundRect(-1.6f, -2.4f, 10.4f, 4.6f, 1.4f, col)
        }
        PdfIcon.Bag -> {
            fillRoundRect(-7.4f, -8f, 14.8f, 12.6f, 2.2f, col)
            strokePath(PdfPath().moveTo(-3.4f, 4f).curveTo(-3.4f, 10f, 3.4f, 10f, 3.4f, 4f), col, 1.5f)
        }
        PdfIcon.Ticket -> {
            fillRoundRect(-9.4f, -5.8f, 18.8f, 11.6f, 2f, col)
            fillCircle(-9.4f, 0f, 2.2f, bg); fillCircle(9.4f, 0f, 2.2f, bg)
            line(3.4f, -4.2f, 3.4f, 4.2f, bg, 0.9f, floatArrayOf(1.2f, 1.6f))
        }
        PdfIcon.Cash -> {
            fillCircle(0f, 0f, 8.2f, col)
            text("€", 0f, -3.9f, PdfFont.Bold, 11f, bg, PdfAlign.Center)
        }
        PdfIcon.Route -> {
            fillCircle(-6f, -5f, 2.4f, col); fillCircle(6f, 5f, 2.4f, col)
            strokePath(PdfPath().moveTo(-6f, -2.6f).curveTo(-6f, 4f, 5f, -4f, 6f, 2.6f), col, 1.4f)
        }
        PdfIcon.Dot -> fillCircle(0f, 0f, 3.4f, col)
    }
    restore()
}

/** Shared palette, ARGB. Brand ramp comes from the app's indigo-to-blue palette. */
internal object Pdf {
    val B1 = argb(0xA9CCF6); val B2 = argb(0x4E9CE8); val B3 = argb(0x6E73D0); val B4 = argb(0x4D52B4); val B5 = argb(0x3A3E93)
    val Navy = argb(0x14174A)
    val Teal = argb(0x6FBF9F)
    val TealDeep = argb(0x2F8F6B)
    val TealTint = argb(0xE4F4EC)
    val Ink = argb(0x0E1024)
    val Muted = argb(0x5B5F7B)
    val Faint = argb(0x8A8EAC)
    val Line = argb(0xDDE0EE)
    val Tint = argb(0xF7F8FC)
    val Tint2 = argb(0xEBEDF8)
    val White = argb(0xFFFFFF)
    val Shadow = argb(0x1C2070)
}
