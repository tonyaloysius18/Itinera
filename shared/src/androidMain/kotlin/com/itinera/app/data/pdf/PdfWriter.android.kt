package com.itinera.app.data.pdf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream
import java.io.File

actual fun createPdfWriter(assets: PdfAssets): PdfWriter = AndroidPdfWriter(assets)

private const val PAGE_W = 595
private const val PAGE_H = 842

/**
 * Draws through android.graphics.pdf.PdfDocument. The canvas is flipped once per page so all
 * coordinates are PDF-style (y up); text and bitmaps are un-flipped locally so they stay upright.
 */
private class AndroidPdfWriter(assets: PdfAssets) : PdfWriter {
    private val doc = PdfDocument()
    private var page: PdfDocument.Page? = null
    private var canvas: Canvas = Canvas()
    private var pageNo = 0
    private var alphaMul = 1f
    private val alphaStack = ArrayDeque<Float>()

    private val logo = BitmapFactory.decodeByteArray(assets.logoTile, 0, assets.logoTile.size)
    private val plane = BitmapFactory.decodeByteArray(assets.plane, 0, assets.plane.size)
    private val wordmark = typefaceFrom(assets.wordmarkFont, "wordmark")
    private val tagline = typefaceFrom(assets.taglineFont, "tagline")

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    override fun newPage() {
        page?.let { doc.finishPage(it) }
        val p = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, ++pageNo).create())
        page = p
        canvas = p.canvas
        canvas.translate(0f, PAGE_H.toFloat())
        canvas.scale(1f, -1f)
        alphaMul = 1f
        alphaStack.clear()
    }

    override fun finish(): ByteArray {
        page?.let { doc.finishPage(it) }
        page = null
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    // ── state ────────────────────────────────────────────────────
    override fun save() { canvas.save(); alphaStack.addLast(alphaMul) }
    override fun restore() { canvas.restore(); alphaMul = alphaStack.removeLast() }
    override fun translate(dx: Float, dy: Float) = canvas.translate(dx, dy)
    override fun scale(sx: Float, sy: Float) = canvas.scale(sx, sy)
    override fun rotate(degrees: Float) = canvas.rotate(degrees)
    override fun alpha(a: Float) { alphaMul *= a }

    override fun clipRoundRect(x: Float, y: Float, w: Float, h: Float, r: Float) {
        canvas.clipPath(Path().apply { addRoundRect(RectF(x, y, x + w, y + h), r, r, Path.Direction.CW) })
    }

    // ── shapes ───────────────────────────────────────────────────
    private fun fill(color: Int): Paint = paint.apply {
        reset(); isAntiAlias = true; style = Paint.Style.FILL; this.color = color; alpha = (android.graphics.Color.alpha(color) * alphaMul).toInt()
    }

    private fun stroke(color: Int, width: Float, dash: FloatArray? = null): Paint = paint.apply {
        reset(); isAntiAlias = true; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
        this.color = color; alpha = (android.graphics.Color.alpha(color) * alphaMul).toInt(); strokeWidth = width
        if (dash != null) pathEffect = DashPathEffect(dash, 0f)
    }

    override fun fillRoundRect(x: Float, y: Float, w: Float, h: Float, r: Float, color: Int) {
        val rr = minOf(r, w / 2, h / 2)
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), rr, rr, fill(color))
    }

    override fun strokeRoundRect(x: Float, y: Float, w: Float, h: Float, r: Float, color: Int, lineWidth: Float) {
        val rr = minOf(r, w / 2, h / 2)
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), rr, rr, stroke(color, lineWidth))
    }

    override fun fillCircle(cx: Float, cy: Float, r: Float, color: Int) = canvas.drawCircle(cx, cy, r, fill(color))

    override fun strokeCircle(cx: Float, cy: Float, r: Float, color: Int, lineWidth: Float) =
        canvas.drawCircle(cx, cy, r, stroke(color, lineWidth))

    override fun line(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, lineWidth: Float, dash: FloatArray?) =
        canvas.drawLine(x1, y1, x2, y2, stroke(color, lineWidth, dash))

    private fun PdfPath.toPath() = Path().also { p ->
        for (op in ops) when (op) {
            is PdfPath.Op.Move -> p.moveTo(op.x, op.y)
            is PdfPath.Op.Line -> p.lineTo(op.x, op.y)
            is PdfPath.Op.Cubic -> p.cubicTo(op.x1, op.y1, op.x2, op.y2, op.x, op.y)
            PdfPath.Op.Close -> p.close()
        }
    }

    override fun fillPath(path: PdfPath, color: Int) = canvas.drawPath(path.toPath(), fill(color))
    override fun strokePath(path: PdfPath, color: Int, lineWidth: Float) = canvas.drawPath(path.toPath(), stroke(color, lineWidth))

    // ── text ─────────────────────────────────────────────────────
    private fun textPaint(font: PdfFont, size: Float, color: Int, tracking: Float): Paint = paint.apply {
        reset(); isAntiAlias = true; isSubpixelText = true; style = Paint.Style.FILL
        this.color = color; alpha = (android.graphics.Color.alpha(color) * alphaMul).toInt()
        textSize = size
        typeface = when (font) {
            PdfFont.Wordmark -> wordmark
            PdfFont.Tagline -> tagline
            PdfFont.Regular -> Typeface.create("sans-serif", Typeface.NORMAL)
            PdfFont.Medium -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
            PdfFont.Semibold, PdfFont.Bold -> Typeface.create("sans-serif", Typeface.BOLD)
            PdfFont.Heavy -> Typeface.create("sans-serif-black", Typeface.NORMAL)
        }
        letterSpacing = if (size > 0f) tracking / size else 0f
    }

    override fun textWidth(s: String, font: PdfFont, size: Float, tracking: Float): Float =
        textPaint(font, size, 0, tracking).measureText(s)

    override fun text(s: String, x: Float, y: Float, font: PdfFont, size: Float, color: Int, align: PdfAlign, tracking: Float) {
        val p = textPaint(font, size, color, tracking)
        val w = p.measureText(s)
        val dx = when (align) { PdfAlign.Left -> 0f; PdfAlign.Center -> -w / 2; PdfAlign.Right -> -w }
        canvas.save()
        canvas.translate(x, y)
        canvas.scale(1f, -1f)
        canvas.drawText(s, dx, 0f, p)
        canvas.restore()
    }

    // ── images ───────────────────────────────────────────────────
    override fun image(image: PdfImage, x: Float, y: Float, w: Float, tint: Int?): Float {
        val h = w / image.aspect
        val bmp: Bitmap
        val src: Rect?
        when (image) {
            PdfImage.LogoTile -> { bmp = logo; src = null }
            PdfImage.Plane -> {
                bmp = plane
                src = Rect(PLANE_CROP_L.toInt(), PLANE_CROP_T.toInt(), (PLANE_CROP_L + PLANE_CROP_W).toInt(), (PLANE_CROP_T + PLANE_CROP_H).toInt())
            }
        }
        val p = paint.apply {
            reset(); isFilterBitmap = true; isAntiAlias = true
            alpha = (255 * alphaMul).toInt()
            if (tint != null) colorFilter = PorterDuffColorFilter(tint, PorterDuff.Mode.SRC_IN)
        }
        canvas.save()
        canvas.translate(x, y + h)
        canvas.scale(1f, -1f)
        canvas.drawBitmap(bmp, src, RectF(0f, 0f, w, h), p)
        canvas.restore()
        return h
    }

    private fun typefaceFrom(bytes: ByteArray, name: String): Typeface {
        val f = File.createTempFile("pdf_$name", ".ttf")
        return try {
            f.writeBytes(bytes)
            Typeface.createFromFile(f)
        } catch (_: Throwable) {
            Typeface.SERIF
        } finally {
            f.delete()
        }
    }
}
