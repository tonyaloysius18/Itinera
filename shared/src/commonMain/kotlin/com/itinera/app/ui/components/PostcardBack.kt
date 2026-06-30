package com.itinera.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.resources.Res
import com.itinera.app.resources.caudex_bold
import com.itinera.app.resources.eagle_lake
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

private const val PB_RATIO = 1748f / 1240f

private data class PbSlot(val x: Float, val y: Float, val w: Float, val h: Float)
private val PB_TOP    = PbSlot(0.517f, 0.018f, 0.432f, 0.444f)   // matches pb_mask_top.png
private val PB_BOTTOM = PbSlot(0.480f, 0.470f, 0.455f, 0.512f)   // matches pb_mask_bottom.png

// "From {country}" live line
private const val PB_LINE_X        = 0.094f
private const val PB_LINE_BASELINE = 0.262f
private const val PB_LINE_TOP      = 0.085f
private const val PB_LINE_W        = 0.360f
private const val PB_LINE_ROT      = 0f
private const val PB_LINE_MAX_FONT = 0.062f

// Live writing block (Date + Travellers) on the left
private const val PB_BLOCK_X = 0.052f
private const val PB_BLOCK_Y = 0.340f

private const val PB_BLOCK_W = 0.370f

// Set to your Canva font for an exact match with the baked title:

@Composable
fun PostcardBack(
    paper: DrawableResource,
    map: DrawableResource,
    frameTop: DrawableResource,
    frameBottom: DrawableResource,
    maskTop: DrawableResource,
    maskBottom: DrawableResource,
    stamp: DrawableResource,
    plane: DrawableResource,
    envelope: DrawableResource,
    title: DrawableResource,
    country: String,
    dateRange: String,                 // ⬅ NEW: e.g. "12 Jul – 19 Jul"
    travellers: List<String>,          // ⬅ NEW: one name per line
    onPickTop: () -> Unit,
    onPickBottom: () -> Unit,
    modifier: Modifier = Modifier,
    topPhoto: (@Composable () -> Unit)? = null,
    bottomPhoto: (@Composable () -> Unit)? = null,
) {
    val topMaskBmp = imageResource(maskTop)
    val bottomMaskBmp = imageResource(maskBottom)

    val PostcardBackTitle = FontFamily(Font(Res.font.caudex_bold))
    val PostcardBack = FontFamily(Font(Res.font.eagle_lake))



    BoxWithConstraints(modifier.fillMaxWidth().aspectRatio(PB_RATIO)) {
        val mw = maxWidth
        val mh = maxHeight

        FullArt(paper)
        FullArt(map)

        PhotoSlot(topMaskBmp,    PB_TOP,    mw, mh, onPickTop,    topPhoto)
        PhotoSlot(bottomMaskBmp, PB_BOTTOM, mw, mh, onPickBottom, bottomPhoto)

        FullArt(frameTop)
        FullArt(frameBottom)
        FullArt(stamp)
        FullArt(plane)
        FullArt(envelope)
        FullArt(title)

        // "From {country}"
        Box(
            Modifier
                .offset(x = mw * PB_LINE_X, y = mh * PB_LINE_TOP)
                .width(mw * PB_LINE_W)
                .height(mh * (PB_LINE_BASELINE - PB_LINE_TOP))
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 1f)
                    rotationZ = PB_LINE_ROT
                },
            contentAlignment = Alignment.BottomStart,
        ) {
            BasicText(
                text = "From  $country",
                style = TextStyle(color = Color(0xFF111111), fontWeight = FontWeight.Normal, fontFamily = PostcardBackTitle),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(9.sp, (mw.value * PB_LINE_MAX_FONT).sp, 0.5.sp),
            )
        }

        // ── Live writing block: Date + Travellers (To/From removed) ──
        val labelSize = (mw.value * 0.034f).sp
        val valueSize = (mw.value * 0.022f).sp
        Column(
            Modifier
                .offset(x = mw * PB_BLOCK_X, y = mh * PB_BLOCK_Y)
                .width(mw * PB_BLOCK_W),
        ) {
            // Date
            Text("Date:", fontFamily = PostcardBackTitle, fontSize = labelSize, color = Color(0xFF111111))
            Spacer(Modifier.height(mh * 0.012f))
            Text(
                dateRange,
                fontFamily = PostcardBack, fontSize = valueSize, color = Color(0xFF333333),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            HorizontalDivider(color = Color(0xFF111111), thickness = 1.dp)

            Spacer(Modifier.height(mh * 0.03f))

            // Travellers
            Text("Travellers:", fontFamily = PostcardBackTitle, fontSize = labelSize, color = Color(0xFF111111))
            Spacer(Modifier.height(mh * 0.012f))
            // Travelers — split into two underlined rows
            val half = (travellers.size + 1) / 2
            val line1 = travellers.take(half).joinToString(", ")
            val line2 = travellers.drop(half).joinToString(", ")

            Text(line1, fontFamily = PostcardBack, fontSize = valueSize, color = Color(0xFF333333),
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            HorizontalDivider(color = Color(0xFF111111), thickness = 1.dp)

            Spacer(Modifier.height(mh * 0.035f))           // gap between the two lines

            Text(line2, fontFamily = PostcardBack, fontSize = valueSize, color = Color(0xFF333333),
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            HorizontalDivider(color = Color(0xFF111111), thickness = 1.dp)
            HorizontalDivider(color = Color(0xFF111111), thickness = 1.dp)
        }
    }
}

@Composable
private fun BoxScope.FullArt(res: DrawableResource) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = Modifier.matchParentSize(),
        contentScale = ContentScale.FillBounds,
    )
}

@Composable
private fun BoxScope.PhotoSlot(
    mask: ImageBitmap,
    slot: PbSlot,
    mw: Dp,
    mh: Dp,
    onPick: () -> Unit,
    photo: (@Composable () -> Unit)?,
) {
    Box(
        Modifier
            .offset(x = mw * slot.x, y = mh * slot.y)
            .size(width = mw * slot.w, height = mh * slot.h)
            .clickable(onClick = onPick),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawImage(
                        image = mask,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(mask.width, mask.height),
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                        blendMode = BlendMode.DstIn,
                    )
                },
        ) {
            if (photo != null) photo()
            else Spacer(Modifier.matchParentSize().background(Color(0x22000000)))
        }

        if (photo == null) {
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Outlined.AddPhotoAlternate,
                    contentDescription = "Add photo",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(30.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text("Add photo", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            }
        }
    }
}