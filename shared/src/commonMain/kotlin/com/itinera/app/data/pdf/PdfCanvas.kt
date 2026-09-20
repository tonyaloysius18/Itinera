package com.itinera.app.data.pdf

/**
 * Tiny drawing surface the trip PDF is laid out against, so the layout lives once in
 * commonMain and each platform only supplies primitives.
 *
 * Coordinates follow PDF convention: origin bottom-left, y grows upward, units are points
 * (A4 = 595 × 842). Rectangles are given by their bottom-left corner.
 */
interface PdfCanvas {
    fun save()
    fun restore()
    fun translate(dx: Float, dy: Float)
    fun scale(sx: Float, sy: Float)

    /** Counter-clockwise, in degrees. */
    fun rotate(degrees: Float)

    /** Multiplies the opacity of everything drawn until the matching [restore]. */
    fun alpha(a: Float)
    fun clipRoundRect(x: Float, y: Float, w: Float, h: Float, r: Float)

    fun fillRoundRect(x: Float, y: Float, w: Float, h: Float, r: Float, color: Int)
    fun strokeRoundRect(x: Float, y: Float, w: Float, h: Float, r: Float, color: Int, lineWidth: Float)
    fun fillCircle(cx: Float, cy: Float, r: Float, color: Int)
    fun strokeCircle(cx: Float, cy: Float, r: Float, color: Int, lineWidth: Float)
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, lineWidth: Float, dash: FloatArray? = null)
    fun fillPath(path: PdfPath, color: Int)
    fun strokePath(path: PdfPath, color: Int, lineWidth: Float)

    fun text(
        s: String, x: Float, y: Float, font: PdfFont, size: Float, color: Int,
        align: PdfAlign = PdfAlign.Left, tracking: Float = 0f,
    )

    fun textWidth(s: String, font: PdfFont, size: Float, tracking: Float = 0f): Float

    /** Draws [image] with its left edge at [x], bottom at [y], [w] wide. Returns the drawn height. */
    fun image(image: PdfImage, x: Float, y: Float, w: Float, tint: Int? = null): Float
}

/** A [PdfCanvas] that also owns the document: call [newPage] before the first draw. */
interface PdfWriter : PdfCanvas {
    fun newPage()
    fun finish(): ByteArray
}

/** Bytes the writer needs up front; loaded from compose resources by the caller. */
class PdfAssets(
    val logoTile: ByteArray,
    val plane: ByteArray,
    val wordmarkFont: ByteArray,
    val taglineFont: ByteArray,
)

enum class PdfFont { Wordmark, Tagline, Regular, Medium, Semibold, Bold, Heavy }
enum class PdfAlign { Left, Center, Right }

/** LogoTile is the square app icon; Plane is itinera_logo.png cropped to the plane + trail. */
enum class PdfImage(val aspect: Float) {
    LogoTile(1f),
    Plane(PLANE_CROP_W / PLANE_CROP_H),
}

// itinera_logo.png is 500×500 with the artwork inside this box (measured from its alpha channel).
const val PLANE_CROP_L = 82f
const val PLANE_CROP_T = 170f
const val PLANE_CROP_W = 336f
const val PLANE_CROP_H = 160f

class PdfPath {
    sealed interface Op {
        data class Move(val x: Float, val y: Float) : Op
        data class Line(val x: Float, val y: Float) : Op
        data class Cubic(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val x: Float, val y: Float) : Op
        data object Close : Op
    }

    val ops = mutableListOf<Op>()
    fun moveTo(x: Float, y: Float) = apply { ops += Op.Move(x, y) }
    fun lineTo(x: Float, y: Float) = apply { ops += Op.Line(x, y) }
    fun curveTo(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) = apply { ops += Op.Cubic(x1, y1, x2, y2, x, y) }
    fun close() = apply { ops += Op.Close }
}

expect fun createPdfWriter(assets: PdfAssets): PdfWriter

fun argb(hex: Long): Int = (0xFF000000L or hex).toInt()
