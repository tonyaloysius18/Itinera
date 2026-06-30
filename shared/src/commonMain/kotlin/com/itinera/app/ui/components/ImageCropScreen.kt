package com.itinera.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.itinera.app.i18n.LocalStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pan/zoom crop. Shows the full image fitted to the screen, with a dimmed overlay
 * and a transparent "hole" showing the crop window. This allows the user to see
 * the full context of their photo while choosing the crop.
 */
@Composable
fun ImageCropScreen(
    imageBytes: ByteArray,
    aspectRatio: Float = 0.78f,
    onConfirm: (ByteArray) -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val painter = rememberAsyncImagePainter(model = imageBytes)

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var winSize by remember { mutableStateOf(IntSize.Zero) }
    var parentSize by remember { mutableStateOf(IntSize.Zero) }
    var processing by remember { mutableStateOf(false) }

    // Helper to calculate geometry based on current image and screen/window sizes
    fun getMath(): CropMath? {
        val img = painter.intrinsicSize
        if (img.isUnspecified || winSize.width <= 0 || parentSize.width <= 0) return null
        val iw = img.width; val ih = img.height
        val pw = parentSize.width.toFloat(); val ph = parentSize.height.toFloat()
        val winW = winSize.width.toFloat(); val winH = winSize.height.toFloat()

        // ContentScale.Fit on the full screen
        val fitScale = minOf(pw / iw, ph / ih)
        // Scale required to COVER the crop window
        val minScale = maxOf(winW / (iw * fitScale), winH / (ih * fitScale))

        return CropMath(iw, ih, pw, ph, winW, winH, fitScale, minScale)
    }

    val transformableState = rememberTransformableState { zoom, pan, _ ->
        val m = getMath() ?: return@rememberTransformableState
        scale = (scale * zoom).coerceIn(m.minScale, 8f)

        val dispW = m.iw * m.fitScale * scale
        val dispH = m.ih * m.fitScale * scale
        val maxX = maxOf(0f, (dispW - m.winW) / 2f)
        val maxY = maxOf(0f, (dispH - m.winH) / 2f)

        offset = Offset(
            (offset.x + pan.x).coerceIn(-maxX, maxX),
            (offset.y + pan.y).coerceIn(-maxY, maxY),
        )
    }

    // Initialize scale to "Fill" the crop window as soon as we have sizes
    LaunchedEffect(painter.intrinsicSize, winSize, parentSize) {
        val m = getMath() ?: return@LaunchedEffect
        if (scale == 1f) scale = m.minScale
    }

    fun confirm() {
        val m = getMath() ?: return
        val cx = m.pw / 2f; val cy = m.ph / 2f
        val scl = m.fitScale * scale

        // Map global screen coordinates to source image coordinates
        fun srcX(gx: Float) = (m.iw / 2f) + (gx - cx - offset.x) / scl
        fun srcY(gy: Float) = (m.ih / 2f) + (gy - cy - offset.y) / scl

        val cropL = cx - m.winW / 2f
        val cropT = cy - m.winH / 2f

        val nL = (srcX(cropL) / m.iw).coerceIn(0f, 1f)
        val nT = (srcY(cropT) / m.ih).coerceIn(0f, 1f)
        val nR = (srcX(cropL + m.winW) / m.iw).coerceIn(0f, 1f)
        val nB = (srcY(cropT + m.winH) / m.ih).coerceIn(0f, 1f)

        val outW = 1080
        val outH = (outW / aspectRatio).toInt()
        processing = true

        scope.launch {
            try {
                val bytes = withContext(Dispatchers.Main) {
                    cropToRect(imageBytes, nL, nT, nR, nB, outW, outH)
                }
                onConfirm(bytes)
            } catch (e: Exception) {
                processing = false
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { parentSize = it },
        contentAlignment = Alignment.Center
    ) {
        // 1. The Image (Fitting the screen, so context is visible)
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .transformable(transformableState)
                .graphicsLayer(
                    scaleX = scale, scaleY = scale,
                    translationX = offset.x, translationY = offset.y,
                ),
        )

        // 2. Dimmed Overlay with a hole for the crop window
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            val winW = winSize.width.toFloat()
            val winH = winSize.height.toFloat()
            if (winW <= 0) return@Canvas

            // Darken everything
            drawRect(Color.Black.copy(alpha = 0.55f))

            // Clear the hole
            val left = (size.width - winW) / 2f
            val top = (size.height - winH) / 2f
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(winW, winH),
                cornerRadius = CornerRadius(12.dp.toPx()),
                blendMode = BlendMode.Clear
            )
        }

        // 3. The Crop Window (just the visible border)
        Box(
            Modifier
                .fillMaxWidth(0.86f)
                .aspectRatio(aspectRatio)
                .onSizeChanged { winSize = it }
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
        )

        // Title text
        Text(
            s.cropAndScale,
            color = Color.White.copy(alpha = 0.65f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 16.dp),
        )

        // Bottom Controls
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss, enabled = !processing) { Text(s.cancel, color = Color.White) }
            Button(enabled = !processing, onClick = { confirm() }) {
                if (processing) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(s.usePhoto)
                }
            }
        }
    }
}

private data class CropMath(
    val iw: Float, val ih: Float,
    val pw: Float, val ph: Float,
    val winW: Float, val winH: Float,
    val fitScale: Float,
    val minScale: Float
)
