package com.itinera.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.itinera.app.resources.Res
import com.itinera.app.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.imageResource
import kotlin.math.roundToInt

/**
 * Builds a single tall PNG-able ImageBitmap of the postcard: front face on top,
 * back face below (each 1748×1240 → sheet 1748×2480). The text (From/country,
 * date, travellers) is baked in at the same fractional positions the live cards use.
 *
 * Rendered by drawing directly into an ImageBitmap with CanvasDrawScope — each photo
 * slot is masked inside its own isolated saveLayer (never a nested offscreen capture),
 * which is what avoids the iOS GraphicsLayer-capture crash we hit before.
 */

private const val CARD_W = 1748f
private const val CARD_H = 1240f

private const val CARD_GAP = 70f

// ── front constants (mirror PostcardFront.kt) ──
private val F_HEART = SlotR(0.046f, 0.381f, 0.477f, 0.612f)
private val F_RECT  = SlotR(0.407f, 0.213f, 0.593f, 0.787f)
private const val F_LINE_X = 0.074f
private const val F_LINE_BASELINE = 0.325f
private const val F_LINE_TOP = 0.120f
private const val F_LINE_W = 0.372f
private const val F_LINE_ROT = -7f
private const val F_LINE_MAX_FONT = 0.100f

// ── back constants (mirror PostcardBack.kt) ──
private val B_TOP    = SlotR(0.517f, 0.018f, 0.432f, 0.444f)
private val B_BOTTOM = SlotR(0.480f, 0.470f, 0.455f, 0.512f)
private const val B_LINE_X = 0.094f
private const val B_LINE_BASELINE = 0.262f
private const val B_LINE_TOP = 0.085f
private const val B_LINE_W = 0.360f
private const val B_LINE_ROT = 0f
private const val B_LINE_MAX_FONT = 0.062f
private const val B_BLOCK_X = 0.052f
private const val B_BLOCK_Y = 0.340f
private const val B_BLOCK_W = 0.370f

private data class SlotR(val x: Float, val y: Float, val w: Float, val h: Float)

private val INK = Color(0xFF111111)
private val INK_SOFT = Color(0xFF333333)

/**
 * Composable that gathers everything needed (layer bitmaps, fonts, a density-1 text
 * measurer) and returns a suspend lambda. Invoke the lambda off the main work to fetch
 * the slot photos from their URLs and produce the final sheet bitmap.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun rememberPostcardExporter(
    country: String,
    dateRange: String,
    travellers: List<String>,
    heartUrl: String,
    rectUrl: String,
    backTopUrl: String,
    backBottomUrl: String,
    loadBytes: suspend (String) -> ByteArray?,
): suspend () -> ImageBitmap {
    // front layers
    val paper      = imageResource(Res.drawable.postcard_paper)
    val halftone   = imageResource(Res.drawable.postcard_halftone)
    val map        = imageResource(Res.drawable.postcard_map)
    val heartFrame = imageResource(Res.drawable.postcard_heart_frame)
    val rectFrame  = imageResource(Res.drawable.postcard_rect_frame)
    val heartMask  = imageResource(Res.drawable.heart_fill)
    val rectMask   = imageResource(Res.drawable.rect_fill)
    val title      = imageResource(Res.drawable.postcard_title)
    val plane      = imageResource(Res.drawable.postcard_plane)
    // back layers
    val pbMap        = imageResource(Res.drawable.pb_map)
    val pbFrameTop   = imageResource(Res.drawable.pb_frame_top)
    val pbFrameBot   = imageResource(Res.drawable.pb_frame_bottom)
    val pbMaskTop    = imageResource(Res.drawable.pb_mask_top)
    val pbMaskBot    = imageResource(Res.drawable.pb_mask_bottom)
    val pbStamp      = imageResource(Res.drawable.pb_stamp)
    val pbPlane      = imageResource(Res.drawable.pb_plane)
    val pbEnvelope   = imageResource(Res.drawable.pb_envelope)
    val pbTitle      = imageResource(Res.drawable.pb_title)

    val resolver = LocalFontFamilyResolver.current
    // density 1f → 1.sp == 1px, so the fraction math lines up at native bitmap resolution
    val measurer = remember(resolver) { TextMeasurer(resolver, Density(1f, 1f), LayoutDirection.Ltr) }

    val frontFromFont = FontFamily(Font(Res.font.eagle_lake))
    val backTitleFont = FontFamily(Font(Res.font.caudex_bold))
    val backBodyFont  = FontFamily(Font(Res.font.eagle_lake))

    return remember(country, dateRange, travellers, heartUrl, rectUrl, backTopUrl, backBottomUrl) {
        suspend {
            val heart  = loadImg(heartUrl, loadBytes)
            val rect   = loadImg(rectUrl, loadBytes)
            val top    = loadImg(backTopUrl, loadBytes)
            val bottom = loadImg(backBottomUrl, loadBytes)

            renderSheet(
                paper, halftone, map, heartFrame, rectFrame, heartMask, rectMask, title, plane, heart, rect,
                pbMap, pbFrameTop, pbFrameBot, pbMaskTop, pbMaskBot, pbStamp, pbPlane, pbEnvelope, pbTitle, top, bottom,
                country, dateRange, travellers,
                frontFromFont, backTitleFont, backBodyFont, measurer,
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
private suspend fun loadImg(url: String, loadBytes: suspend (String) -> ByteArray?): ImageBitmap? =
    if (url.isBlank()) null
    else loadBytes(url)?.let { runCatching { it.decodeToImageBitmap() }.getOrNull() }

private fun renderSheet(
    fPaper: ImageBitmap, fHalftone: ImageBitmap, fMap: ImageBitmap,
    fHeartFrame: ImageBitmap, fRectFrame: ImageBitmap, fHeartMask: ImageBitmap, fRectMask: ImageBitmap,
    fTitle: ImageBitmap, fPlane: ImageBitmap, fHeartPhoto: ImageBitmap?, fRectPhoto: ImageBitmap?,
    bMap: ImageBitmap, bFrameTop: ImageBitmap, bFrameBot: ImageBitmap, bMaskTop: ImageBitmap, bMaskBot: ImageBitmap,
    bStamp: ImageBitmap, bPlane: ImageBitmap, bEnvelope: ImageBitmap, bTitle: ImageBitmap,
    bTopPhoto: ImageBitmap?, bBottomPhoto: ImageBitmap?,
    country: String, dateRange: String, travellers: List<String>,
    frontFromFont: FontFamily, backTitleFont: FontFamily, backBodyFont: FontFamily,
    measurer: TextMeasurer,
): ImageBitmap {
    val w = CARD_W.roundToInt()
    val h = (CARD_H * 2f + CARD_GAP).roundToInt()
    val image = ImageBitmap(w, h)
    val canvas = Canvas(image)

    CanvasDrawScope().draw(Density(1f, 1f), LayoutDirection.Ltr, canvas, Size(w.toFloat(), h.toFloat())) {
        // ── FRONT (top half, originY = 0) ──
        fullArt(fPaper, 0f); fullArt(fHalftone, 0f); fullArt(fMap, 0f)
        fHeartPhoto?.let { maskedPhoto(it, fHeartMask, rectOf(F_HEART, 0f)) }
        fRectPhoto?.let { maskedPhoto(it, fRectMask, rectOf(F_RECT, 0f)) }
        fullArt(fHeartFrame, 0f); fullArt(fRectFrame, 0f); fullArt(fTitle, 0f); fullArt(fPlane, 0f)
        fromLine(
            measurer, "From  $country", frontFromFont, INK,
            F_LINE_X * CARD_W, F_LINE_TOP * CARD_H,
            F_LINE_W * CARD_W, (F_LINE_BASELINE - F_LINE_TOP) * CARD_H,
            F_LINE_MAX_FONT * CARD_W, F_LINE_ROT,
        )

        // ── BACK (bottom half, originY = CARD_H) ──
        val by = CARD_H + CARD_GAP
        fullArt(fPaper, by); fullArt(bMap, by)
        bTopPhoto?.let { maskedPhoto(it, bMaskTop, rectOf(B_TOP, by)) }
        bBottomPhoto?.let { maskedPhoto(it, bMaskBot, rectOf(B_BOTTOM, by)) }
        fullArt(bFrameTop, by); fullArt(bFrameBot, by); fullArt(bStamp, by)
        fullArt(bPlane, by); fullArt(bEnvelope, by); fullArt(bTitle, by)
        fromLine(
            measurer, "From  $country", backTitleFont, INK,
            B_LINE_X * CARD_W, by + B_LINE_TOP * CARD_H,
            B_LINE_W * CARD_W, (B_LINE_BASELINE - B_LINE_TOP) * CARD_H,
            B_LINE_MAX_FONT * CARD_W, B_LINE_ROT,
        )
        backBlock(measurer, by, backTitleFont, backBodyFont, dateRange, travellers)
    }
    return image
}

private fun rectOf(s: SlotR, originY: Float): Rect {
    val left = s.x * CARD_W
    val top = originY + s.y * CARD_H
    return Rect(left, top, left + s.w * CARD_W, top + s.h * CARD_H)
}

private fun DrawScope.fullArt(img: ImageBitmap, originY: Float) {
    drawImage(
        image = img,
        dstOffset = IntOffset(0, originY.roundToInt()),
        dstSize = IntSize(CARD_W.roundToInt(), CARD_H.roundToInt()),
    )
}

/** Photo covers the slot rect, then the mask clips it to the torn shape — isolated in one layer. */
private fun DrawScope.maskedPhoto(photo: ImageBitmap, mask: ImageBitmap, dst: Rect) {
    val canvas = drawContext.canvas
    canvas.saveLayer(dst, Paint())

    // center-crop "cover"
    val pa = photo.width.toFloat() / photo.height
    val da = dst.width / dst.height
    var sw = photo.width; var sh = photo.height; var sx = 0; var sy = 0
    if (pa > da) { sw = (photo.height * da).roundToInt(); sx = (photo.width - sw) / 2 }
    else { sh = (photo.width / da).roundToInt(); sy = (photo.height - sh) / 2 }

    val dOff = IntOffset(dst.left.roundToInt(), dst.top.roundToInt())
    val dSize = IntSize(dst.width.roundToInt(), dst.height.roundToInt())

    canvas.drawImageRect(photo, IntOffset(sx, sy), IntSize(sw, sh), dOff, dSize, Paint())
    canvas.drawImageRect(
        mask, IntOffset.Zero, IntSize(mask.width, mask.height), dOff, dSize,
        Paint().apply { blendMode = BlendMode.DstIn },
    )
    canvas.restore()
}

/** Bottom-left aligned, auto-shrunk to fit width, tilted around its left-baseline. */
private fun DrawScope.fromLine(
    measurer: TextMeasurer, text: String, font: FontFamily, color: Color,
    boxL: Float, boxT: Float, boxW: Float, boxH: Float, maxPx: Float, rotDeg: Float,
) {
    fun style(px: Float) = TextStyle(color = color, fontWeight = FontWeight.Normal, fontFamily = font, fontSize = px.sp)
    var sizePx = maxPx
    var lr = measurer.measure(text, style(sizePx), maxLines = 1)
    while (sizePx > 9f && lr.size.width > boxW) {
        sizePx -= 2f
        lr = measurer.measure(text, style(sizePx), maxLines = 1)
    }
    val topY = boxT + boxH - lr.size.height
    rotate(rotDeg, pivot = Offset(boxL, boxT + boxH)) {
        drawText(lr, topLeft = Offset(boxL, topY))
    }
}

private fun DrawScope.backBlock(
    measurer: TextMeasurer, originY: Float, titleFont: FontFamily, bodyFont: FontFamily,
    dateRange: String, travellers: List<String>,
) {
    val w = CARD_W; val h = CARD_H
    val blockX = B_BLOCK_X * w
    val blockW = B_BLOCK_W * w
    val labelPx = 0.034f * w
    val valuePx = 0.022f * w
    val dividerPx = 0.0022f * w
    var cy = originY + B_BLOCK_Y * h

    fun text(t: String, font: FontFamily, px: Float, color: Color): Float {
        val lr = measurer.measure(
            t,
            TextStyle(color = color, fontFamily = font, fontSize = px.sp),
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            constraints = Constraints(maxWidth = blockW.roundToInt()),
        )
        drawText(lr, topLeft = Offset(blockX, cy))
        return lr.size.height.toFloat()
    }
    fun divider() {
        drawLine(INK, Offset(blockX, cy), Offset(blockX + blockW, cy), strokeWidth = dividerPx)
        cy += dividerPx
    }

    cy += text("Date:", titleFont, labelPx, INK)
    cy += 0.012f * h
    cy += text(dateRange, bodyFont, valuePx, INK_SOFT)
    divider()
    cy += 0.03f * h
    cy += text("Travellers:", titleFont, labelPx, INK)
    cy += 0.012f * h

    val half = (travellers.size + 1) / 2
    val line1 = travellers.take(half).joinToString(", ")
    val line2 = travellers.drop(half).joinToString(", ")

    cy += text(line1, bodyFont, valuePx, INK_SOFT)
    divider()
    cy += 0.035f * h
    cy += text(line2, bodyFont, valuePx, INK_SOFT)
    divider()
    divider()
}