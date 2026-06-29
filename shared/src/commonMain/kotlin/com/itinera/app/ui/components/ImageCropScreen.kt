package com.itinera.app.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
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
 * Pan/zoom crop. The photo moves behind a fixed window; on confirm we compute which
 * fraction of the source image sits inside the window and pass that to the native
 * cropToRect (thread-safe renderer). Honors the user's framing; no capture API.
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
    var processing by remember { mutableStateOf(false) }

    val transformableState = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 6f)
        val maxX = winSize.width * (scale - 1f) / 2f
        val maxY = winSize.height * (scale - 1f) / 2f
        offset = Offset(
            (offset.x + pan.x).coerceIn(-maxX, maxX),
            (offset.y + pan.y).coerceIn(-maxY, maxY),
        )
    }

    fun confirm() {
        val img = painter.intrinsicSize
        val winW = winSize.width.toFloat()
        val winH = winSize.height.toFloat()
        if (img.isUnspecified || img.width <= 0f || img.height <= 0f || winW <= 0f) return

        val iw = img.width; val ih = img.height
        val cover = maxOf(winW / iw, winH / ih)
        val dispW = iw * cover; val dispH = ih * cover
        val cx = winW / 2f; val cy = winH / 2f
        fun srcX(gx: Float) = ((cx + (gx - cx - offset.x) / scale) - (winW - dispW) / 2f) / cover
        fun srcY(gy: Float) = ((cy + (gy - cy - offset.y) / scale) - (winH - dispH) / 2f) / cover

        val nL = (srcX(0f) / iw).coerceIn(0f, 1f)
        val nT = (srcY(0f) / ih).coerceIn(0f, 1f)
        val nR = (srcX(winW) / iw).coerceIn(0f, 1f)
        val nB = (srcY(winH) / ih).coerceIn(0f, 1f)

        val outW = 1080
        val outH = (outW / aspectRatio).toInt()
        processing = true

        scope.launch {
            try {
                // Must stay on Main so UIKit graphics can render perfectly without freezing
                val bytes = withContext(Dispatchers.Main) {
                    cropToRect(imageBytes, nL, nT, nR, nB, outW, outH)
                }
                onConfirm(bytes)
            } catch (e: Exception) {
                processing = false
                println("CROP FAILED: ${e.message}")
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxWidth(0.86f)
                .aspectRatio(aspectRatio)
                .onSizeChanged { winSize = it }
                .clip(RoundedCornerShape(12.dp))
                .transformable(transformableState),
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale, scaleY = scale,
                        translationX = offset.x, translationY = offset.y,
                    ),
            )
        }
        Box(
            Modifier
                .fillMaxWidth(0.86f)
                .aspectRatio(aspectRatio)
                .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
        )

        Text(
            s.cropAndScale,
            color = Color.White.copy(alpha = 0.65f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 16.dp),
        )

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