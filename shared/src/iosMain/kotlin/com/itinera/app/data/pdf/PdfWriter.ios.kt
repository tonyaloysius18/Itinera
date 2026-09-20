package com.itinera.app.data.pdf

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGContextAddCurveToPoint
import platform.CoreGraphics.CGContextAddEllipseInRect
import platform.CoreGraphics.CGContextAddLineToPoint
import platform.CoreGraphics.CGContextAddPath
import platform.CoreGraphics.CGContextBeginPath
import platform.CoreGraphics.CGContextClip
import platform.CoreGraphics.CGContextClosePath
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextFillPath
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGContextMoveToPoint
import platform.CoreGraphics.CGContextRef
import platform.CoreGraphics.CGContextRestoreGState
import platform.CoreGraphics.CGContextRotateCTM
import platform.CoreGraphics.CGContextSaveGState
import platform.CoreGraphics.CGContextScaleCTM
import platform.CoreGraphics.CGContextSetAlpha
import platform.CoreGraphics.CGContextSetBlendMode
import platform.CoreGraphics.CGContextSetLineCap
import platform.CoreGraphics.CGContextSetLineDash
import platform.CoreGraphics.CGContextSetLineJoin
import platform.CoreGraphics.CGContextSetLineWidth
import platform.CoreGraphics.CGContextSetRGBFillColor
import platform.CoreGraphics.CGContextSetRGBStrokeColor
import platform.CoreGraphics.CGContextStrokePath
import platform.CoreGraphics.CGContextTranslateCTM
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageCreateWithImageInRect
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGPathCreateWithRoundedRect
import platform.CoreGraphics.CGPathRelease
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGLineCap
import platform.CoreGraphics.CGLineJoin
import platform.CoreGraphics.CGBlendMode
import platform.CoreText.CTFontManagerRegisterFontsForURL
import platform.CoreText.kCTFontManagerScopeProcess
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.NSKernAttributeName
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIFontWeightBold
import platform.UIKit.UIFontWeightHeavy
import platform.UIKit.UIFontWeightMedium
import platform.UIKit.UIFontWeightRegular
import platform.UIKit.UIFontWeightSemibold
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIImage
import platform.UIKit.drawAtPoint
import platform.UIKit.sizeWithAttributes
import platform.posix.memcpy

actual fun createPdfWriter(assets: PdfAssets): PdfWriter = IosPdfWriter(assets)

private const val PAGE_W = 595.0
private const val PAGE_H = 842.0

// Custom fonts only need registering once per process.
private var fontsRegistered = false

/**
 * Draws through UIKit's PDF context. The context is flipped once per page so coordinates are
 * PDF-style (y up); text is un-flipped locally because UIKit string drawing expects y down.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosPdfWriter(private val assets: PdfAssets) : PdfWriter {
    private val data = NSMutableData()
    private var ctx: CGContextRef? = null
    private var alphaMul = 1.0
    private val alphaStack = ArrayDeque<Double>()

    private val logo: CGImageRef? = UIImage.imageWithData(assets.logoTile.toNSData())?.CGImage
    private val planeCrop: CGImageRef? = UIImage.imageWithData(assets.plane.toNSData())?.CGImage?.let {
        CGImageCreateWithImageInRect(it, CGRectMake(PLANE_CROP_L.toDouble(), PLANE_CROP_T.toDouble(), PLANE_CROP_W.toDouble(), PLANE_CROP_H.toDouble()))
    }
    private val tinted = HashMap<Int, CGImageRef?>()
    private val fonts = HashMap<String, UIFont>()

    init {
        registerFonts()
        UIGraphicsBeginPDFContextToData(data, CGRectMake(0.0, 0.0, PAGE_W, PAGE_H), null)
    }

    override fun newPage() {
        UIGraphicsBeginPDFPage()
        ctx = UIGraphicsGetCurrentContext()
        CGContextTranslateCTM(ctx, 0.0, PAGE_H)
        CGContextScaleCTM(ctx, 1.0, -1.0)
        alphaMul = 1.0
        alphaStack.clear()
    }

    override fun finish(): ByteArray {
        UIGraphicsEndPDFContext()
        return data.toByteArray()
    }

    // ── state ────────────────────────────────────────────────────
    override fun save() { CGContextSaveGState(ctx); alphaStack.addLast(alphaMul) }
    override fun restore() { CGContextRestoreGState(ctx); alphaMul = alphaStack.removeLast() }
    override fun translate(dx: Float, dy: Float) = CGContextTranslateCTM(ctx, dx.toDouble(), dy.toDouble())
    override fun scale(sx: Float, sy: Float) = CGContextScaleCTM(ctx, sx.toDouble(), sy.toDouble())
    override fun rotate(degrees: Float) = CGContextRotateCTM(ctx, degrees * kotlin.math.PI / 180.0)
    override fun alpha(a: Float) { alphaMul *= a; CGContextSetAlpha(ctx, alphaMul) }

    private fun fillColor(c: Int) {
        CGContextSetRGBFillColor(ctx, r(c), g(c), b(c), 1.0)
    }

    private fun strokeColor(c: Int) {
        CGContextSetRGBStrokeColor(ctx, r(c), g(c), b(c), 1.0)
    }

    private fun r(c: Int) = ((c shr 16) and 0xFF) / 255.0
    private fun g(c: Int) = ((c shr 8) and 0xFF) / 255.0
    private fun b(c: Int) = (c and 0xFF) / 255.0

    // ── shapes ───────────────────────────────────────────────────
    private fun addRoundRect(x: Float, y: Float, w: Float, h: Float, r: Float) {
        val rr = minOf(r, w / 2, h / 2).toDouble()
        val rect = CGRectMake(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
        if (rr <= 0.0) {
            CGContextBeginPath(ctx)
            CGContextMoveToPoint(ctx, x.toDouble(), y.toDouble())
            CGContextAddLineToPoint(ctx, (x + w).toDouble(), y.toDouble())
            CGContextAddLineToPoint(ctx, (x + w).toDouble(), (y + h).toDouble())
            CGContextAddLineToPoint(ctx, x.toDouble(), (y + h).toDouble())
            CGContextClosePath(ctx)
        } else {
            val path = CGPathCreateWithRoundedRect(rect, rr, rr, null)
            CGContextBeginPath(ctx)
            CGContextAddPath(ctx, path)
            CGPathRelease(path)
        }
    }

    override fun clipRoundRect(x: Float, y: Float, w: Float, h: Float, r: Float) {
        addRoundRect(x, y, w, h, r)
        CGContextClip(ctx)
    }

    override fun fillRoundRect(x: Float, y: Float, w: Float, h: Float, r: Float, color: Int) {
        fillColor(color)
        addRoundRect(x, y, w, h, r)
        CGContextFillPath(ctx)
    }

    override fun strokeRoundRect(x: Float, y: Float, w: Float, h: Float, r: Float, color: Int, lineWidth: Float) {
        strokeColor(color)
        CGContextSetLineWidth(ctx, lineWidth.toDouble())
        addRoundRect(x, y, w, h, r)
        CGContextStrokePath(ctx)
    }

    override fun fillCircle(cx: Float, cy: Float, r: Float, color: Int) {
        fillColor(color)
        CGContextBeginPath(ctx)
        CGContextAddEllipseInRect(ctx, CGRectMake((cx - r).toDouble(), (cy - r).toDouble(), (2 * r).toDouble(), (2 * r).toDouble()))
        CGContextFillPath(ctx)
    }

    override fun strokeCircle(cx: Float, cy: Float, r: Float, color: Int, lineWidth: Float) {
        strokeColor(color)
        CGContextSetLineWidth(ctx, lineWidth.toDouble())
        CGContextBeginPath(ctx)
        CGContextAddEllipseInRect(ctx, CGRectMake((cx - r).toDouble(), (cy - r).toDouble(), (2 * r).toDouble(), (2 * r).toDouble()))
        CGContextStrokePath(ctx)
    }

    override fun line(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, lineWidth: Float, dash: FloatArray?) {
        CGContextSaveGState(ctx)
        strokeColor(color)
        CGContextSetLineWidth(ctx, lineWidth.toDouble())
        CGContextSetLineCap(ctx, CGLineCap.kCGLineCapRound)
        if (dash != null) CGContextSetLineDash(ctx, 0.0, dash.map { it.toDouble() }.toDoubleArray().toCValues(), dash.size.convert())
        CGContextBeginPath(ctx)
        CGContextMoveToPoint(ctx, x1.toDouble(), y1.toDouble())
        CGContextAddLineToPoint(ctx, x2.toDouble(), y2.toDouble())
        CGContextStrokePath(ctx)
        CGContextRestoreGState(ctx)
    }

    private fun addPath(path: PdfPath) {
        CGContextBeginPath(ctx)
        for (op in path.ops) when (op) {
            is PdfPath.Op.Move -> CGContextMoveToPoint(ctx, op.x.toDouble(), op.y.toDouble())
            is PdfPath.Op.Line -> CGContextAddLineToPoint(ctx, op.x.toDouble(), op.y.toDouble())
            is PdfPath.Op.Cubic -> CGContextAddCurveToPoint(ctx, op.x1.toDouble(), op.y1.toDouble(), op.x2.toDouble(), op.y2.toDouble(), op.x.toDouble(), op.y.toDouble())
            PdfPath.Op.Close -> CGContextClosePath(ctx)
        }
    }

    override fun fillPath(path: PdfPath, color: Int) {
        fillColor(color); addPath(path); CGContextFillPath(ctx)
    }

    override fun strokePath(path: PdfPath, color: Int, lineWidth: Float) {
        CGContextSaveGState(ctx)
        strokeColor(color)
        CGContextSetLineWidth(ctx, lineWidth.toDouble())
        CGContextSetLineCap(ctx, CGLineCap.kCGLineCapRound)
        CGContextSetLineJoin(ctx, CGLineJoin.kCGLineJoinRound)
        addPath(path)
        CGContextStrokePath(ctx)
        CGContextRestoreGState(ctx)
    }

    // ── text ─────────────────────────────────────────────────────
    private fun uiFont(font: PdfFont, size: Float): UIFont = fonts.getOrPut("$font/$size") {
        val s = size.toDouble()
        when (font) {
            PdfFont.Wordmark -> UIFont.fontWithName("Arizonia-Regular", s)
            PdfFont.Tagline -> UIFont.fontWithName("Caudex-Bold", s)
            PdfFont.Regular -> UIFont.systemFontOfSize(s, UIFontWeightRegular)
            PdfFont.Medium -> UIFont.systemFontOfSize(s, UIFontWeightMedium)
            PdfFont.Semibold -> UIFont.systemFontOfSize(s, UIFontWeightSemibold)
            PdfFont.Bold -> UIFont.systemFontOfSize(s, UIFontWeightBold)
            PdfFont.Heavy -> UIFont.systemFontOfSize(s, UIFontWeightHeavy)
        } ?: UIFont.systemFontOfSize(s)
    }

    private fun attrs(font: PdfFont, size: Float, color: Int, tracking: Float): Map<Any?, Any?> = buildMap {
        put(NSFontAttributeName, uiFont(font, size))
        put(NSForegroundColorAttributeName, UIColor.colorWithRed(r(color), g(color), b(color), 1.0))
        if (tracking != 0f) put(NSKernAttributeName, NSNumber(double = tracking.toDouble()))
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun textWidth(s: String, font: PdfFont, size: Float, tracking: Float): Float =
        (s as NSString).sizeWithAttributes(attrs(font, size, 0, tracking)).useContents { width }.toFloat()

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun text(s: String, x: Float, y: Float, font: PdfFont, size: Float, color: Int, align: PdfAlign, tracking: Float) {
        val w = textWidth(s, font, size, tracking)
        val dx = when (align) { PdfAlign.Left -> 0f; PdfAlign.Center -> -w / 2; PdfAlign.Right -> -w }
        val f = uiFont(font, size)
        CGContextSaveGState(ctx)
        CGContextTranslateCTM(ctx, x.toDouble(), y.toDouble())
        CGContextScaleCTM(ctx, 1.0, -1.0)      // back to y-down so UIKit draws upright
        // drawAtPoint takes the top-left of the line box; shift up by the ascender so the baseline lands on y.
        (s as NSString).drawAtPoint(CGPointMake(dx.toDouble(), -f.ascender), attrs(font, size, color, tracking))
        CGContextRestoreGState(ctx)
    }

    // ── images ───────────────────────────────────────────────────
    override fun image(image: PdfImage, x: Float, y: Float, w: Float, tint: Int?): Float {
        val h = w / image.aspect
        val base = if (image == PdfImage.LogoTile) logo else planeCrop
        val img = if (tint != null && base != null) tinted.getOrPut(tint) { tintedCopy(base, tint) } else base
        if (img != null) CGContextDrawImage(ctx, CGRectMake(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble()), img)
        return h
    }

    /**
     * Recolours an image's opaque pixels. CoreGraphics can't clip to an RGBA mask, and blend modes
     * don't survive into PDF, so the tint is baked into a bitmap once per colour.
     */
    private fun tintedCopy(src: CGImageRef, color: Int): CGImageRef? {
        val w = CGImageGetWidth(src)
        val h = CGImageGetHeight(src)
        val space = CGColorSpaceCreateDeviceRGB()
        val bc = CGBitmapContextCreate(null, w, h, 8u, 0u, space, CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value) ?: return null
        val rect = CGRectMake(0.0, 0.0, w.toDouble(), h.toDouble())
        CGContextDrawImage(bc, rect, src)
        CGContextSetBlendMode(bc, CGBlendMode.kCGBlendModeSourceIn)
        CGContextSetRGBFillColor(bc, r(color), g(color), b(color), 1.0)
        CGContextFillRect(bc, rect)
        return CGBitmapContextCreateImage(bc)
    }

    // ── fonts ────────────────────────────────────────────────────
    private fun registerFonts() {
        if (fontsRegistered) return
        fontsRegistered = true
        for ((name, bytes) in listOf("itinera_wordmark.ttf" to assets.wordmarkFont, "itinera_tagline.ttf" to assets.taglineFont)) {
            val path = NSTemporaryDirectory() + name
            bytes.toNSData().writeToFile(path, atomically = true)
            val cfUrl = CFBridgingRetain(NSURL.fileURLWithPath(path))
            CTFontManagerRegisterFontsForURL(cfUrl?.reinterpret(), kCTFontManagerScopeProcess, null)
            CFBridgingRelease(cfUrl)
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { NSData.create(bytes = it.addressOf(0), length = size.convert()) }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val out = ByteArray(size)
    if (size > 0) out.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return out
}
