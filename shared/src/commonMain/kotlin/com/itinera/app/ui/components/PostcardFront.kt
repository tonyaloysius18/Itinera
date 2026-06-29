package com.itinera.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.resources.Res
import com.itinera.app.resources.arizonia_regular
import com.itinera.app.resources.eagle_lake
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

// UPDATED - "From {country}" is now one live text line (same font/size/baseline/tilt)

private const val POSTCARD_RATIO = 1748f / 1240f

private data class SlotRect(val x: Float, val y: Float, val w: Float, val h: Float)
private val HEART_SLOT = SlotRect(0.046f, 0.381f, 0.477f, 0.612f)   // matches heart_fill.png
private val RECT_SLOT  = SlotRect(0.407f, 0.213f, 0.593f, 0.787f)   // matches rect_fill.png

// "From {country}" line, measured from the original baked "From":
private const val LINE_X        = 0.074f   // left edge, same as the old "From"
private const val LINE_BASELINE = 0.325f   // tucked a little closer under "Postcard"
private const val LINE_TOP      = 0.120f   // top of the box the text bottom-aligns within
private const val LINE_W        = 0.372f   // runs from LINE_X to just before the rect frame (0.456)
private const val LINE_ROT      = -7f      // matches the "Postcard" tilt (-7.1 deg); flip sign if wrong way
private const val LINE_MAX_FONT = 0.100f   // * card width; bigger cap (short names like "Italy" grow more)

// Set this to the font you used for "From" in Canva so the line matches the design:
//   val PostcardFont = FontFamily(Font(Res.font.your_from_font))


@Composable
fun PostcardFront(
    paper: DrawableResource,
    halftone: DrawableResource,
    map: DrawableResource,
    heartFrame: DrawableResource,
    rectFrame: DrawableResource,
    heartMask: DrawableResource,
    rectMask: DrawableResource,
    title: DrawableResource,
    plane: DrawableResource,
    country: String,
    onPickHeart: () -> Unit,
    onPickRect: () -> Unit,
    modifier: Modifier = Modifier,
    heartPhoto: (@Composable () -> Unit)? = null,
    rectPhoto: (@Composable () -> Unit)? = null,
) {
    val heartMaskBmp = imageResource(heartMask)
    val rectMaskBmp = imageResource(rectMask)

    val PostcardFont = FontFamily(Font(Res.font.eagle_lake))

    BoxWithConstraints(
        modifier.fillMaxWidth().aspectRatio(POSTCARD_RATIO)
    ) {
        val mw = maxWidth
        val mh = maxHeight

        // 1 - base + texture
        FullArt(paper)
        FullArt(halftone)
        FullArt(map)

        // 2 - photos, each masked to its exact opening, between paper and frames
        PhotoSlot(heartMaskBmp, HEART_SLOT, mw, mh, onPickHeart, heartPhoto)
        PhotoSlot(rectMaskBmp,  RECT_SLOT,  mw, mh, onPickRect,  rectPhoto)

        // 3 - torn frames on top
        FullArt(heartFrame)
        FullArt(rectFrame)

        // 4 - title + plane (note: "From" is NOT baked anymore; it's drawn below)
        FullArt(title)
        FullArt(plane)

        // 5 - "From {country}" as ONE live line: bottom-aligned so the baseline is fixed,
        //     auto-shrinks to fit the width, tilted to match the design.
        Box(
            Modifier
                .offset(x = mw * LINE_X, y = mh * LINE_TOP)
                .width(mw * LINE_W)
                .height(mh * (LINE_BASELINE - LINE_TOP))
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 1f)   // pivot at left-baseline
                    rotationZ = LINE_ROT
                },
            contentAlignment = Alignment.BottomStart,
        ) {
            BasicText(
                text = "From  $country",
                style = TextStyle(
                    color = Color(0xFF111111),
                    fontWeight = FontWeight.Normal,
                    fontFamily = PostcardFont,
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 9.sp,
                    maxFontSize = (mw.value * LINE_MAX_FONT).sp,
                    stepSize = 0.5.sp,
                ),
            )
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

/** A tappable photo opening: photo (or "add photo" placeholder) masked to the exact torn shape. */
@Composable
private fun BoxScope.PhotoSlot(
    mask: ImageBitmap,
    slot: SlotRect,
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
            if (photo != null) {
                photo()
            } else {
                Spacer(Modifier.matchParentSize().background(Color(0x22000000)))
            }
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
                Text(
                    "Add photo",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }
        }
    }
}