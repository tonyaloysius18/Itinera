package com.itinera.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val MAX_SCALE = 5f
private const val DOUBLE_TAP_SCALE = 2.5f

@Composable
actual fun PdfViewer(bytes: ByteArray, modifier: Modifier) {
    val context = LocalContext.current
    val pages by produceState<List<ImageBitmap>?>(initialValue = null, bytes) {
        value = withContext(Dispatchers.IO) { renderPdfToBitmaps(context, bytes) }
    }

    if (pages == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(pages!!) { page ->
                ZoomablePage(page)
            }
        }
    }
}

@Composable
private fun ZoomablePage(bitmap: ImageBitmap) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    fun clamp(o: Offset, s: Float): Offset {
        val maxX = (size.width * (s - 1f)) / 2f
        val maxY = (size.height * (s - 1f)) / 2f
        return Offset(
            o.x.coerceIn(-maxX, maxX),
            o.y.coerceIn(-maxY, maxY),
        )
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clipToBounds()
            .onSizeChanged { size = it }
            // double-tap toggles zoom
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = DOUBLE_TAP_SCALE
                            offset = Offset.Zero
                        }
                    },
                )
            }
            // pinch-zoom + pan — only consumes when pinching or already zoomed,
            // so single-finger scrolling between pages still works at 1x
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()
                        val multiTouch = event.changes.size > 1
                        if (scale > 1f || multiTouch) {
                            val newScale = (scale * zoom).coerceIn(1f, MAX_SCALE)
                            scale = newScale
                            offset = if (newScale > 1f) clamp(offset + pan, newScale) else Offset.Zero
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                    if (scale <= 1f) {
                        scale = 1f
                        offset = Offset.Zero
                    }
                }
            },
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}

private fun renderPdfToBitmaps(context: Context, bytes: ByteArray): List<ImageBitmap> {
    val file = File.createTempFile("doc_", ".pdf", context.cacheDir)
    return try {
        file.writeBytes(bytes)
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val targetWidth = 1800   // higher res so zooming in (e.g. QR codes) stays sharp
        val result = ArrayList<ImageBitmap>(renderer.pageCount)
        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val scale = targetWidth.toFloat() / page.width
            val height = (page.height * scale).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(targetWidth, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(AndroidColor.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            result.add(bitmap.asImageBitmap())
        }
        renderer.close()
        pfd.close()
        result
    } catch (e: Exception) {
        println("PDF RENDER FAILED: ${e.message}")
        emptyList()
    } finally {
        file.delete()
    }
}